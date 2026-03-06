package net.yigitguven.loots;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LootBundleScreen extends AbstractContainerScreen<LootBundleMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(Loots.MODID,
            "textures/gui/loot_bundle.png");

    public LootBundleScreen(LootBundleMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // We'll use a generic background or a custom one if available.
        // For now, let's just draw a simple colored background box if texture is
        // missing,
        // but typically we'd use a 256x256 texture file.
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Draw a dark background for the "loot box" look
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0x88000000); // Semi-transparent black
        guiGraphics.renderOutline(x, y, imageWidth, imageHeight, 0xFFFFFFFF); // White border

        // Draw slots (represented by boxes for now)
        for (int i = 0; i < 9; i++) {
            guiGraphics.fill(x + 7 + i * 18, y + 19, x + 7 + i * 18 + 18, y + 19 + 18, 0x44FFFFFF);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
