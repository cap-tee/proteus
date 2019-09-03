package riscv.plugins

import riscv._

import spinal.core._
import spinal.lib._

import scala.collection.mutable

class MemoryBackbone(implicit config: Config) extends Plugin with MemoryService {
  private var externalIBus: MemBus = null
  private var externalDBus: MemBus = null
  private var internalIBus: MemBus = null
  private var internalDBus: MemBus = null
  private var internalDBusStage: Stage = null
  private var dbusFilter: Option[MemBusFilter] = None
  private val dbusObservers = mutable.ArrayBuffer[MemBusObserver]()

  override def finish(): Unit = {
    assert(internalIBus != null)
    assert(internalDBus != null)

    pipeline plug new Area {
      externalIBus = master(new MemBus(config.ibusConfig)).setName("ibus")
      externalIBus <> internalIBus

      externalDBus = master(new MemBus(config.dbusConfig)).setName("dbus")
      externalDBus <> internalDBus
      dbusFilter.foreach(_(internalDBusStage, internalDBus, externalDBus))
      dbusObservers.foreach(_(internalDBusStage, internalDBus))
    }
  }

  override def getExternalIBus: MemBus = {
    assert(externalIBus != null)
    externalIBus
  }

  override def getExternalDBus: MemBus = {
    assert(externalDBus != null)
    externalDBus
  }

  override def createInternalIBus(stage: Stage): MemBus = {
    assert(internalIBus == null)

    stage plug new Area {
      internalIBus = master(new MemBus(config.ibusConfig))
    }

    internalIBus
  }

  override def createInternalDBus(stage: Stage): MemBus = {
    assert(internalDBus == null)

    stage plug new Area {
      internalDBus = master(new MemBus(config.dbusConfig))
    }

    internalDBusStage = stage
    internalDBus
  }

  override def filterDBus(filter: MemBusFilter): Unit = {
    assert(dbusFilter.isEmpty)
    dbusFilter = Some(filter)
  }

  override def observeDBus(observer: MemBusObserver): Unit = {
    dbusObservers += observer
  }
}
