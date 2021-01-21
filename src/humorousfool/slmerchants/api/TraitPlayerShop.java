package humorousfool.slmerchants.api;

import humorousfool.slmerchants.SLMerchants;
import humorousfool.slmerchants.shops.SelectionManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TraitPlayerShop extends Trait
{
    public TraitPlayerShop()
    {
        super("playerShop");
        maxSize = 9 * SLMerchants.getInstance().getConfig().getInt("defaultrows");
        shopLayout = new String[54];
        shopPrices = new double[54];
    }

    @Persist Location containerLocation;
    @Persist int maxSize;

    @Persist String[] shopLayout;
    @Persist double[] shopPrices;

    @EventHandler
    public void rightClick(NPCRightClickEvent event)
    {
        if(event.getNPC() != getNPC() || event.getClicker().isSneaking())
        {
            return;
        }

        if(getNPC().hasTrait(Owner.class) && getNPC().getTrait(Owner.class).getOwner().equals(event.getClicker().getName()) &&
                SelectionManager.getInstance().selectedNPCs.containsKey(event.getClicker().getUniqueId()))
        {
            event.getClicker().openInventory(getDisplayedInventory(true));
            return;
        }

        if(containerLocation == null)
        {
            event.getClicker().sendMessage(ChatColor.RED + "ERROR: Could not find shop container.");
            return;
        }
        if(getNPC().getEntity().getLocation().distance(containerLocation) > SLMerchants.getInstance().getConfig().getInt("maxcontainerdistance"))
        {
            event.getClicker().sendMessage(ChatColor.RED + "ERROR: Container is too far away.");
            return;
        }

        //Block chest = containerLocation.getBlock();
        //BlockState state = chest.getState();
        //Container container = (Container) state;

        event.getClicker().openInventory(getDisplayedInventory(false));
    }

    public void setLayoutItem(int slot, ItemStack item)
    {
        shopLayout[slot] = item.getType().name();
        shopPrices[slot] = 10D;
    }

    public void setContainerLocation(Location location)
    {
        containerLocation = location;
    }

    public int getExpansionUpgrades()
    {
        return (maxSize / 9) - 3;
    }

    private Inventory getDisplayedInventory(boolean editMode)
    {
        String title = getNPC().getName();
        int upgradeSlots = 0;

        if(editMode)
        {
            title = "Shop Layout";

            if(maxSize < 54)
            {
                upgradeSlots = 9;
            }
        }

        Inventory inv = Bukkit.createInventory(null, maxSize + upgradeSlots, title + ChatColor.BLACK);

        for(int i = 0; i < maxSize - 1; i++)
        {
            if(shopLayout[i] == null)
            {
                shopLayout[i] = Material.AIR.name();
                ItemStack air = new ItemStack(Material.AIR);
                inv.setItem(i, air);
                continue;
            }
            else if(Material.valueOf(shopLayout[i]) == Material.AIR)
            {
                inv.setItem(i, new ItemStack(Material.AIR));
                continue;
            }

            ItemStack tempStack = new ItemStack(Material.valueOf(shopLayout[i]));

            ItemMeta meta = tempStack.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.RED + "Cost: " + ChatColor.GOLD + "$" + shopPrices[i]);
            meta.setLore(lore);
            tempStack.setItemMeta(meta);

            inv.setItem(i, tempStack);
        }

        if(upgradeSlots == 9)
        {
            ItemStack upgrade = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta meta = upgrade.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Buy extra row");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.RED + "Cost: " + ChatColor.GOLD + "$" + SLMerchants.getInstance().getConfig().getInt("costtoexpand"));
            meta.setLore(lore);
            upgrade.setItemMeta(meta);
            inv.setItem(inv.getSize() - 5, upgrade);
        }

        return inv;
    }
}
