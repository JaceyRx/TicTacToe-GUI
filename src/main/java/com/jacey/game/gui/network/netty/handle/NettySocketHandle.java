package com.jacey.game.gui.network.netty.handle;

import com.jacey.game.gui.manager.MessageManager;
import com.jacey.game.gui.manager.OnlineClientManager;
import com.jacey.game.gui.msg.IMessage;
import com.jacey.game.gui.msg.NetMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Description: netty主处理类
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class NettySocketHandle extends ChannelInboundHandlerAdapter {

    /** 客户端请求的心跳命令 */
    private static final ByteBuf HEARTBEAT_SEQUENCE =
            Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hb_request", CharsetUtil.UTF_8));
    /**
     * 客户端连接时调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("已连接服务器......");
        OnlineClientManager.getInstance().addSession(ctx.channel());
    }

    /**
     * 当channel失效时（比如客户端断线或者服务器主动调用ctx.close），关闭channel对应的channelActor
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("接服务器已断开......");
        OnlineClientManager.getInstance().removeSession();
    }

    /**
     * 读取的时候调用
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetMessage netMessage = (NetMessage) msg;
        // 消息分发处理
        MessageManager.getInstance().handleRequest(netMessage);
    }

    /**
     * 心跳事件处理（心跳）
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (IdleState.WRITER_IDLE.equals(event.state())) { // 如果写通道处于空闲状态就发送心跳命令
                ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
                log.info("【心跳】发送心跳包结束....");
            }
        }
    }

    /**
     * 异常断开处理
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        if (cause.getMessage().startsWith("远程主机强迫关闭了一个现有的连接") == false) {
            InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
            log.error("【链接异常断开】, ip = {}, exception = ", insocket.getAddress().getHostAddress(), cause);
        }
    }

    private void write(IMessage message, Channel channel) {
        if (channel != null && channel.isActive() && channel.isWritable()) {
            channel.writeAndFlush(message);
        }
    }
}
