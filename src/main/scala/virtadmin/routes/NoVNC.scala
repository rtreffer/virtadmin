package virtadmin.routes

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import com.codahale.jerkson.Json
import virtadmin.LibvirtMDNS
import virtadmin.SSHPortForwarder
import scala.collection._
import scala.collection.JavaConversions._
import java.net.InetAddress
import virtadmin.LibvirtConnections
import scala.xml.XML
import com.netiq.websockify.StaticTargetResolver
import com.netiq.websockify.DirectProxyHandler
import java.util.concurrent.Executors
import io.netty.channel.socket.nio.NioClientSocketChannelFactory
import xitrum.handler.ChannelPipelineFactory
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory

object NoVNC extends NoVNC

class NoVNC extends virtadmin.Layout {

    val clientFactory = {
        val executor = Executors.newCachedThreadPool();
        new NioClientSocketChannelFactory(executor, executor)
    }
    def connect = WEBSOCKET("/novnc/:ip/:name") { try {

        // perform websocket handshake
        val url = webSocketScheme + "://" + serverName + ":" + serverPort + request.getUri
        val factory = new WebSocketServerHandshakerFactory(url, "base64", false)
        val handshaker = factory.newHandshaker(request)
        handshaker.handshake(channel, request)

        // get parameters 
        val ip = param[String]("ip")
        val name = param[String]("name")
        val addr = InetAddress.getByName(ip)

        // build ssh vnc port tunnel
        val details =
            LibvirtConnections.domainDetails(addr, name).map(
                XML.loadString(_)
            ).get
        val remoteVNCport = (
                details \\ "graphics" \ "@port"
            ).iterator.next.toString.toInt
        val localPort = SSHPortForwarder.forward(addr, remoteVNCport)

        // setup novnc
        val pipeline = channel.getPipeline
        ChannelPipelineFactory.removeUnusedDefaultHttpHandlersForWebSocket(pipeline)
        val handler = new DirectProxyHandler(
                channel,
                clientFactory,
                new StaticTargetResolver("127.0.0.1", localPort))
        pipeline.addLast("novnc", handler)

    } catch {
        case e => {
            e.printStackTrace
            channel.close
        }
    } }

}
