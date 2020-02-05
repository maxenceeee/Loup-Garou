package ml.maxence.loupgarou.roles;

import ml.maxence.loupgarou.LGPlugin;
import ml.maxence.loupgarou.entities.LGPlayer;
import ml.maxence.loupgarou.game.WinType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Set;

public class Cupidon extends LGRole{

    private boolean played;
    private LGPlayer[] couple;

    public Cupidon() {
        super("cupidon", "Le", "&dCupidon", new String[]{"Choisissez deux joueurs", "qui s'aimeront jusqu'à la mort"});
        played = false;
        couple = new LGPlayer[2];
        Arrays.fill(couple, null);
    }

    @Override
    public boolean canPlayAtNight() {
        return !played;
    }

    @Override
    public WinType getWinType() {
        return WinType.INNOCENT;
    }

    @Override
    public void handleNightTurnStart(Set<LGPlayer> players) {
        played = true;
    }

    @Override
    public void handlePlayerClick(LGPlayer source, LGPlayer target)
    {
        source.getPlayer().sendMessage(ChatColor.RED + "Vous avez choisi : " + target.getDisplayName());
        if (this.couple[0] == null)
            this.couple[0] = target;
        else if (this.couple[1] == null)
        {
            this.couple[1] = target;
            LGPlugin.getInstance().getGame().nextNightEvent();
        }
    }

    @Override
    public void handleNightTurnEnd(Set<LGPlayer> oldplayers) {
        String msg;
        Player[] p = new Player[2];
        p[0] = this.couple[0] == null ? null : this.couple[0].getPlayer();
        p[1] = this.couple[1] == null ? null : this.couple[1].getPlayer();
        if (p[0] == null || p[1] == null)
            msg = ChatColor.RED + "Aucun choix de fait, il n'y aura pas d'amoureux dans cette partie.";
        else
        {
            msg = ChatColor.RED + "Les deux amoureux sont " + ChatColor.BOLD + p[0].getDisplayName() + ChatColor.RED + " et " + ChatColor.BOLD + p[1].getDisplayName();
            this.couple[0].setCouple(this.couple[1]);
            this.couple[1].setCouple(this.couple[0]);
            p[0].sendMessage(ChatColor.RED + "Vous êtes amoureux de " + p[1].getDisplayName() + ", si l'un d'entre vous meurt, l'autre aussi !");
            p[1].sendMessage(ChatColor.RED + "Vous êtes amoureux de " + p[0].getDisplayName() + ", si l'un d'entre vous meurt, l'autre aussi !");
        }
        oldplayers.stream().filter(lgp -> lgp.getPlayer().isOnline()).forEach(wwp -> wwp.getPlayer().sendMessage(msg));
    }

}
