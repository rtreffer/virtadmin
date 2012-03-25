package virtadmin

import scala.collection._
import java.net.InetAddress
import org.libvirt.Connect

object LibvirtConnections {

    val connectionMap = new mutable.HashMap[InetAddress,Connect]

    def domainDetails(addr : InetAddress, name : String) = {
        connectionMap.get(addr).map(connection => {
            val domain = connection.domainLookupByName(name)
            domain.getXMLDesc(0);
        }).filter(e => e != null && e.trim.length > 0)
    }

    def domain(addr : InetAddress, name : String) = {
        val runningDomains = connectionMap.get(addr).map(connection => {
            val domain = connection.domainLookupByName(name)
            domain
        }).filter(_ != null)
        if (runningDomains.size > 0) {
            runningDomains.get
        } else {
            null
        }
    }

    def connect(addr : InetAddress) = connectionMap synchronized {
        connectionMap.get(addr).foreach(
            con => if (!con.isConnected) {
                try { con.close }
                connectionMap.remove(addr)
            }
        )
        if (!connectionMap.contains(addr)) {
            System.out.println("Trying to connect ot " + addr)
            try { // this may fail
                val con = new Connect("qemu+ssh://" + addr.getHostAddress() + "/system?no_tty=1")
                System.out.println(con.listDefinedDomains.mkString(","))
                if (con.isConnected) {
                    connectionMap.put(
                        addr,
                        con
                    )
                }
            } catch {
                case e =>
                    System.out.println("Connection failure caused by " + e)
                    e.printStackTrace(System.out)
            }
        }
        connectionMap.get(addr)
    }

    def getAllDomains = 
        connectionMap.filter(_._2.isConnected).map(
            entry => {
                val addr = entry._1
                val con = entry._2
                (
                    addr,
                    con.listDefinedDomains.map(name => {
                        val domain = con.domainLookupByName(name)
                        System.out.println(domain.getXMLDesc(0))
                        (name, domain.isActive == 1)
                    }) ++
                    con.listDomains.map(id => {
                        val domain = con.domainLookupByID(id)
                        System.out.println(domain.getXMLDesc(0))
                        (domain.getName, domain.isActive == 1)
                    })
                )
            }
        )

}