package committee.nova.quickplant.mixin;

import net.minecraft.block.FluidFillable;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
    private int itemAge;

    public MixinItemEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getStack();

    @Shadow
    public abstract void setStack(ItemStack stack);

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo c) {
        final var stack = this.getStack();
        if (!isPlant(stack)) return;
        if (this.itemAge == 0) return;
        if (this.itemAge % 100 != 0) return;
        if (!tryPlantThere()) return;
        final var newStack = stack.copy();
        newStack.setCount(newStack.getCount() - 1);
        if (newStack.getCount() <= 0) {
            this.discard();
            c.cancel();
        }
        this.setStack(newStack);
    }

    private boolean isPlant(ItemStack stack) {
        if (stack.isEmpty()) return false;
        final var item = stack.getItem();
        if (!(item instanceof final BlockItem bi)) return false;
        return bi.getBlock() instanceof PlantBlock;
    }

    private boolean tryPlantThere() {
        final var world = this.getWorld();
        final var plantPos = new BlockPos((int) Math.floor(this.getX()), (int) Math.floor(this.getY() + 0.2), (int) Math.floor(this.getZ()));
        final var blockIn = world.getBlockState(plantPos);
        if (!blockIn.isReplaceable()) return false;
        final var item = (BlockItem) this.getStack().getItem();
        final var plant = (PlantBlock) item.getBlock();
        if (plant instanceof FluidFillable && !blockIn.getFluidState().isIn(FluidTags.WATER)) return false;
        if (blockIn.getBlock() == plant) return false;
        boolean place = false;
        try {
            place = plant.canPlaceAt(world.getBlockState(plantPos), world, plantPos);
        } catch (Exception ignored) {
        }
        if (!place) return false;
        final var success = world.setBlockState(plantPos, plant.getDefaultState(), 3);
        if (success) world.playSound(null, plantPos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, .5F, 1F);
        return success;
    }
}
