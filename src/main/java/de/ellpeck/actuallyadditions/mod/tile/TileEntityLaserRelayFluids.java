/*
 * This file ("TileEntityLaserRelayFluids.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.tile;

import de.ellpeck.actuallyadditions.api.laser.IConnectionPair;
import de.ellpeck.actuallyadditions.api.laser.LaserType;
import de.ellpeck.actuallyadditions.api.laser.Network;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityLaserRelayEnergy.Mode;
import de.ellpeck.actuallyadditions.mod.util.StringUtil;
import de.ellpeck.actuallyadditions.mod.util.WorldUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.OnlyIn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TileEntityLaserRelayFluids extends TileEntityLaserRelay {

    public final ConcurrentHashMap<EnumFacing, TileEntity> handlersAround = new ConcurrentHashMap<>();
    private final IFluidHandler[] fluidHandlers = new IFluidHandler[6];
    private Mode mode = Mode.BOTH;

    public TileEntityLaserRelayFluids() {
        super("laserRelayFluids", LaserType.FLUID);

        for (int i = 0; i < this.fluidHandlers.length; i++) {
            EnumFacing facing = EnumFacing.values()[i];
            this.fluidHandlers[i] = new IFluidHandler() {
                @Override
                public IFluidTankProperties[] getTankProperties() {
                    return new IFluidTankProperties[0];
                }

                @Override
                public int fill(FluidStack resource, boolean doFill) {
                    return TileEntityLaserRelayFluids.this.transmitFluid(facing, resource, doFill);
                }

                @Override
                public FluidStack drain(FluidStack resource, boolean doDrain) {
                    return null;
                }

                @Override
                public FluidStack drain(int maxDrain, boolean doDrain) {
                    return null;
                }
            };
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!this.world.isRemote) {
            if (this.mode == Mode.INPUT_ONLY) {
                for (EnumFacing side : this.handlersAround.keySet()) {
                    WorldUtil.doFluidInteraction(this.handlersAround.get(side), this, side.getOpposite(), Integer.MAX_VALUE);
                }
            }
        }
    }

    @Override
    public boolean shouldSaveDataOnChangeOrWorldStart() {
        return true;
    }

    @Override
    public void saveDataOnChangeOrWorldStart() {
        Map<EnumFacing, TileEntity> old = new HashMap<>(this.handlersAround);
        boolean change = false;

        this.handlersAround.clear();
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos pos = this.getPos().offset(side);
            if (this.world.isBlockLoaded(pos)) {
                TileEntity tile = this.world.getTileEntity(pos);
                if (tile != null && !(tile instanceof TileEntityLaserRelay)) {
                    if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())) {
                        this.handlersAround.put(side, tile);

                        TileEntity oldTile = old.get(side);
                        if (oldTile == null || !tile.equals(oldTile)) {
                            change = true;
                        }
                    }
                }
            }
        }

        if (change || old.size() != this.handlersAround.size()) {
            Network network = this.getNetwork();
            if (network != null) {
                network.changeAmount++;
            }
        }
    }

    @Override
    public IFluidHandler getFluidHandler(EnumFacing facing) {
        return this.fluidHandlers[facing == null
            ? 0
            : facing.ordinal()];
    }

    private int transmitFluid(EnumFacing from, FluidStack stack, boolean doFill) {
        int transmitted = 0;
        if (stack != null && this.mode != Mode.OUTPUT_ONLY) {
            Network network = this.getNetwork();
            if (network != null) {
                transmitted = this.transferFluidToReceiverInNeed(from, network, stack, doFill);
            }
        }
        return transmitted;
    }

    private int transferFluidToReceiverInNeed(EnumFacing from, Network network, FluidStack stack, boolean doFill) {
        int transmitted = 0;
        //Keeps track of all the Laser Relays and Energy Acceptors that have been checked already to make nothing run multiple times
        Set<BlockPos> alreadyChecked = new HashSet<>();

        Set<TileEntityLaserRelayFluids> relaysThatWork = new HashSet<>();
        int totalReceiverAmount = 0;

        for (IConnectionPair pair : network.connections) {
            for (BlockPos relay : pair.getPositions()) {
                if (relay != null && this.world.isBlockLoaded(relay) && !alreadyChecked.contains(relay)) {
                    alreadyChecked.add(relay);
                    TileEntity relayTile = this.world.getTileEntity(relay);
                    if (relayTile instanceof TileEntityLaserRelayFluids) {
                        TileEntityLaserRelayFluids theRelay = (TileEntityLaserRelayFluids) relayTile;
                        if (theRelay.mode != Mode.INPUT_ONLY) {
                            boolean workedOnce = false;

                            for (EnumFacing facing : theRelay.handlersAround.keySet()) {
                                if (theRelay != this || facing != from) {
                                    TileEntity tile = theRelay.handlersAround.get(facing);

                                    EnumFacing opp = facing.getOpposite();
                                    if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opp)) {
                                        IFluidHandler cap = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opp);
                                        if (cap != null && cap.fill(stack, false) > 0) {
                                            totalReceiverAmount++;
                                            workedOnce = true;
                                        }
                                    }
                                }
                            }

                            if (workedOnce) {
                                relaysThatWork.add(theRelay);
                            }
                        }
                    }
                }
            }
        }

        if (totalReceiverAmount > 0 && !relaysThatWork.isEmpty()) {
            int amountPer = stack.amount / totalReceiverAmount;
            if (amountPer <= 0) {
                amountPer = stack.amount;
            }

            for (TileEntityLaserRelayFluids theRelay : relaysThatWork) {
                for (Map.Entry<EnumFacing, TileEntity> receiver : theRelay.handlersAround.entrySet()) {
                    if (receiver != null) {
                        EnumFacing side = receiver.getKey();
                        EnumFacing opp = side.getOpposite();
                        TileEntity tile = receiver.getValue();
                        if (!alreadyChecked.contains(tile.getPos())) {
                            alreadyChecked.add(tile.getPos());
                            if (theRelay != this || side != from) {
                                if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opp)) {
                                    IFluidHandler cap = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opp);
                                    if (cap != null) {
                                        FluidStack copy = stack.copy();
                                        copy.amount = amountPer;
                                        transmitted += cap.fill(copy, doFill);
                                    }
                                }

                                //If everything that could be transmitted was transmitted
                                if (transmitted >= stack.amount) {
                                    return transmitted;
                                }
                            }
                        }
                    }
                }
            }
        }

        return transmitted;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getExtraDisplayString() {
        return StringUtil.localize("info." + ActuallyAdditions.MODID + ".laserRelay.fluid.extra") + ": " + TextFormatting.DARK_RED + StringUtil.localize(this.mode.name) + TextFormatting.RESET;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getCompassDisplayString() {
        return TextFormatting.GREEN + StringUtil.localize("info." + ActuallyAdditions.MODID + ".laserRelay.energy.display");
    }

    @Override
    public void onCompassAction(PlayerEntity player) {
        this.mode = this.mode.getNext();
    }

    @Override
    public void writeSyncableNBT(CompoundNBT compound, NBTType type) {
        super.writeSyncableNBT(compound, type);

        if (type != NBTType.SAVE_BLOCK) {
            compound.setString("Mode", this.mode.toString());
        }
    }

    @Override
    public void readSyncableNBT(CompoundNBT compound, NBTType type) {
        super.readSyncableNBT(compound, type);

        if (type != NBTType.SAVE_BLOCK) {
            String modeStrg = compound.getString("Mode");
            if (modeStrg != null && !modeStrg.isEmpty()) {
                this.mode = Mode.valueOf(modeStrg);
            }
        }
    }
}
