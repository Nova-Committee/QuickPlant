package committee.nova.quickplant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(QuickPlant.MODID)
public class QuickPlant {
    public static final String MODID = "quickplant";
    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final ForgeConfigSpec.IntValue refreshInterval;
    public static final ForgeConfigSpec.IntValue expireTime;
    public static final ForgeConfigSpec.BooleanValue playSound;

    static {
        final var builder = new ForgeConfigSpec.Builder();
        builder.comment("QuickPlant Configuration");
        refreshInterval = builder.comment("The time of the refresh interval. After each interval, the plant item will try to plant below.")
                .defineInRange("refreshInterval", 100, 20, 6000);
        expireTime = builder.comment("The time before the item entity's expiration. The item entity whose age is larger than this will be cleared, just like what happens in vanilla.")
                .defineInRange("expireTime", 6000, 20, 36000);
        playSound = builder.comment("If true, a sound will be played on quick-planting.")
                .define("playSound", true);
        COMMON_CONFIG = builder.build();
    }

    public QuickPlant() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
        MinecraftForge.EVENT_BUS.addListener(this::onItemDrop);
        MinecraftForge.EVENT_BUS.addListener(this::onItemExpire);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onItemDrop(final EntityJoinLevelEvent event) {
        if (event.isCanceled()) return;
        final var entity = event.getEntity();
        if (!(entity instanceof final ItemEntity itemEntity)) return;
        final var stack = itemEntity.getItem();
        final byte plantType = isPlant(stack);
        if (plantType == 0) return;
        itemEntity.lifespan = refreshInterval.get();
    }

    public void onItemExpire(final ItemExpireEvent event) {
        if (event.isCanceled()) return;
        final var itemEntity = event.getEntity();
        final var stack = itemEntity.getItem();
        final byte plantType = isPlant(stack);
        if (plantType == 0) return;
        if (itemEntity.tickCount >= expireTime.get()) return;
        if (!tryPlantThere(itemEntity, plantType == 1)) {
            event.setExtraLife(refreshInterval.get());
            event.setCanceled(true);
            return;
        }
        final var newStack = stack.copy();
        newStack.setCount(newStack.getCount() - 1);
        if (newStack.getCount() <= 0) return;
        itemEntity.setItem(newStack);
        event.setExtraLife(refreshInterval.get());
        event.setCanceled(true);
    }

    private byte isPlant(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        final var plant = stack.getItem();
        if (plant instanceof IPlantable) return 1;
        if (!(plant instanceof BlockItem)) return 0;
        final var plantBlock = ((BlockItem) plant).getBlock();
        return (byte) ((plantBlock instanceof IPlantable) ? 2 : 0);
    }

    private boolean tryPlantThere(ItemEntity itemEntity, boolean isSeed) {
        final var world = itemEntity.level();
        final var plantPos = new BlockPos((int) Math.floor(itemEntity.getX()), (int) Math.floor(itemEntity.getY() + 0.2), (int) Math.floor(itemEntity.getZ()));
        final var dirtPos = plantPos.below();
        final var blockIn = world.getBlockState(plantPos);
        if (!blockIn.canBeReplaced()) return false;
        final var item = itemEntity.getItem().getItem();
        final var plant = isSeed ? (IPlantable) item : (IPlantable) ((BlockItem) item).getBlock();
        if (plant instanceof LiquidBlockContainer && !blockIn.getFluidState().is(FluidTags.WATER)) return false;
        if (blockIn.getBlock() == plant.getPlant(world, plantPos).getBlock()) return false;
        final var dirt = world.getBlockState(dirtPos);
        boolean place = false;
        try {
            place = dirt.canSustainPlant(world, dirtPos, Direction.UP, plant);
        } catch (Exception ignored) {
        }
        if (!place) return false;
        final var pl = plant.getPlant(world, plantPos);
        final var success = world.setBlock(plantPos, pl, 3);
        if (success && playSound.get())
            world.playSound(null, plantPos, pl.getSoundType().getPlaceSound(), SoundSource.BLOCKS, .5F, 1F);
        return success;
    }
}
