/*
 * This file ("BlockEmpowerer.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks;

import de.ellpeck.actuallyadditions.mod.blocks.base.BlockContainerBase;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityEmpowerer;
import de.ellpeck.actuallyadditions.mod.util.ItemUtil;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockEmpowerer extends BlockContainerBase {

    public BlockEmpowerer() {
        super(Material.ROCK, this.name);

        this.setHarvestLevel("pickaxe", 0);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.STONE);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityEmpowerer();
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        return BlockSlabs.AABB_BOTTOM_HALF;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction par6, float par7, float par8, float par9) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            TileEntityEmpowerer empowerer = (TileEntityEmpowerer) world.getTileEntity(pos);
            if (empowerer != null) {
                ItemStack stackThere = empowerer.inv.getStackInSlot(0);
                if (StackUtil.isValid(heldItem)) {
                    if (!StackUtil.isValid(stackThere) && TileEntityEmpowerer.isPossibleInput(heldItem)) {
                        ItemStack toPut = heldItem.copy();
                        toPut.setCount(1);
                        empowerer.inv.setStackInSlot(0, toPut);
                        if (!player.capabilities.isCreativeMode) {
                            heldItem.shrink(1);
                        }
                        return true;
                    } else if (ItemUtil.canBeStacked(heldItem, stackThere)) {
                        int maxTransfer = Math.min(stackThere.getCount(), heldItem.getMaxStackSize() - heldItem.getCount());
                        if (maxTransfer > 0) {
                            player.setHeldItem(hand, StackUtil.grow(heldItem, maxTransfer));
                            ItemStack newStackThere = stackThere.copy();
                            newStackThere = StackUtil.shrink(newStackThere, maxTransfer);
                            empowerer.inv.setStackInSlot(0, newStackThere);
                            return true;
                        }
                    }
                } else {
                    if (StackUtil.isValid(stackThere)) {
                        player.setHeldItem(hand, stackThere.copy());
                        empowerer.inv.setStackInSlot(0, StackUtil.getEmpty());
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }
}
