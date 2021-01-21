package humorousfool.slmerchants.shops;

import humorousfool.slmerchants.SLMerchants;
import humorousfool.slmerchants.api.TraitPlayerShop;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ShopManager implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "Sender must be a player.");
            return true;
        }

        if(args == null || args.length == 0 || args[0] == null)
        {
            sender.sendMessage(ChatColor.RED + "Valid sub-commands: " + ChatColor.WHITE + "create, delete");
            return true;
        }

        Player player = (Player) sender;

        if(args[0].equalsIgnoreCase("create"))
        {
            if(!sender.hasPermission("slmerchants.user.create"))
            {
                sender.sendMessage(ChatColor.RED + "Insufficient Permissions.");
                return true;
            }
            double createPrice = SLMerchants.getInstance().getConfig().getDouble("costtocreate");

            if(SLMerchants.getInstance().getEconomy().getBalance((OfflinePlayer) sender) < createPrice)
            {
                sender.sendMessage(ChatColor.RED + "Error creating shop: Insufficient funds, you need at least " + ChatColor.GOLD + "$" + createPrice + ChatColor.RED + ".");
                return true;
            }
            else
            {
                SLMerchants.getInstance().getEconomy().withdrawPlayer((OfflinePlayer) sender, createPrice);
            }

            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, sender.getName() + "'s Shop");
            npc.addTrait(Owner.class);
            npc.getTrait(Owner.class).setOwner(sender);
            npc.addTrait(TraitPlayerShop.class);
            npc.spawn(player.getLocation().getBlock().getLocation().add(0.5D, 0D, 0.5D));

            if(!npc.isSpawned())
            {
                sender.sendMessage(ChatColor.RED + "Error creating shop: Could not spawn NPC.");
                return true;
            }

            player.sendMessage(ChatColor.GREEN + "Successfully created shop! " + ChatColor.GOLD + "$" + createPrice + ChatColor.GREEN + " was taken from your account.");
            player.getWorld().playSound(npc.getEntity().getLocation(), Sound.ENTITY_CHICKEN_EGG, SoundCategory.MASTER, 1f, 2f);
        }

        else if(args[0].equalsIgnoreCase("delete"))
        {
            if(!SelectionManager.getInstance().selectedNPCs.containsKey(((Player) sender).getUniqueId()))
            {
                sender.sendMessage(ChatColor.RED + "No shop selected.");
                return true;
            }

            NPC npc = SelectionManager.getInstance().selectedNPCs.get(player.getUniqueId());
            if(npc.isSpawned())
            {
                int extraRows = npc.getTrait(TraitPlayerShop.class).getExpansionUpgrades();

                player.getWorld().playSound(npc.getEntity().getLocation(), Sound.ENTITY_CHICKEN_EGG, SoundCategory.MASTER, 1f, 2f);
                npc.despawn();
                CitizensAPI.getNPCRegistry().deregister(npc);
                SelectionManager.getInstance().selectedNPCs.remove(player.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Successfully deleted shop.");


                double refund = SLMerchants.getInstance().getConfig().getDouble("refundamount");
                refund += extraRows * SLMerchants.getInstance().getConfig().getInt("refundamountperexpansion");
                if(refund > 0)
                {
                    SLMerchants.getInstance().getEconomy().depositPlayer((OfflinePlayer) sender, refund);
                    sender.sendMessage(ChatColor.GREEN + "You have been refunded " + ChatColor.GOLD + "$" + refund + ChatColor.GREEN + ".");
                }
            }
        }

        return true;
    }
}
