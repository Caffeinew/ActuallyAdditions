/*
 * This file ("ClientProxy.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.proxy;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.ClientRegistryHandler;
import de.ellpeck.actuallyadditions.mod.blocks.InitBlocks;
import de.ellpeck.actuallyadditions.mod.blocks.render.*;
import de.ellpeck.actuallyadditions.mod.entity.InitEntities;
import de.ellpeck.actuallyadditions.mod.entity.RenderWorm;
import de.ellpeck.actuallyadditions.mod.event.ClientEvents;
import de.ellpeck.actuallyadditions.mod.misc.special.SpecialRenderInit;
import de.ellpeck.actuallyadditions.mod.tile.*;
import de.ellpeck.actuallyadditions.mod.util.IColorProvidingBlock;
import de.ellpeck.actuallyadditions.mod.util.IColorProvidingItem;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.List;

public class ClientProxy implements IProxy {

    private static final List<Item> COLOR_PRODIVIDING_ITEMS_FOR_REGISTERING = new ArrayList<>();
    private static final List<Block> COLOR_PRODIVIDING_BLOCKS_FOR_REGISTERING = new ArrayList<>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ActuallyAdditions.LOGGER.info("PreInitializing ClientProxy...");

        MinecraftForge.EVENT_BUS.register(new ClientRegistryHandler());

        InitEntities.initClient();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        ActuallyAdditions.LOGGER.info("Initializing ClientProxy...");

        RenderWorm.fixItemStack();

        new ClientEvents();

        //ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCompost.class, new RenderCompost());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAtomicReconstructor.class, new RenderReconstructorLens());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySmileyCloud.class, new RenderSmileyCloud());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDisplayStand.class, new RenderDisplayStand());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEmpowerer.class, new RenderEmpowerer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBatteryBox.class, new RenderBatteryBox());

        TileEntitySpecialRenderer<TileEntityLaserRelay> laser = new RenderLaserRelay();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserRelayEnergy.class, laser);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserRelayEnergyAdvanced.class, laser);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserRelayEnergyExtreme.class, laser);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserRelayItem.class, laser);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserRelayItemWhitelist.class, laser);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserRelayFluids.class, laser);

        for (Item item : COLOR_PRODIVIDING_ITEMS_FOR_REGISTERING) {
            if (item instanceof IColorProvidingItem) {
                Minecraft.getInstance().getItemColors().registerItemColorHandler(((IColorProvidingItem) item).getItemColor(), item);
            }
        }

        for (Block block : COLOR_PRODIVIDING_BLOCKS_FOR_REGISTERING) {
            if (block instanceof IColorProvidingBlock) {
                Minecraft.getInstance().getBlockColors().registerBlockColorHandler(((IColorProvidingBlock) block).getBlockColor(), block);
            }
            if (block instanceof IColorProvidingItem) {
                Minecraft.getInstance().getItemColors().registerItemColorHandler(((IColorProvidingItem) block).getItemColor(), block);
            }
        }

        IBlockColor color = (state, world, pos, tint) -> {
            if (world != null && pos != null) {
                TileEntity tileentity = world.getTileEntity(pos);
                if (tileentity instanceof TileEntityCompost && ((TileEntityCompost) tileentity).getCurrentDisplay().getBlock() != state.getBlock()) {
                    BlockState iblockstate = ((TileEntityCompost) tileentity).getCurrentDisplay();
                    return Minecraft.getInstance().getBlockColors().colorMultiplier(iblockstate, world, pos, tint);
                }
            }
            return -1;
        };
        Minecraft.getInstance().getBlockColors().registerBlockColorHandler(color, InitBlocks.blockCompost);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        ActuallyAdditions.LOGGER.info("PostInitializing ClientProxy...");

        new SpecialRenderInit();
    }

    @Override
    public void addRenderRegister(ItemStack stack, ResourceLocation location, String variant) {
        ClientRegistryHandler.MODEL_LOCATIONS_FOR_REGISTERING.put(stack, new ModelResourceLocation(location, variant));
    }

    @Override
    public void addColoredItem(Item item) {
        COLOR_PRODIVIDING_ITEMS_FOR_REGISTERING.add(item);
    }

    @Override
    public void addColoredBlock(Block block) {
        COLOR_PRODIVIDING_BLOCKS_FOR_REGISTERING.add(block);
    }

    @Override
    public PlayerEntity getCurrentPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void sendBreakPacket(BlockPos pos) {
        NetHandlerPlayClient netHandlerPlayClient = Minecraft.getInstance().getConnection();
        assert netHandlerPlayClient != null;
        netHandlerPlayClient.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, Minecraft.getInstance().objectMouseOver.sideHit));
    }
}
