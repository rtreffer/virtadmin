package virtadmin.routes
import java.net.InetAddress
import virtadmin.LibvirtConnections
import scala.collection._

object Domain extends Domain

class Domain extends virtadmin.Layout {

    def action = GET("/domain/:ip/:name/:action") {
        val ip = param[String]("ip")
        val name = param[String]("name")
        val action = param[String]("action")
        val addr = InetAddress.getByName(ip)

        val domain = LibvirtConnections.domain(addr, name)

        action match {
            case "create" => domain.create
            case "shutdown" => domain.shutdown
            case "reboot" => domain.reboot(0)
            case "destroy" => domain.destroy
            case _ =>
                throw new IllegalArgumentException("Unknown action " + action)
        }

        respondJson(immutable.Map(
            "status" -> "ok"
        ))
    }

}
