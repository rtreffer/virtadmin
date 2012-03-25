package virtadmin

import scala.collection._
import scala.collection.JavaConversions._
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.transport.compression.DelayedZlibCompression
import net.schmizz.sshj.transport.compression.ZlibCompression
import net.schmizz.sshj.transport.compression.NoneCompression
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder
import java.net.InetSocketAddress
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.File

object SSHPortForwarder {

    val connectionMap = new mutable.HashMap[InetAddress,SSHClient]
    val forwardingMap = new mutable.HashMap[Int,(InetAddress,Int,LocalPortForwarder,ServerSocket)]

    val config = {
        val config = new DefaultConfig
        config.setCompressionFactories(
            new DelayedZlibCompression.Factory,
            new ZlibCompression.Factory,
            new NoneCompression.Factory
        )
        config
    }

    def connect(addr : InetAddress) = connectionMap synchronized {
        connectionMap
            .filter(_._2.isConnected)
            .getOrElse(addr, {
                val client = new SSHClient(config)
                client.addHostKeyVerifier(new PromiscuousVerifier)
                client.connect(addr)
                client.authPublickey(System.getProperty("user.name"));
                connectionMap.put(addr, client)
                client
            })
    }

    def forward(addr : InetAddress, port : Int) : Int = {
        var resultPort = 0
        forwardingMap.synchronized {
            val portList = forwardingMap
                .filter(e => (
                    e._2._1 == addr &&
                    e._2._2 == port &&
                    e._2._4.isBound &&
                    !e._2._4.isClosed
                ))
                .map(_._1)
            if (portList.size > 0) {
                return portList.head
            }
            val client = connect(addr)
            val params = new LocalPortForwarder.Parameters(
                "0.0.0.0", 0, "127.0.0.1", port)
            val ss = new ServerSocket()
            try {
                ss.setReuseAddress(true);
                ss.bind(new InetSocketAddress("127.0.0.1", 0))
                val forwarder = client.newLocalPortForwarder(params, ss)
                resultPort = ss.getLocalPort
                forwardingMap.put(resultPort,
                    (addr,port,forwarder,ss))
                new Thread() {
                    override def run = forwarder.listen
                }.start
                resultPort
            } catch {
                case e => {
                    e.printStackTrace
                    ss.close
                }
            }
        }
        Thread.sleep(1)
        resultPort
    }

}
