/*
 * This file ("ConfigurationHandler.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.config;

import java.io.File;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ConfigurationHandler {

    public static Configuration config;

    public ConfigurationHandler(File configFile) {
        ActuallyAdditions.LOGGER.info("Grabbing Configurations...");

        MinecraftForge.EVENT_BUS.register(this);

        config = new Configuration(configFile);
        config.load();

        redefineConfigs();
    }

    public static void redefineConfigs() {
        ConfigValues.defineConfigValues(config);

        if (config.hasChanged()) {
            config.save();
        }
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(ActuallyAdditions.MODID)) {
            redefineConfigs();
        }
    }
}