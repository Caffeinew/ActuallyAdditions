/*
 * This file ("ItemToolAA.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items.base;

import de.ellpeck.actuallyadditions.api.misc.IDisableableItem;
import de.ellpeck.actuallyadditions.mod.config.ConfigurationHandler;
import de.ellpeck.actuallyadditions.mod.util.ItemUtil;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import de.ellpeck.actuallyadditions.mod.util.StringUtil;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Set;

public class ItemToolAA extends ToolItem implements IDisableableItem {

    private final String name;
    private final ItemStack repairItem;
    private String repairOredict;
    private final boolean disabled;

    public ItemToolAA(float attack, float speed, ToolMaterial toolMat, String repairItem, String unlocalizedName, IRarity rarity, Set<Block> effectiveStuff) {
        this(attack, speed, toolMat, ItemStack.EMPTY, unlocalizedName, rarity, effectiveStuff);
        this.repairOredict = repairItem;
    }

    public ItemToolAA(float attack, float speed, ToolMaterial toolMat, ItemStack repairItem, String unlocalizedName, IRarity rarity, Set<Block> effectiveStuff) {
        super(attack, speed, toolMat, effectiveStuff);

        this.repairItem = repairItem;
        this.name = unlocalizedName;
        this.rarity = rarity;
        this.disabled = ConfigurationHandler.config.getBoolean("Disable: " + StringUtil.badTranslate(unlocalizedName), "Tool Control", false, "This will disable the " + StringUtil.badTranslate(unlocalizedName) + ". It will not be registered.");
        if (!this.disabled) {
            this.register();
        }
    }

    private void register() {
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemToRepair, ItemStack stack) {
        if (StackUtil.isValid(this.repairItem)) {
            return ItemUtil.areItemsEqual(this.repairItem, stack, false);
        } else if (this.repairOredict != null) {
            int[] idsStack = OreDictionary.getOreIDs(stack);
            for (int id : idsStack) {
                if (OreDictionary.getOreName(id).equals(this.repairOredict)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDisabled() {
        return this.disabled;
    }
}
