package committee.nova.quickplant.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
    @Shadow
    private int age;

    public MixinItemEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getStack();

    @Shadow
    public abstract void setStack(ItemStack stack);

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo c) {
        final ItemStack stack = this.getStack();
        if (!isPlant(stack)) return;
        if (this.age == 0) return;
        if (this.age % 100 != 0) return;
        if (!tryPlantThere()) return;
        final ItemStack newStack = stack.copy();
        newStack.setCount(newStack.getCount() - 1);
        if (newStack.getCount() <= 0) {
            this.remove();
            c.cancel();
        }
        this.setStack(newStack);
    }

    private boolean isPlant(ItemStack stack) {
        if (stack.isEmpty()) return false;
        final Item item = stack.getItem();
        if (!(item instanceof BlockItem)) return false;
        return ((BlockItem) item).getBlock() instanceof PlantBlock;
    }

    private boolean tryPlantThere() {
        final World world = this.world;
        final BlockPos plantPos = new BlockPos(this.x, this.y + 0.2, this.z);
        final BlockState blockIn = world.getBlockState(plantPos);
        if (!blockIn.getMaterial().isReplaceable()) return false;
        final BlockItem item = (BlockItem) this.getStack().getItem();
        final PlantBlock plant = (PlantBlock) item.getBlock();
        if (plant instanceof FluidFillable && !blockIn.getFluidState().matches(FluidTags.WATER)) return false;
        if (blockIn.getBlock() == plant) return false;
        boolean place = false;
        try {
            place = plant.canPlaceAt(world.getBlockState(plantPos), world, plantPos);
        } catch (Exception ignored) {
        }
        if (!place) return false;
        final boolean success = world.setBlockState(plantPos, plant.getDefaultState(), 3);
        if (success) world.playSound(null, plantPos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, .5F, 1F);
        return success;
    }
}
