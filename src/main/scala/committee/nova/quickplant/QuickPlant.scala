package committee.nova.quickplant

import committee.nova.quickplant.proxies.CommonProxy
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.{Mod, SidedProxy}

@Mod(modid = QuickPlant.MODID, useMetadata = true, acceptedMinecraftVersions = "[1.6,)", modLanguage = "scala")
object QuickPlant {
  final val MODID = "quickplant"
  final val proxyPrefix = "committee.nova.quickplant.proxies."

  @SidedProxy(serverSide = proxyPrefix + "CommonProxy", clientSide = proxyPrefix + "ClientProxy")
  var proxy: CommonProxy = _

  @EventHandler def preInit(event: FMLPreInitializationEvent): Unit = proxy.preInit(event)

  @EventHandler def init(event: FMLInitializationEvent): Unit = proxy.init(event)
}
