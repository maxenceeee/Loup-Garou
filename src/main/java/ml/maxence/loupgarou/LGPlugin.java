package ml.maxence.loupgarou;

import ml.maxence.loupgarou.commands.StartCmd;
import ml.maxence.loupgarou.game.LGGame;
import ml.maxence.loupgarou.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class LGPlugin extends JavaPlugin {

    private static LGPlugin instance;

    private LGGame game;

    @Override
    public void onEnable() {
        instance = this;

        game = new LGGame();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        getCommand("start").setExecutor(new StartCmd());
    }


    @Override
    public void onDisable() {

    }

    public static LGPlugin getInstance() {
        return instance;
    }

    public LGGame getGame() {
        return game;
    }
}
