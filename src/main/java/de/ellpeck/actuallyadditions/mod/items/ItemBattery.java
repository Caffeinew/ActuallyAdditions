/*
 * This file ("ItemBattery.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.items.base.ItemEnergy;
import de.ellpeck.actuallyadditions.mod.util.ItemUtil;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBattery extends ItemEnergy {

    public ItemBattery(int capacity, int transfer) {
        super(capacity, transfer);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return ItemUtil.isEnabled(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isRemote && entity instanceof PlayerEntity && ItemUtil.isEnabled(stack) && !isSelected) {
            PlayerEntity player = (PlayerEntity) entity;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack slot = player.inventory.getStackInSlot(i);
                if (StackUtil.isValid(slot) && slot.getCount() == 1) {
                    int extractable = this.extractEnergy(stack, Integer.MAX_VALUE, true);
                    int received = slot.getCapability(CapabilityEnergy.ENERGY).map(e -> e.receiveEnergy(extractable, false)).orElse(0);

                    if (received > 0) {
                        this.extractEnergy(stack, received, false);
                    }
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity player, Hand hand) {
        if (!worldIn.isRemote && player.isSneaking()) {
            ItemUtil.changeEnabled(player, hand);
            return ActionResult.resultSuccess(player.getHeldItem(hand));
        }
        return super.onItemRightClick(worldIn, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<ITextComponent> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add(new TranslationTextComponent("tooltip." + ActuallyAdditions.MODID + ".battery." + (ItemUtil.isEnabled(stack)
            ? "discharge"
            : "noDischarge")));
        list.add(new TranslationTextComponent("tooltip." + ActuallyAdditions.MODID + ".battery.changeMode"));
    }
}
