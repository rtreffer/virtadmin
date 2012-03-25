package com.netiq.websockify;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.socket.ClientSocketChannelFactory;
import io.netty.channel.socket.nio.NioClientSocketChannelFactory;
import io.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WebsockifyServer {
	private Executor executor;
	private ServerBootstrap sb;
	private ClientSocketChannelFactory cf;
	private Channel serverChannel = null;
	
	public enum SSLSetting { OFF, ON, REQUIRED };
	
	public WebsockifyServer ( )
	{		
        // Configure the bootstrap.
        executor = Executors.newCachedThreadPool();
        sb = new ServerBootstrap(new NioServerSocketChannelFactory(executor, executor));

        // Set up the event pipeline factory.
        cf = new NioClientSocketChannelFactory(executor, executor);		
	}
	
	public void connect ( int localPort, String remoteHost, int remotePort )
	{
		connect ( localPort, remoteHost, remotePort, null );
	}
	
	public void connect ( int localPort, String remoteHost, int remotePort, String webDirectory )
	{
		connect ( localPort, remoteHost, remotePort, SSLSetting.OFF, null, null, webDirectory );
	}
	
	public void connect ( int localPort, String remoteHost, int remotePort, SSLSetting sslSetting, String keystore, String keystorePassword, String webDirectory )
	{
		connect ( localPort, new StaticTargetResolver ( remoteHost, remotePort ), sslSetting, keystore, keystorePassword, webDirectory );		
	}
	
	public void connect ( int localPort, IProxyTargetResolver resolver )
	{
		connect ( localPort, resolver, null );
	}
	
	public void connect ( int localPort, IProxyTargetResolver resolver, String webDirectory )
	{
		connect ( localPort, resolver, SSLSetting.OFF, null, null, webDirectory );
	}
	
	public void connect ( int localPort, IProxyTargetResolver resolver, SSLSetting sslSetting, String keystore, String keystorePassword, String webDirectory )
	{
		if ( serverChannel != null )
		{
			close ( );
		}

        sb.setPipelineFactory(new WebsockifyProxyPipelineFactory(cf, resolver, sslSetting, keystore, keystorePassword, webDirectory));

        // Start up the server.
        serverChannel = sb.bind(new InetSocketAddress(localPort));
		
	}
	
	public void close ( )
	{
		if ( serverChannel != null && serverChannel.isBound() )
		{
			serverChannel.close();
			serverChannel = null;
		}
	}
	
	public Channel getChannel ( )
	{
		return serverChannel;
	}

}
