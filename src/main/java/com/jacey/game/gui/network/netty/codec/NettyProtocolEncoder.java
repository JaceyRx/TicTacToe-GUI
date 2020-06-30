package com.jacey.game.gui.network.netty.codec;

import com.jacey.game.gui.msg.NetMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Description: 自定义编码器
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
public class NettyProtocolEncoder extends MessageToByteEncoder<NetMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NetMessage netMessage, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(netMessage.toBinaryMsg());
    }
}
