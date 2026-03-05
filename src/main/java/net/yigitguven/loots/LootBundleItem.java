package net.yigitguven.loots;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;

import java.util.List;

public class LootBundleItem extends BundleItem {
    private final LootRarity rarity;

    public LootBundleItem(Properties properties, LootRarity rarity) {
        super(properties.stacksTo(1));
        this.rarity = rarity;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack bundle, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }
        ItemStack itemStack = slot.getItem();
        if (itemStack.isEmpty()) {
            boolean result = super.overrideStackedOnOther(bundle, slot, action, player);
            if (result) {
                checkEmptyAndDestroy(bundle, player);
            }
            return result;
        } else {
            return true; // Prevent putting items in
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bundle, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess slotAccess) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }
        if (!other.isEmpty()) {
            return true; // Prevent putting items in
        }
        boolean result = super.overrideOtherStackedOnMe(bundle, other, slot, action, player, slotAccess);
        if (result) {
            checkEmptyAndDestroy(bundle, player);
        }
        return result;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
            net.minecraft.world.InteractionHand hand) {
        net.minecraft.world.InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!level.isClientSide && result.getResult().consumesAction()) {
            checkEmptyAndDestroy(result.getObject(), player);
        }
        return result;
    }

    private void checkEmptyAndDestroy(ItemStack stack, Player player) {
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents != null && !contents.items().iterator().hasNext()) {
            stack.setCount(0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.loots.rarity")
                .append(": ")
                .append(Component.literal(rarity.getName().toUpperCase())
                        .withStyle(rarity.getColor())));
        tooltip.add(Component.translatable("tooltip.loots.take_only").withStyle(ChatFormatting.GRAY,
                ChatFormatting.ITALIC));
    }
}
