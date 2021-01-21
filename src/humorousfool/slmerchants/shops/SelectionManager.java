package humorousfool.slmerchants.shops;

import humorousfool.slmerchants.SLMerchants;
import humorousfool.slmerchants.api.TraitPlayerShop;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

public class SelectionManager implements Listener
{
    private SelectionManager() {}

    private static SelectionManager instance;

    public static SelectionManager getInstance()
    {
        if(instance == null)
        {
            instance = new SelectionManager();
        }

        return instance;
    }

    public final HashMap<UUID, NPC> selectedNPCs = new HashMap<>();

    @EventHandler
    public void onRightClick(NPCRightClickEvent event)
    {
        if(!event.getClicker().isSneaking())
        {
            return;
        }

        if(!event.getNPC().hasTrait(TraitPlayerShop.class) && !event.getNPC().hasTrait(Owner.class))
        {
            return;
        }
        if(!event.getNPC().getTrait(Owner.class).getOwner().equals(event.getClicker().getName()))
        {
            return;
        }

        selectedNPCs.put(event.getClicker().getUniqueId(), event.getNPC());
        event.getClicker().sendMessage(ChatColor.GREEN + "Shop Selected: right click a chest to bind it, left click to unselect.");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if(!selectedNPCs.containsKey(event.getPlayer().getUniqueId()))
        {
            return;
        }

        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            if(event.getClickedBlock().getType() != Material.CHEST)
            {
                return;
            }

            if(selectedNPCs.get(event.getPlayer().getUniqueId()).isSpawned() &&
                event.getClickedBlock().getLocation().distance(selectedNPCs.get(event.getPlayer().getUniqueId()).getEntity().getLocation()) >
                SLMerchants.getInstance().getConfig().getInt("maxcontainerdistance"))
            {
                event.getPlayer().sendMessage(ChatColor.RED + "Container is too far away.");
                event.setCancelled(true);
                return;
            }

            selectedNPCs.get(event.getPlayer().getUniqueId()).getTrait(TraitPlayerShop.class).setContainerLocation(event.getClickedBlock().getLocation());
            selectedNPCs.remove(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(ChatColor.GREEN + "Shop bound and unselected.");
            event.setCancelled(true);
        }

        else if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            selectedNPCs.remove(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(ChatColor.GREEN + "Shop unselected.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event)
    {
        selectedNPCs.remove(event.getPlayer().getUniqueId());
    }
}
