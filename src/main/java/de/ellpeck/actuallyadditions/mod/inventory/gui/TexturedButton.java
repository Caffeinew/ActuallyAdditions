/*
 * This file ("TexturedButton.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TexturedButton extends Button {

    public final List<String> textList = new ArrayList<>();
    private final ResourceLocation resLoc;
    public int texturePosX;
    public int texturePosY;

    public TexturedButton(ResourceLocation resLoc, int x, int y, int texturePosX, int texturePosY, int width, int height, IPressable pressable) {
        this(resLoc, x, y, texturePosX, texturePosY, width, height, new ArrayList<>());
    }

    public TexturedButton(ResourceLocation resLoc, int x, int y, int texturePosX, int texturePosY, int width, int height, List<String> hoverTextList, IPressable pressable) {
        super(x, y, width, height, StringTextComponent.EMPTY, pressable);
        this.texturePosX = texturePosX;
        this.texturePosY = texturePosY;
        this.resLoc = resLoc;
        this.textList.addAll(hoverTextList);
    }

    @Override
    public void drawButton(Minecraft minecraft, int x, int y, float f) {
        if (this.visible) {
            minecraft.getTextureManager().bindTexture(this.resLoc);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
            int k = this.getHoverState(this.hovered);
            if (k == 0) {
                k = 1;
            }

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            this.blit(matrices, this.x, this.y, this.texturePosX, this.texturePosY - this.height + k * this.height, this.width, this.height);
            this.mouseDragged(minecraft, x, y);
        }
    }

    public void drawHover(int x, int y) {
        if (this.isMouseOver()) {
            Minecraft mc = Minecraft.getInstance();
            GuiUtils.drawHoveringText(this.textList, x, y, mc.displayWidth, mc.displayHeight, -1, mc.fontRenderer);
        }
    }
}
