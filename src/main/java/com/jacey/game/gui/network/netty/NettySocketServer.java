package com.jacey.game.gui.network.netty;

import com.jacey.game.gui.manager.ConfigManager;
import com.jacey.game.gui.network.ServerNode;
import com.jacey.game.gui.network.netty.codec.NettyProtocolDecoder;
import com.jacey.game.gui.network.netty.codec.NettyProtocolEncoder;
import com.jacey.game.gui.network.netty.handle.NettySocketHandle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Netty客户端
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class NettySocketServer implements ServerNode {

    private NettySocketServer() {}

    private static NettySocketServer instance = new NettySocketServer();

    public static NettySocketServer getInstance() {
        return instance;
    }

    // 定义线程组，处理读写和链接事件
    EventLoopGroup group = new NioEventLoopGroup();

    @Override
    public void start() throws Exception {
        String serverHost = ConfigManager.SERVER_HOST;
        int serverPort = ConfigManager.SERVER_PORT;

        try {
            // Netty 客户端启动引导
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(serverHost, serverPort))
                    .handler(new ChildChannelHandler());
            bootstrap.connect().sync();
            log.info("netty Client服务已启动，已连接服务器 >>  {}:{}", serverHost, serverPort);
        } catch (Exception e) {
            log.error("【服务器连接异常】 服务器连接地址 >> {}:{}", serverHost, serverPort);
            System.exit(0);
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new NettyProtocolDecoder()); // 自定义解码器
            pipeline.addLast(new NettyProtocolEncoder()); // 自定义编码器
            // 心跳处理：每5秒放包ping
            pipeline.addLast(new IdleStateHandler(ConfigManager.SOCKET_READER_IDLE_TIME,
                    ConfigManager.SOCKET_WRITER_IDLE_TIME,
                    ConfigManager.SOCKET_ALL_IDLE_TIME, TimeUnit.SECONDS));
            pipeline.addLast(new NettySocketHandle());   // 消息处理
        }

    }

    @Override
    public void shutdown() {

    }


}
