package net.yigitguven.loots;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.core.component.DataComponents;

public class LootBundleMenu extends AbstractContainerMenu {
    private final SimpleContainer container;
    private final ItemStack bundleStack;

    // Client-side constructor
    public LootBundleMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, ItemStack.EMPTY);
    }

    // Server-side constructor
    public LootBundleMenu(int containerId, Inventory playerInventory, ItemStack bundleStack) {
        super(Loots.LOOT_BUNDLE_MENU.get(), containerId);
        this.bundleStack = bundleStack;

        // Get contents from bundle or use empty if not present (client side)
        BundleContents contents = bundleStack.get(DataComponents.BUNDLE_CONTENTS);
        int slotCount = 0;
        if (contents != null) {
            for (ItemStack s : contents.items())
                slotCount++;
        }

        // We'll use a fixed size container for simplicity in UI, or dynamic based on
        // bundle size.
        // Bundles are traditionally 64 weight, but our loot tables might have fewer
        // items.
        // Let's use 9 slots (one row) for now as a default for loot boxes.
        this.container = new SimpleContainer(Math.max(9, slotCount));

        if (contents != null) {
            int i = 0;
            for (ItemStack s : contents.items()) {
                if (i < container.getContainerSize()) {
                    container.setItem(i++, s.copy());
                }
            }
        }

        // Add listener to update bundleStack instantly when items are taken
        this.container.addListener(c -> {
            if (!playerInventory.player.level().isClientSide) {
                java.util.List<ItemStack> remaining = new java.util.ArrayList<>();
                for (int j = 0; j < container.getContainerSize(); j++) {
                    if (!container.getItem(j).isEmpty()) {
                        remaining.add(container.getItem(j));
                    }
                }
                if (remaining.isEmpty()) {
                    bundleStack.setCount(0);
                } else {
                    bundleStack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(remaining));
                }
            }
        });

        // Add bundle slots (Take-only)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(container, i, 8 + i * 18, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false; // TAKE ONLY
                }
            });
        }

        // Add player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        // Add player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 9) { // From bundle to player
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // From player to bundle (BLOCKED)
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // Simplified for loot bundles
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            // Update bundle contents or destroy if empty
            if (container.isEmpty()) {
                // Destroy the bundle item in use
                // Finding it is tricky if player moved it.
                // For now, we assume it's the one in their hand or we just clear the contents.
                if (bundleStack.getItem() instanceof LootBundleItem) {
                    bundleStack.setCount(0);
                }
            } else {
                // Ideally we update the bundle contents here if some were left
                java.util.List<ItemStack> remaining = new java.util.ArrayList<>();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (!container.getItem(i).isEmpty()) {
                        remaining.add(container.getItem(i));
                    }
                }
                if (remaining.isEmpty()) {
                    bundleStack.setCount(0);
                } else {
                    bundleStack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(remaining));
                }
            }
        }
    }
}
