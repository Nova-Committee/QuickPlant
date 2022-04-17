package committee.nova.quickplant.mixin;

import committee.nova.quickplant.api.ItemPlantMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
    @Shadow
    private int age;

    public MixinItemEntity(World world) {
        super(world);
    }

    @Shadow
    public abstract ItemStack getItemStack();

    @Shadow
    public abstract void setItemStack(ItemStack itemStack);

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo c) {
        final ItemStack stack = this.getItemStack();
        final byte plantType = isPlant(stack);
        if (plantType == 0) return;
        if (this.age == 0) return;
        if (this.age % 100 != 0) return;
        if (!tryPlantThere(plantType)) return;
        final ItemStack newStack = stack.copy();
        newStack.count--;
        if (newStack.count <= 0) {
            this.remove();
            c.cancel();
        }
        this.setItemStack(newStack);
    }

    private byte isPlant(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        final Item item = stack.getItem();
        if (item instanceof BlockItem) return (byte) ((((BlockItem) item).getBlock() instanceof PlantBlock) ? 1 : 0);
        return (byte) ((item instanceof CropItem || item instanceof SeedItem || ItemPlantMap.getPlantMap().containsKey(item)) ? 2 : 0);
    }

    private boolean tryPlantThere(byte plantType) {
        final World world = this.world;
        final BlockPos plantPos = getBlockPos();
        final BlockState blockIn = world.getBlockState(plantPos);
        if (!blockIn.getBlock().isReplaceable(world, plantPos)) return false;
        final Item item = this.getItemStack().getItem();
        final PlantBlock plant = plantType == 1 ? (PlantBlock) ((BlockItem) item).getBlock() : getPlantBlock(item);
        if (plant == null) return false;
        if (blockIn.getBlock() == plant) return false;
        boolean place = false;
        try {
            place = plant.canBePlacedAtPos(world, plantPos);
        } catch (Exception ignored) {
        }
        if (!place) return false;
        final boolean success = world.setBlockState(plantPos, plant.stateFromData(getItemStack().getMeta()), 3);
        if (success) world.playSound(plantPos.getX(), plantPos.getY(), plantPos.getZ(), "dig.grass", .5F, 1F);
        return success;
    }

    public PlantBlock getPlantBlock(Item item) {
        if (item == Items.CARROT) return (PlantBlock) Blocks.CARROTS;
        if (item == Items.POTATO) return (PlantBlock) Blocks.POTATOES;
        if (item == Items.WHEAT_SEEDS) return (PlantBlock) Blocks.WHEAT;
        if (item == Items.PUMPKIN_SEEDS) return (PlantBlock) Blocks.PUMPKIN_STEM;
        if (item == Items.MELON_SEEDS) return (PlantBlock) Blocks.MELON_STEM;
        if (item == Items.NETHER_WART) return (PlantBlock) Blocks.NETHER_WART;
        final Map<Item, PlantBlock> map = ItemPlantMap.getPlantMap();
        if (map.containsKey(item)) return map.get(item);
        return null;
    }

}
