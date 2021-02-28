/*
 * This file ("BlockTreasureChest.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.recipe.TreasureChestLoot;
import de.ellpeck.actuallyadditions.mod.blocks.base.BlockBase;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.relauncher.OnlyIn;

import java.util.Random;

public class BlockTreasureChest extends BlockBase {

    public BlockTreasureChest() {
        super(Material.WOOD, name);
        this.setHarvestLevel("axe", 0);
        this.setHardness(300.0F);
        this.setResistance(50.0F);
        this.setSoundType(SoundType.WOOD);
        this.setTickRandomly(true);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random rand) {
        for (int i = 0; i < 2; i++) {
            for (float f = 0; f <= 3; f += 0.5) {
                float particleX = rand.nextFloat();
                float particleZ = rand.nextFloat();
                world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, (double) pos.getX() + particleX, (double) pos.getY() + f + 1, (double) pos.getZ() + particleZ, 0.0D, 0.2D, 0.0D);
            }
        }
    }

    @Override
    public Item getItemDropped(BlockState state, Random rand, int par3) {
        return Items.AIR;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!world.isRemote) {
            world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.2F, world.rand.nextFloat() * 0.1F + 0.9F);
            this.dropItems(world, pos);
            world.setBlockState(pos, Blocks.AIR.getDefaultState());

            //TheAchievements.OPEN_TREASURE_CHEST.get(player);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, EntityLivingBase player, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(BlockHorizontal.FACING, player.getHorizontalFacing().getOpposite()), 2);

        super.onBlockPlacedBy(world, pos, state, player, stack);
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        return false;
    }

    private void dropItems(World world, BlockPos pos) {
        for (int i = 0; i < MathHelper.getInt(world.rand, 3, 6); i++) {
            TreasureChestLoot theReturn = WeightedRandom.getRandomItem(world.rand, ActuallyAdditionsAPI.TREASURE_CHEST_LOOT);
            ItemStack itemStack = theReturn.returnItem.copy();
            itemStack.setCount(MathHelper.getInt(world.rand, theReturn.minAmount, theReturn.maxAmount));

            float dX = world.rand.nextFloat() * 0.8F + 0.1F;
            float dY = world.rand.nextFloat() * 0.8F + 0.1F;
            float dZ = world.rand.nextFloat() * 0.8F + 0.1F;
            EntityItem entityItem = new EntityItem(world, pos.getX() + dX, pos.getY() + dY, pos.getZ() + dZ, itemStack);
            float factor = 0.05F;
            entityItem.motionX = world.rand.nextGaussian() * factor;
            entityItem.motionY = world.rand.nextGaussian() * factor + 0.2F;
            entityItem.motionZ = world.rand.nextGaussian() * factor;
            world.spawnEntity(entityItem);
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, Direction.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockHorizontal.FACING);
    }

    @Override
    public BlockState withRotation(BlockState state, Rotation rot) {
        return state.withProperty(BlockHorizontal.FACING, rot.rotate(state.getValue(BlockHorizontal.FACING)));
    }

    @Override
    public BlockState withMirror(BlockState state, Mirror mirror) {
        return this.withRotation(state, mirror.toRotation(state.getValue(BlockHorizontal.FACING)));
    }
}
