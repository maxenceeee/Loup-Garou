package ml.maxence.loupgarou.roles;

import ml.maxence.loupgarou.LGPlugin;
import ml.maxence.loupgarou.entities.LGPlayer;
import ml.maxence.loupgarou.game.WinType;
import ml.maxence.loupgarou.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Witch extends LGRole{

    private static final String POTION_LIFE = "Potion de vie";
    private static final String POTION_DEATH = "Potion de mort";

    private Map<UUID, Boolean[]> potions;

    public Witch() {
        super("witch", "La", "Sorcière", new String[]{"Vous avez deux potions, une pour rendre la vie", "et une pour la prendre.", "Utilisez les à bon escient !"});
        potions = new HashMap<>();
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
    public void handleNightTurnStart(Set<LGPlayer> players) {
        super.handleNightTurnStart(players);
        for (LGPlayer lgp : players) {
            Player p = lgp.getPlayer();
            if (!potions.containsKey(lgp.getPlayer().getUniqueId()))
                potions.put(lgp.getPlayer().getUniqueId(), new Boolean[]{true, true});
            openStand(lgp);
        }
    }

    @Override
    public void handleNightTurnEnd(Set<LGPlayer> oldplayers) {
        super.handleNightTurnEnd(oldplayers);
        for (LGPlayer lgp : oldplayers) {
            Player p = lgp.getPlayer();
            p.closeInventory();
        }
    }

    private void openStand(LGPlayer player)
    {
        Boolean[] pot = this.potions.get(player.getPlayer().getUniqueId());
        if (pot == null)
            return ;
        Inventory inv = LGPlugin.getInstance().getServer().createInventory(null, InventoryType.BREWING, ChatColor.DARK_PURPLE + "Alambic de Sorcière");
        Set<LGPlayer> dead = LGPlugin.getInstance().getGame().getDeaths().keySet();

        ItemStack potion1 = new ItemBuilder(Material.POTION, 1).setName(ChatColor.LIGHT_PURPLE + POTION_LIFE).setLore(dead.isEmpty() ? new String[]{ChatColor.RED + "Aucun mort à sauver"} : null).toItemStack();
        PotionMeta potionMeta1 = (PotionMeta) potion1.getItemMeta();
        PotionData potionData1 = new PotionData(PotionType.STRENGTH);
        potionMeta1.setBasePotionData(potionData1);
        potion1.setItemMeta(potionMeta1);


        ItemStack potion2 = new ItemBuilder(Material.POTION, 1).setName(ChatColor.DARK_PURPLE + POTION_DEATH).toItemStack();
        PotionMeta potionMeta2 = (PotionMeta) potion2.getItemMeta();
        PotionData potionData2 = new PotionData(PotionType.INSTANT_HEAL);
        potionMeta2.setBasePotionData(potionData2);
        potion2.setItemMeta(potionMeta2);

        ItemStack emptyPotion1 = new ItemBuilder(Material.GLASS_BOTTLE).setName(ChatColor.LIGHT_PURPLE + POTION_LIFE).setLore(new String[]{ChatColor.RED + "Potion déjà utilisée"}).toItemStack();
        ItemStack emptyPotion2 = new ItemBuilder(Material.GLASS_BOTTLE).setName(ChatColor.LIGHT_PURPLE + POTION_DEATH).setLore(new String[]{ChatColor.RED + "Potion déjà utilisée"}).toItemStack();
        ItemStack quit = new ItemBuilder(Material.BARRIER).setName("Passer votre tour").toItemStack();
        inv.setItem(0, pot[0] ? potion1 : emptyPotion1);
        inv.setItem(2, pot[1] ? potion2 : emptyPotion2);
        inv.setItem(1, quit);

        player.getPlayer().openInventory(inv);
    }

    @Override
    public boolean overrideInventoryClick(LGPlayer source, Inventory i, ItemStack current) {
        if (current == null)
            return false;
        if (i.getName().equals(ChatColor.DARK_PURPLE + "Alambic de Sorcière"))
            return this.onStandInventoryClick(source, current);
        if (i.getName().equals(ChatColor.LIGHT_PURPLE + POTION_LIFE))
            return this.onLifePotionInventoryClick(current);
        if (i.getName().equals(ChatColor.DARK_PURPLE + POTION_DEATH))
            return this.onDeathPotionInventoryClick(current);
        return false;
    }

    private boolean onDeathPotionInventoryClick(ItemStack item)
    {
        if (item.getType() != Material.SKULL_ITEM || item.getDurability() != 3)
            return true;
        String owner = ((SkullMeta)item.getItemMeta()).getOwner();
        if (owner == null)
            return true;
        Player p = LGPlugin.getInstance().getServer().getPlayerExact(owner);
        if (p == null)
            return true;
        LGPlayer wwp = LGPlugin.getInstance().getGame().getPlayerByPlayer(p);
        if (wwp == null)
            return true;
        if (LGPlugin.getInstance().getGame().getDeaths().containsKey(wwp))
            return true;
        Boolean[] pot = this.potions.get(wwp.getPlayer().getUniqueId());
        if (pot == null || !pot[1])
            return true;
        pot[1] = false;
        LGPlugin.getInstance().getGame().diePlayer(wwp, this);
        openStand(wwp);
        return true;
    }

    private boolean onLifePotionInventoryClick(ItemStack item)
    {
        if (item.getType() != Material.SKULL_ITEM || item.getDurability() != 3)
            return true;
        String owner = ((SkullMeta)item.getItemMeta()).getOwner();
        if (owner == null)
            return true;
        Player p = LGPlugin.getInstance().getServer().getPlayerExact(owner);
        if (p == null)
            return true;
        LGPlayer wwp = LGPlugin.getInstance().getGame().getPlayerByPlayer(p);
        if (wwp == null)
            return true;
        if (LGPlugin.getInstance().getGame().getDeaths().containsKey(wwp))
            return true;
        Boolean[] pot = this.potions.get(wwp.getPlayer().getUniqueId());
        if (pot == null || !pot[0])
            return true;
        pot[0] = false;
        LGPlugin.getInstance().getGame().getDeaths().remove(wwp);
        openStand(wwp);
        return true;
    }


    private boolean onStandInventoryClick(LGPlayer source, ItemStack item)
    {
        if (item.getType() == Material.BARRIER)
        {
            source.getPlayer().closeInventory();
            LGPlugin.getInstance().getGame().nextNightEvent();
            return true;
        }
        if (item.getType() != Material.POTION)
            return false;
        Set<LGPlayer> dead = LGPlugin.getInstance().getGame().getDeaths().keySet();
        if (item.getDurability() == 8193 && !dead.isEmpty())
        {
            Inventory inv = LGPlugin.getInstance().getServer().createInventory(null, 27, ChatColor.LIGHT_PURPLE + POTION_LIFE);
            for (LGPlayer player : dead)
                inv.addItem(new ItemBuilder(Material.SKULL_ITEM).setSkullOwner(player.getDisplayName()).toItemStack());
            source.getPlayer().openInventory(inv);
            return true;
        }
        else if (item.getDurability() == 8268)
        {
            Inventory inv = LGPlugin.getInstance().getServer().createInventory(null, 27, ChatColor.DARK_PURPLE + POTION_DEATH);
            for (LGPlayer wwp : LGPlugin.getInstance().getGame().getPlayerInGame())
            {
                if (!wwp.getPlayer().isOnline() || wwp.isSpectator() || dead.contains(wwp))
                    continue ;
                inv.addItem(new ItemBuilder(Material.SKULL_ITEM).setSkullOwner(wwp.getDisplayName()).toItemStack());
            }
            source.getPlayer().openInventory(inv);
            return true;
        }
        return true;
    }
}
