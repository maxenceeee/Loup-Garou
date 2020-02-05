package ml.maxence.loupgarou.roles;

import ml.maxence.loupgarou.entities.LGPlayer;
import ml.maxence.loupgarou.game.WinType;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public abstract class LGRole {

    public static final LGRole SIMPLE_VILLAGER = new SimpleVillager(); //Villageois
    public static final LGRole SEER = new Seer();                      //Voyante
    public static final LGRole WEREWOLF = new WereWolf();              //Loup Garou
    public static final LGRole LITTLE_GIRL = new LittleGirl();         //Petite Fille
    public static final LGRole SALVATOR = new Salvator();              //Salvateur
    public static final LGRole WITCH = new Witch();                    //Sorcière
    public static final LGRole CUPIDON = new Cupidon();                //Cupidon

    public static final LGRole[] VALUES = new LGRole[]{WITCH, SEER, WEREWOLF, SALVATOR, LITTLE_GIRL,CUPIDON, SIMPLE_VILLAGER};
    public static final LGRole[] NIGHT_ORDER = new LGRole[] {CUPIDON, SEER, SALVATOR, WEREWOLF, WITCH};

    private String prefix;
    private String id;
    private String name;
    private String[] description;
    private boolean disabled;

    public LGRole(String id, String prefix, String name, String[] description) {
        this.prefix = prefix;
        this.id = id;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.description = description;
        this.disabled = false;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String[] getDescription() {
        return description;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public abstract boolean canPlayAtNight();
    public abstract WinType getWinType();
    public void handleNightTurnStart(Set<LGPlayer> players){}
    public void handleNightTurnEnd(Set<LGPlayer> oldplayers) {}
    public void handlePlayerClick(LGPlayer source, LGPlayer target){}

    public boolean overrideInventoryClick(LGPlayer source, Inventory i, ItemStack current) {
        source.getClass();
        i.getClass();
        current.getClass();
        return false;
    }

    public boolean canBeKilled(LGPlayer player, LGRole by)
    {
        // Sonar doesn't like unused variables, even if i need it for POO
        player.getClass();
        by.getClass();
        return true;
    }

    public String getTextAtNight()
    {
        return null;
    }

    public boolean handleDeath(LGPlayer player, LGRole by)
    {
        player.getClass();
        by.getClass();
        return true;
    }

    public int getMaximumDelay()
    {
        return 60;
    }

}
