package committee.nova.quickplant.proxies

import committee.nova.quickplant.common.config.CommonConfig
import committee.nova.quickplant.common.event.QuickPlantEvent
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}

class CommonProxy {
  def preInit(event: FMLPreInitializationEvent): Unit = CommonConfig.init(event)

  def init(event: FMLInitializationEvent): Unit = QuickPlantEvent.init()
}
