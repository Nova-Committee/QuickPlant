package committee.nova.quickplant.common.util

import committee.nova.quickplant.common.config.CommonConfig
import net.minecraft.block.Block
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.IPlantable

import scala.util.Try

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
    val plantPos = entity.getPosition
    val dirtPos = plantPos.down()
    val blockIn = world.getBlockState(plantPos).getBlock
    if (!blockIn.getMaterial.isReplaceable) return false
    val dirt = world.getBlockState(dirtPos).getBlock
    val item = entity.getEntityItem.getItem
    val plant = if (isSeed) item.asInstanceOf[IPlantable] else Block.getBlockFromItem(item).asInstanceOf[IPlantable]
    if (blockIn == plant.getPlant(world, plantPos).getBlock) return false
    val place = Try(dirt.canSustainPlant(world, dirtPos, EnumFacing.UP, plant)).getOrElse(false)
    if (!place) return false
    val success = world.setBlockState(plantPos, plant.getPlant(world, plantPos).getBlock.getStateFromMeta(item.getMetadata(entity.getEntityItem.getItemDamage)))
    if (success && CommonConfig.playSound) world.playSoundEffect(plantPos.getX, plantPos.getY, plantPos.getZ, "dig.grass", .5F, 1F)
    success
  }
}
