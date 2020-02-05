package ml.maxence.loupgarou.commands;

import ml.maxence.loupgarou.LGPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp())
            return false;

        LGPlugin.getInstance().getGame().startGame();
        return true;
    }
}