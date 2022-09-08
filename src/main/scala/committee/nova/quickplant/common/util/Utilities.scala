package committee.nova.quickplant.common.util

import committee.nova.quickplant.common.config.CommonConfig
import net.minecraft.block.Block
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.common.{ForgeDirection, IPlantable}

import scala.util.Try

object Utilities {
  def getPlant(stack: ItemStack): IPlantable = {
    isPlant(stack) match {
      case 0 => null
      case 1 => stack.getItem.asInstanceOf[IPlantable]
      case 2 => getBlockFromItem(stack).asInstanceOf[IPlantable]
    }
  }

  def isPlant(stack: ItemStack): Byte = {
    if (stack == null) return 0
    val item = stack.getItem
    if (item.isInstanceOf[IPlantable]) return 1
    if (getBlockFromItem(stack).isInstanceOf[IPlantable]) 2 else 0
  }

  def tryPlantThere(entity: EntityItem, isSeed: Boolean): Boolean = {
    val world = entity.worldObj
    val x = Math.floor(entity.posX).toInt
    val y = Math.floor(entity.posY).toInt
    val z = Math.floor(entity.posZ).toInt
    val blockIn = Block.blocksList(world.getBlockId(x, y, z))
    if (blockIn != null && !blockIn.blockMaterial.isReplaceable) return false
    val dirt = Block.blocksList(world.getBlockId(x, y - 1, z))
    if (dirt == null) return false
    val item = entity.getEntityItem.getItem
    val plant = if (isSeed) item.asInstanceOf[IPlantable] else getBlockFromItem(item).asInstanceOf[IPlantable]
    if (blockIn != null && blockIn.blockID == plant.getPlantID(world, x, y, z)) return false
    val place = Try(dirt.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, plant)).getOrElse(false)
    if (!place) return false
    val success = world.setBlock(x, y, z, plant.getPlantID(world, x, y, z), item.getMetadata(entity.getEntityItem.getItemDamage), 2)
    if (success && CommonConfig.playSound) world.playSoundEffect(x, y, z, "dig.grass", .5F, 1F)
    success
  }

  private def getBlockFromItem(item: Item): Block = Try(Block.blocksList(item.itemID)).getOrElse(null)

  private def getBlockFromItem(stack: ItemStack): Block = getBlockFromItem(stack.getItem)
}
