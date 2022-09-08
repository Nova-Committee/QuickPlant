package committee.nova.quickplant.common.config

import committee.nova.quickplant.common.config.CommonConfig._
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.common.Configuration

object CommonConfig {
  var refreshInterval: Int = _
  var expireTime: Int = _
  var playSound: Boolean = _
  private var config: Configuration = _

  def init(e: FMLPreInitializationEvent): Unit = new CommonConfig(e)
}

class CommonConfig(event: FMLPreInitializationEvent) {
  config = new Configuration(event.getSuggestedConfigurationFile)
  config.load()
  refreshInterval = config.get(Configuration.CATEGORY_GENERAL, "refresh_interval", 100, "Refresh interval for quickplant. A smaller value will make it faster for a plant itemEntity to convert into a plant block. Default is 100 ticks(5 sec)").getInt(100)
  expireTime = config.get(Configuration.CATEGORY_GENERAL, "expireTime", 6000, "Expire time of a plant itemEntity. If smaller than the refreshInterval, the itemEntity will disappear when it refreshes for the first time.").getInt(6000)
  playSound = config.get(Configuration.CATEGORY_GENERAL, "soundEffect", true, "Should quickplant be with a sound effect?").getBoolean(true)
  config.save()
}
