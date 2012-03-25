package com.netiq.websockify;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;

public class StaticTargetResolver implements IProxyTargetResolver {
	
	private InetSocketAddress targetAddress; 
	
	public StaticTargetResolver ( String targetHost, int targetPort )
	{
		targetAddress = new InetSocketAddress ( targetHost, targetPort );
	}

	@Override
	public InetSocketAddress resolveTarget(Channel channel) {
		return targetAddress;
	}

}
