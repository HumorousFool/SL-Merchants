package humorousfool.slmerchants;

import humorousfool.slmerchants.api.TraitPlayerShop;
import humorousfool.slmerchants.shops.InventoryManager;
import humorousfool.slmerchants.shops.SelectionManager;
import humorousfool.slmerchants.shops.ShopManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class SLMerchants extends JavaPlugin
{
    private static SLMerchants instance;
    public static SLMerchants getInstance()
    {
        return instance;
    }

    private Economy economy;

    public void onEnable()
    {
        instance = this;

        if(!setupEconomy())
        {
            this.getLogger().severe("SL-Merchants disabled! No Vault plugin found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if(getServer().getPluginManager().getPlugin("Citizens") == null)
        {
            this.getLogger().severe("SL-Merchants disabled! No Citizens plugin found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //Config
        getConfig().options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(SelectionManager.getInstance(), this);
        getServer().getPluginManager().registerEvents(new InventoryManager(), this);
        getCommand("slmerchants").setExecutor(new ShopManager());

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TraitPlayerShop.class).withName("playerShop"));
    }

    public void onDisable()
    {
        instance = null;
    }

    private boolean setupEconomy()
    {
        if(getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if(rsp == null)
        {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy()
    {
        return economy;
    }
}
