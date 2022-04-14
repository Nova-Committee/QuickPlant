package committee.nova.quickplant;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
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

    public void onItemDrop(final EntityJoinWorldEvent event) {
        if (event.isCanceled()) return;
        final Entity entity = event.getEntity();
        if (!(entity instanceof ItemEntity)) return;
        final ItemEntity itemEntity = (ItemEntity) entity;
        final ItemStack stack = itemEntity.getItem();
        final byte plantType = isPlant(stack);
        if (plantType == 0) return;
        itemEntity.lifespan = refreshInterval.get();
    }

    public void onItemExpire(final ItemExpireEvent event) {
        if (event.isCanceled()) return;
        final ItemEntity itemEntity = event.getEntityItem();
        final ItemStack stack = itemEntity.getItem();
        final byte plantType = isPlant(stack);
        if (plantType == 0) return;
        if (itemEntity.getAge() >= expireTime.get()) return;
        if (!tryPlantThere(itemEntity, plantType == 1)) {
            event.setExtraLife(refreshInterval.get());
            event.setCanceled(true);
            return;
        }
        final ItemStack newStack = stack.copy();
        newStack.setCount(newStack.getCount() - 1);
        if (newStack.getCount() <= 0) return;
        itemEntity.setItem(newStack);
        event.setExtraLife(refreshInterval.get());
        event.setCanceled(true);
    }

    private byte isPlant(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        final Item plant = stack.getItem();
        if (plant instanceof IPlantable) return 1;
        if (!(plant instanceof BlockItem)) return 0;
        final Block plantBlock = ((BlockItem) plant).getBlock();
        return (byte) ((plantBlock instanceof IPlantable) ? 2 : 0);
    }

    private boolean tryPlantThere(ItemEntity itemEntity, boolean isSeed) {
        final World world = itemEntity.level;
        final BlockPos plantPos = new BlockPos(itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ());
        final BlockPos dirtPos = plantPos.below();
        final BlockState blockIn = world.getBlockState(plantPos);
        if (!blockIn.getMaterial().isReplaceable()) return false;
        final Item item = itemEntity.getItem().getItem();
        final IPlantable plant = isSeed ? (IPlantable) item : (IPlantable) ((BlockItem) item).getBlock();
        if (plant instanceof ILiquidContainer && !blockIn.getFluidState().is(FluidTags.WATER)) return false;
        if (blockIn.getBlock() == plant.getPlant(world, plantPos).getBlock()) return false;
        final BlockState dirt = world.getBlockState(dirtPos);
        boolean place = false;
        try {
            place = dirt.canSustainPlant(world, dirtPos, Direction.UP, plant);
        } catch (Exception ignored) {
        }
        if (!place) return false;
        final boolean success = world.setBlock(plantPos, plant.getPlant(world, plantPos), 3);
        if (success && playSound.get())
            world.playSound(null, plantPos, SoundEvents.GRASS_PLACE, SoundCategory.BLOCKS, .5F, 1F);
        return success;
    }
}
