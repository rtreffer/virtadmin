/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.netiq.websockify;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import io.netty.buffer.ChannelBuffer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelStateEvent;
import io.netty.channel.ExceptionEvent;
import io.netty.channel.socket.ClientSocketChannelFactory;
import io.netty.handler.codec.frame.FrameDecoder;
import io.netty.handler.codec.http.HttpChunkAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import com.netiq.websockify.WebsockifyServer.SSLSetting;

/**
 * Manipulates the current pipeline dynamically to switch protocols or enable
 * SSL or GZIP.
 */
public class PortUnificationHandler extends FrameDecoder {
    public static final long CONNECTION_TO_FIRST_MSG_TIMEOUT = 1000;

	private final ClientSocketChannelFactory cf;
	private final IProxyTargetResolver resolver;
    private final SSLSetting sslSetting;
    private final String keystore;
    private final String keystorePassword;    
    private final String webDirectory;
    private Timer msgTimer = null;

    private PortUnificationHandler(ClientSocketChannelFactory cf, IProxyTargetResolver resolver, SSLSetting sslSetting, String keystore, String keystorePassword, String webDirectory, final ChannelHandlerContext ctx) {
    	this ( cf, resolver, sslSetting, keystore, keystorePassword, webDirectory);
    	startDirectConnectionTimer(ctx);
    }
    
    public PortUnificationHandler(ClientSocketChannelFactory cf, IProxyTargetResolver resolver, SSLSetting sslSetting, String keystore, String keystorePassword, String webDirectory) {
    	this.cf = cf;
    	this.resolver = resolver;
        this.sslSetting = sslSetting;
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.webDirectory = webDirectory;
    }
    
    // In cases where there will be a direct VNC proxy connection
    // The client won't send any message because VNC servers talk first
    // So we'll set a timer on the connection - if there's no message by the time
    // the timer fires we'll create the proxy connection to the target
    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e)
            throws Exception {
    	startDirectConnectionTimer( ctx );
    }
    
    private void startDirectConnectionTimer ( final ChannelHandlerContext ctx )
    {
    	// cancel any outstanding timer
        cancelDirectionConnectionTimer ( );
    
    	// cancelling a timer makes it unusable again, so we have to create another one
    	msgTimer = new Timer();
    	msgTimer.schedule(new TimerTask ( ) {

			@Override
			public void run() {
		        switchToDirectProxy(ctx);				
			}
    		
    	}, CONNECTION_TO_FIRST_MSG_TIMEOUT);
    	
    }
    
    private void cancelDirectionConnectionTimer ( )
    {
    	if ( msgTimer != null ) {
    		msgTimer.cancel();
    		msgTimer = null;
    	}
    	
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        // Will use the first two bytes to detect a protocol.
        if (buffer.readableBytes() < 2) {
            return null;
        }
        
        cancelDirectionConnectionTimer ( );        

        final int magic1 = buffer.getUnsignedByte(buffer.readerIndex());
        final int magic2 = buffer.getUnsignedByte(buffer.readerIndex() + 1);

        if (isSsl(magic1)) {
            enableSsl(ctx);
        } else if ( isFlashPolicy ( magic1, magic2 ) ) {
        	switchToFlashPolicy(ctx);
        } else {
            switchToWebsocketProxy(ctx);
        }

        // Forward the current read buffer as is to the new handlers.
        return buffer.readBytes(buffer.readableBytes());
    }

    private boolean isSsl(int magic1) {
        if (sslSetting != SSLSetting.OFF) {
            switch (magic1) {
            case 20: case 21: case 22: case 23: case 255:
                return true;
            default:
                return magic1 >= 128;
            }
        }
        return false;
    }
    
    private boolean isFlashPolicy(int magic1, int magic2 ) {
        return (magic1 == '<' && magic2 == 'p');
    }

    private void enableSsl(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.getPipeline();

		Logger.getLogger(PortUnificationHandler.class.getName()).fine("SSL request from " + ctx.getChannel().getRemoteAddress() + ".");

        SSLEngine engine = WebsockifySslContext.getInstance(keystore, keystorePassword).getServerContext().createSSLEngine();
        engine.setUseClientMode(false);

        p.addLast("ssl", new SslHandler(engine));
        p.addLast("unificationA", new PortUnificationHandler(cf, resolver, SSLSetting.OFF, keystore, keystorePassword, webDirectory, ctx));
        p.remove(this);
    }

    private void switchToWebsocketProxy(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.getPipeline();

		Logger.getLogger(PortUnificationHandler.class.getName()).fine("Websocket proxy request from " + ctx.getChannel().getRemoteAddress() + ".");

        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("aggregator", new HttpChunkAggregator(65536));
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast("chunkedWriter", new ChunkedWriteHandler());
        p.addLast("handler", new WebsockifyProxyHandler(cf, resolver, webDirectory));
        p.remove(this);
    }

    private void switchToFlashPolicy(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.getPipeline();

		Logger.getLogger(PortUnificationHandler.class.getName()).fine("Flash policy request from " + ctx.getChannel().getRemoteAddress() + ".");

        p.addLast("flash", new FlashPolicyHandler());

        p.remove(this);
    }

    private void switchToDirectProxy(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.getPipeline();

		Logger.getLogger(PortUnificationHandler.class.getName()).fine("Direct proxy request from " + ctx.getChannel().getRemoteAddress() + ".");
		
        p.addLast("proxy", new DirectProxyHandler( ctx.getChannel(), cf, resolver ));

        p.remove(this);
    }

    // cancel the timer if channel is closed - prevents useless stack traces
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        cancelDirectionConnectionTimer ( );
    }

    // cancel the timer if exception is caught - prevents useless stack traces
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        cancelDirectionConnectionTimer ( );
		Logger.getLogger(PortUnificationHandler.class.getName()).severe("Exception on connection to " + ctx.getChannel().getRemoteAddress() + ": " + e.getCause().getMessage() );
    }
}
