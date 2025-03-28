package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore
import jp.ngt.rtm.rail.util.RailMap
import jp.ngt.rtm.rail.util.RailMapSwitch
import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.server.MinecraftServer
import net.minecraft.util.MathHelper
import org.webctc.WebCTCCore
import org.webctc.cache.rail.RailCacheData
import org.webctc.cache.toDataClass
import org.webctc.common.types.PosInt
import org.webctc.common.types.rail.*
import org.webctc.common.types.toPosInt
import org.webctc.router.WebCTCRouter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class RailRouter : WebCTCRouter() {
    companion object {
        val connections = CopyOnWriteArrayList<Connection>()
    }

    override fun install(application: Route): Route.() -> Unit = {
        get {
            call.respond(RailCacheData.railMapCache.values)
        }
        get("/rail") {
            val x = call.request.queryParameters["x"]?.toIntOrNull()
            val y = call.request.queryParameters["y"]?.toIntOrNull()
            val z = call.request.queryParameters["z"]?.toIntOrNull()
            var railCore: TileEntityLargeRailCore? = null
            if (x != null && y != null && z != null) {
                railCore = WebCTCCore.INSTANCE.server.entityWorld.getTileEntity(x, y, z) as? TileEntityLargeRailCore
            }
            if (railCore == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(railCore.toData())
            }
        }
        webSocket("/ws") {
            val thisConnection = Connection(this)
            try {
                connections += thisConnection
                for (frame in incoming) {
                }
            } catch (e: Exception) {
                MinecraftServer.getServer().logWarning(e.message)
            }
            connections -= thisConnection
        }
    }
}

fun TileEntityLargeRailCore.toData(): LargeRailData {
    return LargeRailData(
        this.startPoint.toPosInt(),
        this.isTrainOnRail,
        this.getRailMaps(),
        this.isConverting()
    )
}

fun TileEntityLargeRailCore.isConverting(): Boolean {
    return (this as? TileEntityLargeRailSwitchCore)?.switch?.points?.any {
        !(it.movement == 0.toFloat() && !it.rpRoot.checkRSInput(this.worldObj) ||
                it.movement == 1.toFloat() && it.rpRoot.checkRSInput(this.worldObj))
    } == true
}

fun TileEntityLargeRailCore.getRailMaps(): List<IRailMapData> {
    if (this is TileEntityLargeRailSwitchCore) {
        this.switch.onBlockChanged(worldObj)
    }
    return this.allRailMaps.map { it.toData() }
}

private val isOpenField = RailMapSwitch::class.java.getDeclaredField("isOpen").apply {
    isAccessible = true
}

fun RailMap.toData(): IRailMapData {
    return if (this is RailMapSwitch) this.toData()
    else RailMapData(
        this.startRP.toDataClass(),
        this.endRP.toDataClass(),
        this.length,
        NeighborPos(
            this.startRP.getNeighborPosData(),
            this.endRP.getNeighborPosData()
        ),
    )
}

fun RailMapSwitch.toData(): IRailMapData {
    return RailMapSwitchData(
        this.startRP.toDataClass(),
        this.endRP.toDataClass(),
        this.length,
        NeighborPos(
            this.startRP.getNeighborPosData(),
            this.endRP.getNeighborPosData()
        ),
        !isOpenField[this].toString().toBoolean()
    )
}

fun RailPosition.getNeighborPosData(): PosInt {
    return PosInt(
        MathHelper.floor_double(this.posX + RailPosition.REVISION[this.direction.toInt()][0]),
        this.blockY,
        MathHelper.floor_double(this.posZ + RailPosition.REVISION[this.direction.toInt()][1])
    )
}

class Connection(val session: WebSocketServerSession) {
    companion object {
        var lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}