package ml.maxence.loupgarou.roles;

import ml.maxence.loupgarou.entities.LGPlayer;
import ml.maxence.loupgarou.game.WinType;
import org.bukkit.ChatColor;

public class Seer extends LGRole {

    private boolean locked;

    public Seer() {
        super("seer", "La", "&5Voyante", new String[]{"Une fois par nuit, regardez le", "rôle d'un autre joueur"});
    }

    public boolean canPlayAtNight()
    {
        return true;
    }

    @Override
    public WinType getWinType()
    {
        return WinType.INNOCENT;
    }

    @Override
    public String getTextAtNight()
    {
        return "Choisissez le joueur donc vous voulez voir la carte";
    }

    @Override
    public void handlePlayerClick(LGPlayer source, LGPlayer target)
    {
        if (this.locked || target.isSpectator() || !target.getPlayer().isOnline())
            return ;
        this.locked = true;
        source.getPlayer().sendTitle( ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Rôle de " + target.getDisplayName(), target.getRole().getName(),5, 50, 5);
    }
}
