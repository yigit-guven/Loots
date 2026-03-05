package net.yigitguven.loots;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public enum LootRarity {
    COMMON("common", ChatFormatting.WHITE, 0.7f),
    RARE("rare", ChatFormatting.BLUE, 0.2f),
    EPIC("epic", ChatFormatting.LIGHT_PURPLE, 0.08f),
    LEGENDARY("legendary", ChatFormatting.GOLD, 0.02f);

    private final String name;
    private final ChatFormatting color;
    private final float weight;
    private final ResourceLocation lootTable;

    LootRarity(String name, ChatFormatting color, float weight) {
        this.name = name;
        this.color = color;
        this.weight = weight;
        this.lootTable = ResourceLocation.fromNamespaceAndPath(Loots.MODID, "bundles/" + name);
    }

    public String getName() {
        return name;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public float getWeight() {
        return weight;
    }

    public ResourceLocation getLootTable() {
        return lootTable;
    }
}
