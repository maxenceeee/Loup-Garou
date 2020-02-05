package ml.maxence.loupgarou.listeners;

import ml.maxence.loupgarou.LGPlugin;
import ml.maxence.loupgarou.entities.LGPlayer;
import ml.maxence.loupgarou.game.LGGame;
import ml.maxence.loupgarou.roles.LGRole;
import ml.maxence.loupgarou.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private LGGame game;

    public PlayerListener() {
        game = LGPlugin.getInstance().getGame();
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickEntity(PlayerInteractEntityEvent event) {
        event.setCancelled(true);

        if (event.getRightClicked() == null || !(event.getRightClicked() instanceof  Player))
            return;

        LGPlayer source = LGPlugin.getInstance().getGame().getPlayerByPlayer(event.getPlayer());
        LGPlayer target = LGPlugin.getInstance().getGame().getPlayerByPlayer((Player) event.getRightClicked());

        if (LGPlugin.getInstance().getGame().getGameState() == GameState.NIGHT && LGPlugin.getInstance().getGame().isCurrentlyPlayed(source.getRole())) {
            source.getRole().handlePlayerClick(source, target);
            return;
        }

        if (LGPlugin.getInstance().getGame().getGameState() == GameState.DAY_1 || LGPlugin.getInstance().getGame().getGameState() == GameState.DAY_2 )
            LGPlugin.getInstance().getGame().handleDayVote(source, target);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        LGPlayer lgPlayer = game.getPlayerByPlayer(event.getPlayer());

        if (game.getGameState() == GameState.DAY_1 || game .getGameState() == GameState.DAY_2) {
            event.setFormat(ChatColor.BOLD + "%1$s " + ChatColor.GRAY + ": %2$s");
            return;
        }

        String message = event.getMessage();

        for (LGRole role : LGRole.VALUES) {
            if (role.equals(LGRole.WEREWOLF)) {
                game.getPlayersByClass(role).forEach(lgPlayer1 -> {lgPlayer1.getPlayer().sendMessage("" + ChatColor.RED + lgPlayer.getPlayer() + ChatColor.GRAY + message); });
            } else if (role.equals(LGRole.LITTLE_GIRL)) {
                game.getPlayersByClass(role).forEach(lgPlayer1 -> {lgPlayer1.getPlayer().sendMessage("" + ChatColor.MAGIC + ChatColor.RED + lgPlayer.getPlayer().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + message);});
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }


    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        GameState gameState = LGPlugin.getInstance().getGame().getGameState();

        if (gameState == GameState.WAITING || gameState == GameState.END) return;


        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        LGPlayer lgPlayer = LGPlugin.getInstance().getGame().getPlayerByPlayer(player);

        lgPlayer.getRole().overrideInventoryClick(lgPlayer, event.getClickedInventory(), event.getCurrentItem());
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(String.format("§a%s à rejoint la partie %s(%d/%d) §a!", event.getPlayer().getDisplayName(), Bukkit.getOnlinePlayers().size() >= 4 ? "§4" : "§a", Bukkit.getMaxPlayers()));
        LGPlugin.getInstance().getGame().getPlayerInGame().add(LGPlugin.getInstance().getGame().getPlayerByPlayer(event.getPlayer()));

    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(String.format("§a%s à rejoint la partie %s(%d/%d) §a!", event.getPlayer().getDisplayName(), Bukkit.getOnlinePlayers().size() >= 4 ? "§4" : "§a", Bukkit.getMaxPlayers()));
        LGPlugin.getInstance().getGame().getPlayerInGame().remove(LGPlugin.getInstance().getGame().getPlayerByPlayer(event.getPlayer()));
    }
}
