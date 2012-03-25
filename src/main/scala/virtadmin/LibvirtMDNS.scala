package virtadmin

import java.net.InetAddress
import javax.jmdns._
import scala.collection._

object LibvirtMDNS extends ServiceListener with Runnable {

    val jmdns = new JmDNS
    var scan = true
    val serviceMap = new mutable.HashMap[InetAddress,(String,String,Long)]

    override def serviceAdded(event : ServiceEvent) = {
        if (event.getType() == "_libvirt._tcp.local.") {
            event.getDNS().requestServiceInfo(event.getType(), event.getName(), 0)
        }
    }

    override def serviceRemoved(event : ServiceEvent) = {}

    override def serviceResolved(event : ServiceEvent) = {
        System.out.println("IP:" + event.getInfo.getInetAddress)
        System.out.println("Port:" + event.getInfo.getPort)
        System.out.println("Name:" + event.getInfo.getName)
        serviceMap.synchronized {
            val old = serviceMap.put(
                (event.getInfo.getInetAddress),
                (event.getInfo.getName,event.getName,System.currentTimeMillis)
            )
            if (old.isEmpty) {
                LibvirtConnections.connect(event.getInfo.getInetAddress)
            }
        }
    }

    override def run = {
        jmdns.addServiceListener("_libvirt._tcp.local.", this)
        var j = 0;
        while (scan) {
            serviceMap.synchronized {
                val limit = System.currentTimeMillis - 60000
                serviceMap.filter(_._2._3 <  limit)
                          .foreach(x => {
                              System.out.println("Removing " + x._2._1)
                              serviceMap.remove(x._1)
                          })
                val refreshLimit = System.currentTimeMillis - 30000
                serviceMap.filter(_._2._3 <  refreshLimit)
                          .foreach(x => {
                              jmdns.requestServiceInfo("_libvirt._tcp.local.", x._2._2, 0)
                          })
            }
            var i = 0
            while (scan && i < 10) {
                Thread.sleep(1000)
                i += 1
                if (j % 60 == 0) {
                    jmdns.requestServiceInfo("_libvirt._tcp.local.", "*", 0)
                }
                j += 1
            }
        }
    }

    def getServices = serviceMap.synchronized {
        serviceMap.map(x => (x._2._1, x._1))
                  .toList.sortWith(
                      (a,b) =>
                          (a._1 < b._1) ||
                          ((a._1 == b._1) && (a._2.toString < b._2.toString))
                  )
    }

    def getAllDomains =
        LibvirtConnections.getAllDomains.map(entry => {
            (entry._1, serviceMap.get(entry._1), entry._2)
        }).filter(!_._2.isEmpty).map(e => (e._1, e._2.get._1, e._3))

    def start = {
        System.out.println("LibvirtMDNS started")
    }

    new Thread(this).start
    Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run { scan = false }
    })

}
