package ml.maxence.loupgarou.roles;

import ml.maxence.loupgarou.LGPlugin;
import ml.maxence.loupgarou.entities.LGPlayer;
import ml.maxence.loupgarou.game.WinType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class WereWolf extends LGRole{

    private Map<UUID, UUID> choices;
    private Set<LGPlayer> players;

    public WereWolf() {
        super("werewolf", "Les", "&8&lLoup-Garou", new String[]{"La nuit, décidez d'une victime à dévorer. Miam"});
        this.choices = new HashMap<>();
    }

    @Override
    public boolean canPlayAtNight() {
        return true;
    }

    @Override
    public WinType getWinType() {
        return WinType.WEREWOLF;
    }

    @Override
    public String getTextAtNight() {
        return "Choisissez la personne que vous voulez dévorer !";
    }

    @Override
    public void handleNightTurnStart(Set<LGPlayer> players) {
        this.players = players;
        this.choices.clear();
        for (LGPlayer player : players)
            this.choices.put(player.getPlayer().getUniqueId(), null);
        Set<LGPlayer> receivers = LGPlugin.getInstance().getGame().getPlayersByClass(LGRole.LITTLE_GIRL);
        receivers.stream().filter(lgPlayer -> {
            return lgPlayer.getPlayer().isOnline();
        }).forEach(wwp -> wwp.getPlayer().sendTitle("", LGRole.LITTLE_GIRL.getTextAtNight(),5, 50, 5));
    }

    @Override
    public void handleNightTurnEnd(Set<LGPlayer> oldplayers) {
        players.clear();
        List<UUID> tops = LGPlugin.getInstance().getGame().getTopVotes(choices);
        String msg;
        if (tops.size() != 1)
            msg = ChatColor.RED + "Aucun choix de fait, in n'y aura pas de victime des loups-garous ce soir.";
        else {
            LGPlayer player = LGPlugin.getInstance().getGame().getPlayerByPlayer(Bukkit.getPlayer(tops.get(0)));
            if (player == null || !player.getPlayer().isOnline())
                msg = ChatColor.RED + "Le joueur choisi s'est déconnecté, in n'y aura pas de victime des loups-garou ce soir.";
            else {
                if (!player.isProtected())
                    LGPlugin.getInstance().getGame().diePlayer(player, this);
                msg = ChatColor.RED + " La victime de ce soir des loups-garous est : " + player.getPlayer().getDisplayName() + " !";
            }
        }
        players.stream().filter(lgplayer -> lgplayer.getPlayer().isOnline()).forEach(lgp -> lgp.getPlayer().sendMessage(msg));
        choices.clear();
    }

    @Override
    public void handlePlayerClick(LGPlayer source, LGPlayer target) {
        if (target.isSpectator() || !target.getPlayer().isOnline()) {
            source.getPlayer().sendMessage(ChatColor.RED + "Ce joueur est déconnecté.");
            return;
        }
        if (choices.containsKey(source.getPlayer().getUniqueId())) {
            choices.put(source.getPlayer().getUniqueId(), target.getPlayer().getUniqueId());
            for (LGPlayer lgp : players) {
                String msg = ChatColor.RED + "[LOUPS] " + ChatColor.GRAY + source.getPlayer().getDisplayName() + " a voté pour " + target.getPlayer().getDisplayName();
                Player player = lgp.getPlayer();
                if (player != null)
                    player.sendMessage(msg);
            }
        }
    }

}
