package com.netiq.websockify;

import static io.netty.channel.Channels.pipeline;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPipelineFactory;
import io.netty.channel.socket.ClientSocketChannelFactory;

import com.netiq.websockify.WebsockifyServer.SSLSetting;

public class WebsockifyProxyPipelineFactory implements ChannelPipelineFactory {

    private final ClientSocketChannelFactory cf;
    private final IProxyTargetResolver resolver;
    private final SSLSetting sslSetting;
    private final String keystore;
    private final String keystorePassword;
    private final String webDirectory;

    public WebsockifyProxyPipelineFactory(ClientSocketChannelFactory cf, IProxyTargetResolver resolver, SSLSetting sslSetting, String keystore, String keystorePassword, String webDirectory) {
        this.cf = cf;
        this.resolver = resolver;
        this.sslSetting = sslSetting;
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.webDirectory = webDirectory;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = pipeline(); // Note the static import.
        
        p.addLast("unification", new PortUnificationHandler(cf, resolver, sslSetting, keystore, keystorePassword, webDirectory));
        return p;

    }

}
