package net.yigitguven.loots;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;

import net.minecraft.server.level.ServerLevel;
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
            // Taking items OUT of the bundle into an empty slot
            boolean result = super.overrideStackedOnOther(bundle, slot, action, player);
            if (result) {
                checkEmptyAndDestroy(bundle, player);
            }
            return result;
        } else {
            // Prevent putting items IN
            return true;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bundle, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess slotAccess) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        if (other.isEmpty()) {
            // Empty cursor, right-clicking on bundle in inventory -> takes top item out
            boolean result = super.overrideOtherStackedOnMe(bundle, other, slot, action, player, slotAccess);
            if (result) {
                checkEmptyAndDestroy(bundle, player);
            }
            return result;
        } else {
            // Cursor has item, trying to put it into bundle -> check if we should allow it
            // (we shouldn't)
            return true; // Block addition
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            BundleContents contents = itemstack.get(DataComponents.BUNDLE_CONTENTS);
            if (contents == null || !contents.items().iterator().hasNext()) {
                Loots.LOGGER.info("Pouch is empty, auto-generating loot for rarity: {}", rarity);
                // Auto-generate loot if empty
                var loot = Loots.generateLoot((net.minecraft.server.level.ServerLevel) level, rarity, player,
                        player.damageSources().generic(), player);
                Loots.LOGGER.info("Auto-generated loot size: {}", loot.size());
                if (!loot.isEmpty()) {
                    itemstack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(loot));
                }
            }

            // Open our custom Take-Only Menu
            player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new LootBundleMenu(containerId, playerInventory, itemstack),
                    Component.translatable("item.loots." + rarity.getName().toLowerCase() + "_loot_bundle")));
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    private void checkEmptyAndDestroy(ItemStack stack, Player player) {
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || !contents.items().iterator().hasNext()) {
            stack.setCount(0);
            if (!player.level().isClientSide) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F,
                        0.8F + player.level().getRandom().nextFloat() * 0.4F);
            }
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
