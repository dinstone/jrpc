
package com.dinstone.jrpc.transport.netty5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.invoker.SkelectonServiceInvoker;
import com.dinstone.jrpc.protocol.Heartbeat;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.transport.AbstractAcceptance;
import com.dinstone.jrpc.transport.Acceptance;
import com.dinstone.jrpc.transport.TransportConfig;

public class NettyAcceptance extends AbstractAcceptance {

    private static final Logger LOG = LoggerFactory.getLogger(NettyAcceptance.class);

    private TransportConfig transportConfig;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    public NettyAcceptance(TransportConfig transportConfig, ImplementBinding implementBinding) {
        super(implementBinding, new SkelectonServiceInvoker());
        this.transportConfig = transportConfig;
    }

    @Override
    public Acceptance bind() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(transportConfig.getParallelCount());

        ServerBootstrap boot = new ServerBootstrap();
        boot.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    TransportProtocolDecoder rpcProtocolDecoder = new TransportProtocolDecoder();
                    rpcProtocolDecoder.setMaxObjectSize(transportConfig.getMaxSize());
                    TransportProtocolEncoder rpcProtocolEncoder = new TransportProtocolEncoder();
                    rpcProtocolEncoder.setMaxObjectSize(transportConfig.getMaxSize());
                    NettyServerHandler nettyServerHandler = new NettyServerHandler();
                    ch.pipeline().addLast(rpcProtocolDecoder);
                    ch.pipeline().addLast(rpcProtocolEncoder);
                    ch.pipeline().addLast(nettyServerHandler);
                }
            });
        boot.option(ChannelOption.SO_BACKLOG, 128);
        boot.childOption(ChannelOption.SO_KEEPALIVE, true);

        InetSocketAddress serviceAddress = implementBinding.getServiceAddress();
        try {
            boot.bind(serviceAddress).sync();
        } catch (Exception e) {
            throw new RuntimeException("can't bind service on " + serviceAddress, e);
        }
        LOG.info("JRPC acceptance bind on {}", serviceAddress);

        return this;
    }

    @Override
    public void destroy() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }

    public class NettyServerHandler extends ChannelHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object message) {
            if (message instanceof Request) {
                Response response = handle((Request) message);
                ctx.writeAndFlush(response);
            } else if (message instanceof Heartbeat) {
                ((Heartbeat) message).getContent().increase();
                ctx.writeAndFlush(message);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("untreated exception", cause);
            ctx.close();
        }

    }

}
