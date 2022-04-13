package committee.nova.quickplant.common.util

import net.minecraft.block.Block
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraftforge.common.IPlantable
import net.minecraftforge.common.util.ForgeDirection

object Utilities {
  def getPlant(stack: ItemStack): IPlantable = {
    isPlant(stack) match {
      case 0 => null
      case 1 => stack.getItem.asInstanceOf[IPlantable]
      case 2 => Block.getBlockFromItem(stack.getItem).asInstanceOf[IPlantable]
    }
  }

  def isPlant(stack: ItemStack): Byte = {
    if (stack == null) return 0
    val item = stack.getItem
    if (item.isInstanceOf[IPlantable]) return 1
    if (Block.getBlockFromItem(item).isInstanceOf[IPlantable]) 2 else 0
  }

  def tryPlantThere(entity: EntityItem, isSeed: Boolean): Boolean = {
    val world = entity.worldObj
    val x = entity.posX.intValue() - 1
    val y = Math.floor(entity.posY).intValue()
    val z = entity.posZ.intValue() - 1
    val blockIn = world.getBlock(x, y, z)
    if (!blockIn.getMaterial.isReplaceable) return false
    val dirt = world.getBlock(x, y - 1, z)
    // TODO:
    val item = entity.getEntityItem.getItem
    val plant = if (isSeed) item.asInstanceOf[IPlantable] else Block.getBlockFromItem(item).asInstanceOf[IPlantable]
    if (!dirt.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, plant)) return false
    world.setBlock(x, y, z, plant.getPlant(world, x, y, z), plant.getPlantMetadata(world, x, y, z), 2)
  }
}
