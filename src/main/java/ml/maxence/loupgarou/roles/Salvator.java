package ml.maxence.loupgarou.roles;

import ml.maxence.loupgarou.LGPlugin;
import ml.maxence.loupgarou.entities.LGPlayer;
import ml.maxence.loupgarou.game.WinType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Salvator extends LGRole{

    public Salvator() {
        super("salvator", "Le", "Salvateur", new String[] {"Une fois par nuit, protégez quelqu'un", "de l'attaque des loups-garous."});
    }

    @Override
    public boolean canPlayAtNight() {
        return true;
    }

    @Override
    public WinType getWinType() {
        return WinType.INNOCENT;
    }

    @Override
    public void handlePlayerClick(LGPlayer source, LGPlayer target) {
        if (!target.getPlayer().isOnline() || target.isSpectator())
            return;
        Player p1 = source.getPlayer();
        if (p1 != null)
            p1.sendMessage(ChatColor.WHITE + " Vous avez protégé : " + ChatColor.YELLOW + target.getPlayer().getDisplayName());
        target.setProtected(true);
        LGPlugin.getInstance().getGame().nextNightEvent();
    }
}
