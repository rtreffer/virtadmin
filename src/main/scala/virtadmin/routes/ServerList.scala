package virtadmin.routes

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import com.codahale.jerkson.Json
import virtadmin.LibvirtMDNS
import scala.collection._

object ServerList extends ServerList

class ServerList extends virtadmin.Layout {

    def formatServerList() =
        LibvirtMDNS.getAllDomains.map(
            server => immutable.Map(
                "name" -> server._2,
                "ip" -> server._1,
                "domains" -> server._3.map(domain => immutable.Map(
                    "name" -> domain._1,
                    "running" -> domain._2
                ))
            )
        )

    def services = GET("/servers") {  // Entry point
         respondJson(formatServerList())
    }

}
