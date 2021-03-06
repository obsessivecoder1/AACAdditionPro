package de.photon.aacadditionpro.modules.checks.inventory;

import de.photon.aacadditionpro.modules.ModuleType;
import de.photon.aacadditionpro.modules.PatternModule;
import de.photon.aacadditionpro.user.TimestampKey;
import de.photon.aacadditionpro.user.User;
import de.photon.aacadditionpro.util.files.configs.LoadFromConfiguration;
import de.photon.aacadditionpro.util.inventory.InventoryUtils;
import de.photon.aacadditionpro.util.server.ServerUtil;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class PerfectExitPattern extends PatternModule.Pattern<User, InventoryCloseEvent>
{
    @LoadFromConfiguration(configPath = ".violation_threshold")
    private int violationThreshold;

    @LoadFromConfiguration(configPath = ".min_tps")
    private double minTps;

    @Override
    protected int process(User user, InventoryCloseEvent event)
    {
        // Creative-clear might trigger this.
        if (user.inAdventureOrSurvivalMode() &&
            // Minimum TPS before the check is activated as of a huge amount of fps
            ServerUtil.getTPS() > minTps &&
            // Inventory is empty
            InventoryUtils.isInventoryEmpty(event.getInventory()))
        {
            final long passedTime = user.getTimestampMap().passedTime(TimestampKey.LAST_INVENTORY_CLICK_ON_ITEM);
            if (passedTime <= 70) {
                if (++user.getInventoryData().perfectExitFails >= this.violationThreshold) {
                    this.message = "Inventory-Verbose | Player: " + user.getPlayer().getName() + " exits inventories in a bot-like way (D: " + passedTime + ')';
                    return passedTime <= 50 ? 15 : 7;
                }
            } else if (user.getInventoryData().perfectExitFails > 0) {
                user.getInventoryData().perfectExitFails--;
            }
        }
        return 0;
    }

    @Override
    public String getConfigString()
    {
        return this.getModuleType().getConfigString() + ".parts.PerfectExit";
    }

    @Override
    public ModuleType getModuleType()
    {
        return ModuleType.INVENTORY;
    }
}
