/*
 * This file ("TileEntityCoffeeMachine.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.tile;

import de.ellpeck.actuallyadditions.api.recipe.CoffeeIngredient;
import de.ellpeck.actuallyadditions.mod.items.InitItems;
import de.ellpeck.actuallyadditions.mod.items.ItemCoffee;
import de.ellpeck.actuallyadditions.mod.items.metalists.TheMiscItems;
import de.ellpeck.actuallyadditions.mod.misc.SoundHandler;
import de.ellpeck.actuallyadditions.mod.network.gui.IButtonReactor;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA.IAcceptor;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA.IRemover;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import de.ellpeck.actuallyadditions.mod.util.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.OnlyIn;
import net.minecraftforge.oredict.OreIngredient;

public class TileEntityCoffeeMachine extends TileEntityInventoryBase implements IButtonReactor, ISharingFluidHandler {

    public static final int SLOT_COFFEE_BEANS = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int CACHE_USE = 35;
    public static final int ENERGY_USED = 150;
    public static final int WATER_USE = 500;
    public static final int COFFEE_CACHE_MAX_AMOUNT = 300;
    public static final int TIME_USED = 500;
    public static final OreIngredient COFFEE = new OreIngredient("cropCoffee");
    public final CustomEnergyStorage storage = new CustomEnergyStorage(300000, 250, 0);
    public final FluidTank tank = new FluidTank(4 * Util.BUCKET) {
        @Override
        public boolean canDrain() {
            return false;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid.getFluid() == FluidRegistry.WATER;
        }
    };
    public int coffeeCacheAmount;
    public int brewTime;
    private int lastEnergy;
    private int lastTank;
    private int lastCoffeeAmount;
    private int lastBrewTime;

    public TileEntityCoffeeMachine() {
        super(11, "coffeeMachine");
    }

    @OnlyIn(Dist.CLIENT)
    public int getCoffeeScaled(int i) {
        return this.coffeeCacheAmount * i / COFFEE_CACHE_MAX_AMOUNT;
    }

    @OnlyIn(Dist.CLIENT)
    public int getWaterScaled(int i) {
        return this.tank.getFluidAmount() * i / this.tank.getCapacity();
    }

    @OnlyIn(Dist.CLIENT)
    public int getEnergyScaled(int i) {
        return this.storage.getEnergyStored() * i / this.storage.getMaxEnergyStored();
    }

    @OnlyIn(Dist.CLIENT)
    public int getBrewScaled(int i) {
        return this.brewTime * i / TIME_USED;
    }

    @Override
    public void writeSyncableNBT(CompoundNBT compound, NBTType type) {
        super.writeSyncableNBT(compound, type);
        this.storage.writeToNBT(compound);
        this.tank.writeToNBT(compound);
        compound.setInteger("Cache", this.coffeeCacheAmount);
        if (type != NBTType.SAVE_BLOCK) {
            compound.setInteger("Time", this.brewTime);
        }
    }

    @Override
    public void readSyncableNBT(CompoundNBT compound, NBTType type) {
        super.readSyncableNBT(compound, type);
        this.storage.readFromNBT(compound);
        this.tank.readFromNBT(compound);
        this.coffeeCacheAmount = compound.getInteger("Cache");
        if (type != NBTType.SAVE_BLOCK) {
            this.brewTime = compound.getInteger("Time");
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (!this.world.isRemote) {
            this.storeCoffee();

            if (this.brewTime > 0 || this.isRedstonePowered) {
                this.brew();
            }

            if ((this.coffeeCacheAmount != this.lastCoffeeAmount || this.storage.getEnergyStored() != this.lastEnergy || this.tank.getFluidAmount() != this.lastTank || this.brewTime != this.lastBrewTime) && this.sendUpdateWithInterval()) {
                this.lastCoffeeAmount = this.coffeeCacheAmount;
                this.lastEnergy = this.storage.getEnergyStored();
                this.lastTank = this.tank.getFluidAmount();
                this.lastBrewTime = this.brewTime;
            }
        }
    }

    @Override
    public IAcceptor getAcceptor() {
        return (slot, stack, automation) -> !automation || slot >= 3 && ItemCoffee.getIngredientFromStack(stack) != null || slot == SLOT_COFFEE_BEANS && COFFEE.apply(stack) || slot == SLOT_INPUT && stack.getItem() == InitItems.itemMisc && stack.getItemDamage() == TheMiscItems.CUP.ordinal();
    }

    @Override
    public IRemover getRemover() {
        return (slot, automation) -> !automation || slot == SLOT_OUTPUT || slot >= 3 && slot < this.inv.getSlots() && ItemCoffee.getIngredientFromStack(this.inv.getStackInSlot(slot)) == null;
    }

    public void storeCoffee() {
        if (StackUtil.isValid(this.inv.getStackInSlot(SLOT_COFFEE_BEANS)) && COFFEE.apply(this.inv.getStackInSlot(SLOT_COFFEE_BEANS))) {
            int toAdd = 2;
            if (toAdd <= COFFEE_CACHE_MAX_AMOUNT - this.coffeeCacheAmount) {
                this.inv.setStackInSlot(SLOT_COFFEE_BEANS, StackUtil.shrink(this.inv.getStackInSlot(SLOT_COFFEE_BEANS), 1));
                this.coffeeCacheAmount += toAdd;
            }
        }
    }

    public void brew() {
        if (!this.world.isRemote) {
            ItemStack input = this.inv.getStackInSlot(SLOT_INPUT);
            if (StackUtil.isValid(input) && input.getItem() == InitItems.itemMisc && input.getItemDamage() == TheMiscItems.CUP.ordinal() && !StackUtil.isValid(this.inv.getStackInSlot(SLOT_OUTPUT)) && this.coffeeCacheAmount >= CACHE_USE && this.tank.getFluid() != null && this.tank.getFluid().getFluid() == FluidRegistry.WATER && this.tank.getFluidAmount() >= WATER_USE) {
                if (this.storage.getEnergyStored() >= ENERGY_USED) {
                    if (this.brewTime % 30 == 0) {
                        this.world.playSound(null, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), SoundHandler.coffeeMachine, SoundCategory.BLOCKS, 0.1F, 1.0F);
                    }

                    this.brewTime++;
                    this.storage.extractEnergyInternal(ENERGY_USED, false);
                    if (this.brewTime >= TIME_USED) {
                        this.brewTime = 0;
                        ItemStack output = new ItemStack(InitItems.itemCoffee);
                        for (int i = 3; i < this.inv.getSlots(); i++) {
                            if (StackUtil.isValid(this.inv.getStackInSlot(i))) {
                                CoffeeIngredient ingredient = ItemCoffee.getIngredientFromStack(this.inv.getStackInSlot(i));
                                if (ingredient != null) {
                                    if (ingredient.effect(output)) {
                                        this.inv.setStackInSlot(i, StackUtil.shrinkForContainer(this.inv.getStackInSlot(i), 1));
                                    }
                                }
                            }
                        }
                        this.inv.setStackInSlot(SLOT_OUTPUT, output.copy());
                        this.inv.getStackInSlot(SLOT_INPUT).shrink(1);
                        this.coffeeCacheAmount -= CACHE_USE;
                        this.tank.drainInternal(WATER_USE, true);
                    }
                }
            } else {
                this.brewTime = 0;
            }
        }
    }

    @Override
    public void onButtonPressed(int buttonID, PlayerEntity player) {
        if (buttonID == 0 && this.brewTime <= 0) {
            this.brew();
        }
    }

    @Override
    public FluidTank getFluidHandler(Direction facing) {
        return this.tank;
    }

    @Override
    public int getMaxFluidAmountToSplitShare() {
        return 0;
    }

    @Override
    public boolean doesShareFluid() {
        return false;
    }

    @Override
    public Direction[] getFluidShareSides() {
        return null;
    }

    @Override
    public IEnergyStorage getEnergyStorage(Direction facing) {
        return this.storage;
    }
}
