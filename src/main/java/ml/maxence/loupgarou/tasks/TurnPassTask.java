package ml.maxence.loupgarou.tasks;

import ml.maxence.loupgarou.LGPlugin;
import ml.maxence.loupgarou.entities.LGPlayer;
import ml.maxence.loupgarou.roles.LGRole;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TurnPassTask extends BukkitRunnable  {

    private LGRole role;
    private boolean night;
    private int time;

    public TurnPassTask(LGRole role, boolean night) {
        this.role = role;
        this.night = night;
        this.time = role.getMaximumDelay();
    }

    public TurnPassTask(boolean night, int time) {
        this.night = night;
        this.time = time;
        this.role = null;
    }

    @Override
    public void run() {
        this.time--;
        broadcastActionBarMessage(ChatColor.RED + "Temps restant : " + (this.time >= 600 ? "" : "0") + (this.time / 60) + ":" + (this.time % 60 < 10 ? "0" : "") + (this.time % 60));
        if (time <= 0 && (role == null || LGPlugin.getInstance().getGame().isCurrentlyPlayed(role))) {
            Bukkit.broadcastMessage(ChatColor.RED + "Temps écoulé !");
            broadcastActionBarMessage(ChatColor.RED + "Temps écoulé !");

            if (night)
                LGPlugin.getInstance().getGame().nextNightEvent();
            else
                LGPlugin.getInstance().getGame().nextDayEvent();
        }
    }

    private void broadcastActionBarMessage(String msg) {
        for (LGPlayer player : LGPlugin.getInstance().getGame().getPlayerInGame()) {
            Player p = player.getPlayer();

            if (p != null)
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
        }
    }
}
