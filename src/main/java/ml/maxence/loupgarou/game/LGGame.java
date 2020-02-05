package ml.maxence.loupgarou.game;

import ml.maxence.loupgarou.LGPlugin;
import ml.maxence.loupgarou.entities.LGPlayer;
import ml.maxence.loupgarou.roles.LGRole;
import ml.maxence.loupgarou.tasks.TurnPassTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class LGGame {

    private GameState gameState;
    private List<LGPlayer> playerInGame;
    private int currentEvent;
    private BukkitTask passtask;
    private Map<UUID, UUID> votes;
    private Map<LGPlayer, LGRole> deaths;


    public LGGame() {
        this.gameState = GameState.WAITING;
        this.playerInGame = new ArrayList<>();
        this.currentEvent = 0;
        this.passtask = null;
        this.votes = new HashMap<>();
        this.deaths = new HashMap<>();
    }


    public List<LGPlayer> getPlayerInGame() {
        return playerInGame;
    }

    public Set<LGPlayer> getPlayersByClass(LGRole... clazz) {
        Set<LGPlayer> set = new HashSet<>();
        for (LGPlayer player : this.getPlayerInGame()) {
            if (player.isSpectator() || !player.getPlayer().isOnline())
                continue;
            for (LGRole tmp : clazz)
                if (player.getRole() != null && tmp.getClass().isAssignableFrom(player.getRole().getClass())) {
                    set.add(player);
                    break;
                }
        }
        return set;
    }

    public Set<LGPlayer> getPlayersByWinType(WinType... types) {
        Set<LGPlayer> set = new HashSet<>();
        for (LGPlayer player : this.getPlayerInGame()) {
            if (player.isSpectator() || !player.getPlayer().isOnline())
                continue;
            for (WinType tmp : types)
                if (player.getRole() != null && player.getRole().getWinType().equals(tmp)) {
                    set.add(player);
                    break;
                }
        }
        return set;
    }

    public LGPlayer getPlayerByPlayer(Player player) {
        AtomicReference<LGPlayer> returnedLGPlayer = null;

        playerInGame.stream().filter(lgPlayer -> {
            return lgPlayer.getPlayer().equals(player);
        }).forEach(lgPlayer -> {
            returnedLGPlayer.set(lgPlayer);
        });

        return returnedLGPlayer.get();
    }

    public void startGame() {
        if (this.gameState != GameState.WAITING)
            return;
        this.gameState = GameState.PREPARE;
        drawCircleAndTeleport();
        selectRoles();
        gameState = GameState.DAY_1;
    }

    public void nextNightEvent() {
        this.cancelPassTask();
        LGRole[] roles = LGRole.NIGHT_ORDER;
        if (this.currentEvent >= 0 && this.currentEvent < roles.length) {
            Set<LGPlayer> oldPlayers = getPlayersByClass(roles[currentEvent]);
            if (!oldPlayers.isEmpty() && !roles[currentEvent].isDisabled() && roles[currentEvent].canPlayAtNight()) {
                roles[currentEvent].handleNightTurnEnd(oldPlayers);

                oldPlayers.forEach(lgp -> lgp.getPlayer().addPotionEffect(PotionEffectType.BLINDNESS.createEffect(999, 2)));
            }
        }

        currentEvent++;
        if (this.currentEvent >= roles.length) {
            if (showDeads())
                startDay();
            return;
        }

        Set<LGPlayer> players = getPlayersByClass(roles[currentEvent]);
        if (players.isEmpty() || roles[currentEvent].isDisabled() || !roles[currentEvent].canPlayAtNight()) {
            nextNightEvent();
            return;
        }
        Bukkit.broadcastMessage(ChatColor.WHITE + " " + roles[currentEvent].getPrefix() + " " + roles[currentEvent].getName() + ChatColor.WHITE + " se réveille" + ("Les".equals(roles[currentEvent].getPrefix()) ? "nt" : "") + " !");
        String n = roles[currentEvent].getTextAtNight();

        for (LGPlayer player : players) {
            Player p = player.getPlayer();
            if (n != null) {
                p.sendTitle("", ChatColor.GOLD + n);
            }

            p.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        roles[currentEvent].handleNightTurnStart(players);
        this.passtask = LGPlugin.getInstance().getServer().getScheduler().runTaskTimer(LGPlugin.getInstance(), new TurnPassTask(roles[currentEvent], true), 20, 20);

    }

    public void nextDayEvent() {
        this.cancelPassTask();
        this.currentEvent++;
        if (this.currentEvent > 0) {
            List<UUID> tops = getTopVotes(votes);
            if (this.currentEvent == 2 || tops.size() == 1) {
                if (tops.size() == 1) {
                    LGPlayer player = getPlayerByPlayer(Bukkit.getPlayer(tops.get(0)));
                    if (player != null && player.getPlayer().isOnline() && !player.isSpectator())
                        diePlayer(player, null);
                }
                if (showDeads())
                    startNight();
                return;
            }
            if (tops.isEmpty())
                Bukkit.broadcastMessage("§eAuncun choix de fait, un deuxième vote sera nécessaire !");
            else
                Bukkit.broadcastMessage("§eEgalité dans les voix, un deuxième vote sera nécessaire !");
            for (UUID uuid : tops) {
                LGPlayer player = getPlayerByPlayer(Bukkit.getPlayer(uuid));
                if (player != null && player.getPlayer().isOnline() && !player.isSpectator())
                    player.setSecondTurn(true);
            }
        }
        votes.clear();
        getPlayerInGame().stream().filter(player -> !(!player.getPlayer().isOnline() || player.isSpectator())).forEach(player -> votes.put(player.getPlayer().getUniqueId(), null));

        passtask = LGPlugin.getInstance().getServer().getScheduler().runTaskTimer(LGPlugin.getInstance(), new TurnPassTask(false, 90), 20, 20);
    }

    public void handleDayVote(LGPlayer source, LGPlayer target) {
        if (gameState != GameState.DAY_1 && gameState != GameState.DAY_2)
            return;
        if (gameState == GameState.DAY_2 && !target.isSecondTurn())
            return;
        if (votes.containsKey(source.getPlayer().getUniqueId())) {
            votes.put(source.getPlayer().getUniqueId(), target.getPlayer().getUniqueId());
            Bukkit.broadcastMessage(ChatColor.BOLD + source.getPlayer().getDisplayName() + ChatColor.WHITE + " a voté pour " + ChatColor.BOLD + target.getPlayer().getDisplayName());
        }
    }

    public boolean showDeads() {
        String day = gameState == GameState.NIGHT ? "cette nuit" : "aujourd'hui";
        if (deaths.isEmpty()) {
            Bukkit.broadcastMessage("Personne n'est mort " + day);
            return true;
        }
        List<LGPlayer> lovers = new ArrayList<>();
        StringBuilder sb = new StringBuilder("Vuctime"+ (deaths.size() == 1 ? "" : "s") + (gameState == GameState.NIGHT ? " de " + day : "d'" + day) + " : ");
        int i = 0;
        for (LGPlayer player : deaths.keySet()) {
            player.setSpectator();
            if (player.isInCouple() && !this.deaths.containsKey(player.getCouple()))
                lovers.add(player.getCouple());

            Player p = player.getPlayer();
            if (p != null)
                p.getWorld().strikeLightningEffect(p.getLocation());
            if (i > 0)
                sb.append(ChatColor.WHITE + ", ");

            sb.append(ChatColor.YELLOW).append(player.getDisplayName());
            i++;
        }

        Bukkit.broadcastMessage(sb.toString());
        boolean ok = true;
        for (Map.Entry<LGPlayer, LGRole> entry : deaths.entrySet())
            if (entry.getKey().getRole() != null && !entry.getKey().getRole().handleDeath(entry.getKey(), entry.getValue()))
                ok = false;

        for (LGPlayer player: lovers) {
            player.setSpectator();;
            Player p = player.getPlayer();
            if (p != null)
                p.getWorld().strikeLightningEffect(p.getLocation());

            Bukkit.broadcastMessage(ChatColor.YELLOW + " " + player.getDisplayName() + ChatColor.WHITE + " était amoureux de " + ChatColor.YELLOW + player.getCouple().getDisplayName() + ChatColor.WHITE + " et se suicide donc par amour.");

        }
        this.deaths.clear();;
        return ok;
    }


    public void selectRoles() {
        LGRole[] list = LGRole.VALUES;
        Random r = new Random();

        int numberOfLG = playerInGame.size() / 4 - 1;
        List<LGPlayer> roleSet = new ArrayList<>();
        for (int i = 0; i < playerInGame.size(); i++) {
            LGPlayer player = playerInGame.get(r.nextInt());
            while (playerInGame.contains(player)) {
                player = playerInGame.get(r.nextInt());
            }

            if (player.getRole() != null) continue;

            if (numberOfLG > 0) {
                player.setRole(LGRole.WEREWOLF);
                numberOfLG--;
                continue;
            }

            if (list[i] == LGRole.WEREWOLF) continue;

            if (list.length <= i) {
                player.setRole(LGRole.SIMPLE_VILLAGER);
                continue;
            }

            player.setRole(list[i]);
        }
    }

    public void startNight() {
        if (checkEnd())
            return;
        gameState = GameState.NIGHT;
        Bukkit.getWorlds().get(0).setTime(1500L);
        currentEvent = -1;
        for (LGPlayer lgPlayer : playerInGame) {
            lgPlayer.setProtected(false);

            if (lgPlayer.isSpectator() || !lgPlayer.getPlayer().isOnline())
                continue;

            lgPlayer.setSecondTurn(false);
            Player player = lgPlayer.getPlayer();

            player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(999, 2));
        }

        Bukkit.broadcastMessage(ChatColor.RED + "La nuit tombe sur OnyxCity...");
        nextNightEvent();
    }

    public void startDay() {
        if (checkEnd())
            return;
        gameState = GameState.DAY_1;

        Bukkit.getWorlds().get(0).setTime(3000L);

        for (LGPlayer lgPlayer : playerInGame) {
            lgPlayer.setProtected(false);

            if (lgPlayer.isSpectator() || !lgPlayer.getPlayer().isOnline())
                continue;

            Player player = lgPlayer.getPlayer();

            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }

        Bukkit.broadcastMessage("§aLe jour vient de se lever !");
        Bukkit.broadcastMessage("Il est temps de voter pour savoir qui va mourrir aujourd'hui");
        currentEvent = -1;

        nextDayEvent();
    }


    public boolean checkEnd() {
        if (getPlayerInGame().size() == 1) //Just for me
            return false;

        Map<LGRole, Integer> roles = new HashMap<>();
        for (LGPlayer player : getPlayerInGame()) {
            if (player.isSpectator() || !player.getPlayer().isOnline())
                continue;

            Integer i = roles.get(player.getRole());
            if (i == null)
                i = 0;

            i++;
            roles.put(player.getRole(), i);
        }
        Set<LGRole> roleSet = roles.keySet();
        byte result = 0;
        int total = 0;

        for (LGRole role : roleSet) {
            total += roles.get(role);
            if (role.getWinType() == WinType.INNOCENT)
                result |= 1;
            if (role.getWinType() == WinType.WEREWOLF)
                result |= 2;
            if (role.getWinType() == WinType.ANSWER)
                result |= 4;
        }

        if (total == 0) {
            String[] messages = new String[]{"§aTout le monde a perdu, il n'y a plus personne en vie dans le village...", "§aLes bâtiments resteront abandonnées et tomberont en ruine bientôt !"};

            Bukkit.broadcastMessage(String.valueOf(messages));
            finishGame();
            return true;
        }
        if (total == 2) {
            LGPlayer[] lgPlayer = new LGPlayer[2];
            int i = 0;
            for (LGRole role : roleSet) {
                Set<LGPlayer> tmp = getPlayersByClass(role);
                for (LGPlayer lgp : tmp) {
                    lgPlayer[i] = lgp;
                    i++;
                }
            }
            if (lgPlayer[0].isInCouple() && lgPlayer[1].isInCouple() && lgPlayer[0].getCouple().equals(lgPlayer[1])) {
                String messages = "§dLe couple (§a " + lgPlayer[0].getPlayer().getDisplayName() + " §d& §a" + lgPlayer[1].getPlayer().getDisplayName() + "§d ont gagnés (C'est bô l'amour) !";
                Bukkit.broadcastMessage(messages);
                finishGame();
                return true;
            }
        }
        if (result == 1) {
            String[] messages = new String[]{"§aLes villageois ont gagnés !", "§aLe village est sauvé !"};
            Bukkit.broadcastMessage(String.valueOf(messages));
            finishGame();
            return true;
        }
        if (result == 2) {
            String[] messages = new String[]{"§cLes loups ont gagné !", "Tout le village a été dévoré (Miam !) !"};

            finishGame();
            return true;
        }

        if (total == 1 && result == 4) {
            LGPlayer lgPlayer = null;
            for (LGRole role : LGRole.VALUES)
                if (role.getWinType() == WinType.ANSWER)
                    lgPlayer = lgPlayer == null ? getPlayersByClass(role).stream().findFirst().orElse(null) : lgPlayer;

            if (lgPlayer == null)
                return false;

            String[] messages = new String[]{"§e" + lgPlayer.getRole().getPrefix() + lgPlayer.getRole().getName()
                    + "§r(" + lgPlayer.getPlayer().getDisplayName() + ")§e à gagné !", "§eIl / Elle est le dernier survivant en vie !"};
            Bukkit.broadcastMessage(String.valueOf(messages));

            finishGame();
            return true;
        }
        return false;
    }

    public void finishGame() {
        gameState = GameState.END;
    }

    public boolean isCurrentlyPlayed(LGRole role) {
        return gameState == GameState.END && LGRole.NIGHT_ORDER[currentEvent].equals(role);
    }

    public void cancelPassTask() {
        if (this.passtask == null)
            return;
        this.passtask.cancel();;
        this.passtask = null;
    }

    public List<UUID> getTopVotes(Map<UUID, UUID> list) {
        Map<UUID, Integer> counts = new HashMap<>();
        for (Map.Entry<UUID, UUID> entry : list.entrySet()) {
            if (entry.getValue() == null)
                continue;
            Integer i = counts.get(entry.getValue());
            if (i == null)
                i = 0;
            i++;
            counts.put(entry.getValue(), i);
        }
        List<UUID> tops = new ArrayList<>();
        int top = 0;
        for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > top) {
                tops.clear();
                top = entry.getValue();
            }
            if (entry.getValue() == top)
                tops.add(entry.getKey());
        }

        return tops;
    }

    public void diePlayer(LGPlayer player, LGRole killer) {
        if (killer == null)
            this.deaths.put(player, null);
        else if (player.getRole() != null && player.getRole().canBeKilled(player, killer))
            this.deaths.put(player, killer);
    }

    public GameState getGameState() {
        return gameState;
    }

    public Map<LGPlayer, LGRole> getDeaths() {
        return deaths;
    }


    private void drawCircleAndTeleport() {
        int nmbrPlace = getPlayerInGame().size();
        double radius = (getPlayerInGame().size() / 4) * 3;
        Location origin = new Location(Bukkit.getWorlds().get(0), 0,0,0);

        for (int i = 0; i < nmbrPlace; i++) {
            double angle = 2 * Math.PI * i / nmbrPlace;
            Location place = origin.clone().add(radius * Math.sin(angle), 0.0d, radius * Math.cos(angle));

            place.getBlock().setType(Material.DIAMOND_BLOCK);

            getPlayerInGame().get(i).getPlayer().teleport(place.clone().add(0, 1, 0));
        }
    }
}
