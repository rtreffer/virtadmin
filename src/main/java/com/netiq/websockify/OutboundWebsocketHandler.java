package com.netiq.websockify;

import io.netty.buffer.ChannelBuffer;
import io.netty.channel.Channel;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class OutboundWebsocketHandler extends OutboundHandler {

	OutboundWebsocketHandler(Channel inboundChannel, Object trafficLock) {
        super ( inboundChannel, trafficLock );
    }
    
	@Override
    protected Object processMessage ( ChannelBuffer buffer ) {
    	// Encode the message to base64
    	ChannelBuffer base64Msg = Base64.encode(buffer, false);
    	return new TextWebSocketFrame(base64Msg);
    }
}
