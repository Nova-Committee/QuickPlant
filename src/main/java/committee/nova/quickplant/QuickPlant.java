package committee.nova.quickplant;

import net.fabricmc.api.ModInitializer;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public class QuickPlant implements ModInitializer {
	@Override
	public void onInitialize() {
		MixinBootstrap.init();
		Mixins.addConfiguration("quickplant.mixins.json");
	}
}
