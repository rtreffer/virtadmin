package virtadmin

import xitrum.handler.Server
import xitrum.routing.Routes

object Boot {
  def main(args: Array[String]) {
    LibvirtMDNS.start
    Server.start()
  }
}
