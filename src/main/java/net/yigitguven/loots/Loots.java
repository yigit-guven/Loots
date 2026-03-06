package net.yigitguven.loots;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Loots.MODID)
public class Loots {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "loots";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under
    // the "loots" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under
    // the "loots" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be
    // registered under the "loots" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MODID);
    // Create a Deferred Register for MenuTypes
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES = DeferredRegister
            .create(Registries.MENU, MODID);

    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<LootBundleMenu>> LOOT_BUNDLE_MENU = MENU_TYPES
            .register("loot_bundle",
                    () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(LootBundleMenu::new));

    // Creates a new Block with the id "loots:example_block", combining the
    // namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "loots:example_block", combining the
    // namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block",
            EXAMPLE_BLOCK);

    // Creates a new food item with the id "loots:example_id", nutrition 1 and
    // saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item",
            new Item.Properties().food(new FoodProperties.Builder()
                    .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    public static final DeferredItem<LootBundleItem> COMMON_LOOT_BUNDLE = ITEMS.register("common_loot_bundle",
            () -> new LootBundleItem(new Item.Properties(), LootRarity.COMMON));
    public static final DeferredItem<LootBundleItem> RARE_LOOT_BUNDLE = ITEMS.register("rare_loot_bundle",
            () -> new LootBundleItem(new Item.Properties(), LootRarity.RARE));
    public static final DeferredItem<LootBundleItem> EPIC_LOOT_BUNDLE = ITEMS.register("epic_loot_bundle",
            () -> new LootBundleItem(new Item.Properties(), LootRarity.EPIC));
    public static final DeferredItem<LootBundleItem> LEGENDARY_LOOT_BUNDLE = ITEMS.register("legendary_loot_bundle",
            () -> new LootBundleItem(new Item.Properties(), LootRarity.LEGENDARY));

    // Creates a creative tab with the id "loots:example_tab" for the example item,
    // that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS
            .register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.loots")) // The language key for the title of your
                                                                      // CreativeModeTab
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> COMMON_LOOT_BUNDLE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this
                                                           // method is preferred over the event
                        output.accept(COMMON_LOOT_BUNDLE.get());
                        output.accept(RARE_LOOT_BUNDLE.get());
                        output.accept(EPIC_LOOT_BUNDLE.get());
                        output.accept(LEGENDARY_LOOT_BUNDLE.get());
                    }).build());

    // The constructor for the mod class is the first code that is run when your mod
    // is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and
    // pass them in automatically.
    public Loots(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register MenuTypes
        MENU_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Loots) to
        // respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in
        // this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config
        // file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof Player player)) {
            return;
        }

        Level level = event.getEntity().level();
        if (level.isClientSide) {
            return;
        }

        // Only drop for Monsters (hostile mobs)
        if (!(event.getEntity() instanceof net.minecraft.world.entity.monster.Monster monster)) {
            return;
        }

        float maxHealth = monster.getMaxHealth();
        float roll = level.random.nextFloat();

        LootRarity selectedRarity = null;

        // Calculate probabilities based on Max Health
        if (maxHealth >= 300) {
            // High Tier (Bosses e.g. Warden/Wither)
            selectedRarity = roll < 0.95f ? LootRarity.LEGENDARY : LootRarity.EPIC;
        } else if (maxHealth >= 100) {
            // Mid-High Tier (Elder Guardian, Ravager etc)
            if (roll < 0.20f)
                selectedRarity = LootRarity.LEGENDARY;
            else if (roll < 0.50f)
                selectedRarity = LootRarity.EPIC;
            else if (roll < 0.85f)
                selectedRarity = LootRarity.RARE;
            else
                selectedRarity = LootRarity.COMMON;
        } else if (maxHealth >= 50) {
            // Mid Tier
            if (roll < 0.10f)
                selectedRarity = LootRarity.EPIC;
            else if (roll < 0.30f)
                selectedRarity = LootRarity.RARE;
            else
                selectedRarity = LootRarity.COMMON; // 70% chance for common here, roughly
        } else {
            // Low Tier (Zombie, Skeleton etc)
            if (roll < 0.01f)
                selectedRarity = LootRarity.EPIC;
            else if (roll < 0.05f)
                selectedRarity = LootRarity.RARE;
            else if (roll < 0.25f)
                selectedRarity = LootRarity.COMMON; // Increased from 10% to 20% total for common
        }

        if (selectedRarity == null)
            return;

        DeferredItem<LootBundleItem> bundleItem = switch (selectedRarity) {
            case COMMON -> COMMON_LOOT_BUNDLE;
            case RARE -> RARE_LOOT_BUNDLE;
            case EPIC -> EPIC_LOOT_BUNDLE;
            case LEGENDARY -> LEGENDARY_LOOT_BUNDLE;
        };

        ItemStack bundleStack = new ItemStack(bundleItem.get());
        ServerLevel serverLevel = (ServerLevel) level;

        List<ItemStack> loot = generateLoot(serverLevel, selectedRarity, event.getEntity(), event.getSource(), player);
        if (!loot.isEmpty()) {
            bundleStack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(loot));
            event.getDrops().add(new ItemEntity(level,
                    event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), bundleStack));
        }
    }

    public static List<ItemStack> generateLoot(ServerLevel level, LootRarity rarity,
            net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.damagesource.DamageSource source,
            Player player) {
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(
                ResourceKey.create(Registries.LOOT_TABLE, rarity.getLootTable()));

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
                .create(LootContextParamSets.ENTITY);

        return lootTable.getRandomItems(params);
    }
}
