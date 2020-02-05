package ml.maxence.loupgarou.entities;

import ml.maxence.loupgarou.roles.LGRole;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class LGPlayer {

    private Player player;
    private LGPlayer couple;
    private boolean isProtected;
    private LGRole role;
    private boolean spectator;
    private boolean secondTurn;

    public LGPlayer(Player player) {
        this.player = player;
        this.couple = null;
        this.isProtected = false;
        this.role = null;
        this.spectator = false;
        this.secondTurn = false;
    }

    public Player getPlayer() {
        return player;
    }

    public LGPlayer getCouple() {
        return couple;
    }

    public void setCouple(LGPlayer couple) {
        this.couple = couple;
    }

    public boolean isInCouple() {
        return this.couple != null;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean aProtected) {
        isProtected = aProtected;
    }

    public LGRole getRole() {
        return role;
    }

    public void setRole(LGRole role) {
        this.role = role;
    }

    public String getDisplayName() {
        OfflinePlayer off = Bukkit.getOfflinePlayer(player.getUniqueId());
        String name;
        if (off.isOnline())
            name = off.getPlayer().getDisplayName();
        else
            name = off.getName();
        return name;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator() {
        this.spectator = true;
        Player p = this.getPlayer();
        if (p == null)
            return ;
        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.setGameMode(GameMode.ADVENTURE);
        p.setAllowFlight(true);
        p.setFlying(true);
        for (Player player : Bukkit.getOnlinePlayers())
            player.hidePlayer(p);
    }

    public void setSecondTurn(boolean secondTurn) {
        this.secondTurn = secondTurn;
    }

    public boolean isSecondTurn() {
        return secondTurn;
    }
}
