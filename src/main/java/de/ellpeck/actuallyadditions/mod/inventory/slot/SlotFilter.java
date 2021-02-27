/*
 * This file ("SlotFilter.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.slot;

import de.ellpeck.actuallyadditions.mod.items.ItemFilter;
import de.ellpeck.actuallyadditions.mod.tile.FilterSettings;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotFilter extends SlotItemHandlerUnconditioned {

    public SlotFilter(ItemStackHandlerAA inv, int slot, int x, int y) {
        super(inv, slot, x, y);
    }

    public SlotFilter(FilterSettings inv, int slot, int x, int y) {
        this(inv.filterInventory, slot, x, y);
    }

    public static boolean checkFilter(Container container, int slotId, PlayerEntity player) {
        if (slotId >= 0 && slotId < container.inventorySlots.size()) {
            Slot slot = container.getSlot(slotId);
            if (slot instanceof SlotFilter) {
                ((SlotFilter) slot).slotClick(player);
                return true;
            }
        }
        return false;
    }

    public static boolean isFilter(ItemStack stack) {
        return StackUtil.isValid(stack) && stack.getItem() instanceof ItemFilter;
    }

    private void slotClick(PlayerEntity player) {
        ItemStack heldStack = player.inventory.getItemStack();
        ItemStack stackInSlot = this.getStack();

        if (StackUtil.isValid(stackInSlot) && !StackUtil.isValid(heldStack)) {
            if (isFilter(stackInSlot)) {
                player.inventory.setItemStack(stackInSlot);
            }

            this.putStack(StackUtil.getEmpty());
        } else if (StackUtil.isValid(heldStack)) {
            if (!isFilter(stackInSlot)) {
                ItemStack s = heldStack.copy();
                s.setCount(1);
                this.putStack(s);

                if (isFilter(heldStack)) {
                    heldStack.shrink(1);
                }
            }
        }
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public void putStack(ItemStack stack) {
        super.putStack(stack.copy());
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        return false;
    }
}
