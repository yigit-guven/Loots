package net.yigitguven.loots;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Loots.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LootsClient {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Loots.LOOT_BUNDLE_MENU.get(), LootBundleScreen::new);
    }
}
