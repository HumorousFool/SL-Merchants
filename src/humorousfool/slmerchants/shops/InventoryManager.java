package humorousfool.slmerchants.shops;

import humorousfool.slmerchants.api.TraitPlayerShop;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryManager implements Listener
{
    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        if(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() != null || event.getSlot() < 0)
        {
            return;
        }

        if(event.getWhoClicked().getOpenInventory().getTitle().endsWith("'s Shop" + ChatColor.BLACK))
        {
            event.setCancelled(true);
        }

        else if(event.getClickedInventory().getTitle().endsWith("Shop Layout" + ChatColor.BLACK))
        {
            event.setCancelled(true);

            if(!SelectionManager.getInstance().selectedNPCs.containsKey(event.getWhoClicked().getUniqueId()))
            {
                event.getWhoClicked().closeInventory();
                return;
            }

            NPC npc = SelectionManager.getInstance().selectedNPCs.get(event.getWhoClicked().getUniqueId());

            npc.getTrait(TraitPlayerShop.class).setLayoutItem(event.getSlot(), event.getCurrentItem());
            event.getInventory().setItem(event.getSlot(), event.getCurrentItem());
        }

        System.out.println(event.getClickedInventory().getTitle());
    }
}
