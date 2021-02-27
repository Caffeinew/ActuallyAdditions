/*
 * This file ("LensDisruption.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items.lens;

import de.ellpeck.actuallyadditions.api.internal.IAtomicReconstructor;
import de.ellpeck.actuallyadditions.api.lens.Lens;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.config.values.ConfigIntValues;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class LensDisruption extends Lens {

    private static final int ENERGY_USE = 150000;

    @Override
    public boolean invoke(BlockState hitState, BlockPos hitBlock, IAtomicReconstructor tile) {
        if (ConfigIntValues.ELEVEN.getValue() == 11 && tile.getEnergy() >= ENERGY_USE && hitBlock != null && !hitState.getBlock().isAir(hitState, tile.getWorldObject(), hitBlock)) {
            int range = 2;
            ArrayList<EntityItem> items = (ArrayList<EntityItem>) tile.getWorldObject().getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(hitBlock.getX() - range, hitBlock.getY() - range, hitBlock.getZ() - range, hitBlock.getX() + range, hitBlock.getY() + range, hitBlock.getZ() + range));
            for (EntityItem item : items) {
                ItemStack stack = item.getItem();
                if (!item.isDead && StackUtil.isValid(stack)) {
                    if (!stack.hasTagCompound() || !stack.getTagCompound().getBoolean(ActuallyAdditions.MODID + "DisruptedAlready")) {

                        ItemStack newStack;
                        do {
                            if (tile.getWorldObject().rand.nextBoolean()) {
                                newStack = new ItemStack(Item.REGISTRY.getRandomObject(tile.getWorldObject().rand));
                            } else {
                                newStack = new ItemStack(Block.REGISTRY.getRandomObject(tile.getWorldObject().rand));
                            }
                        } while (!StackUtil.isValid(newStack));

                        newStack.setCount(stack.getCount());

                        if (!newStack.hasTagCompound()) {
                            newStack.setTagCompound(new CompoundNBT());
                        }
                        newStack.getTagCompound().putBoolean(ActuallyAdditions.MODID + "DisruptedAlready", true);

                        item.setDead();

                        EntityItem newItem = new EntityItem(tile.getWorldObject(), item.posX, item.posY, item.posZ, newStack);
                        tile.getWorldObject().spawnEntity(newItem);

                        tile.extractEnergy(ENERGY_USE);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public float[] getColor() {
        return new float[]{246F / 255F, 255F / 255F, 183F / 255F};
    }

    @Override
    public int getDistance() {
        return 3;
    }

    @Override
    public boolean canInvoke(IAtomicReconstructor tile, Direction sideToShootTo, int energyUsePerShot) {
        return tile.getEnergy() - energyUsePerShot >= ENERGY_USE;
    }
}
