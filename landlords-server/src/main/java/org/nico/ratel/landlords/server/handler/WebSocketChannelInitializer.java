package org.nico.ratel.landlords.server.handler;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.nico.ratel.landlords.entity.ServerTransferData;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.netty.buffer.Unpooled.wrappedBuffer;

public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast(new IdleStateHandler(60 * 30, 0, 0, TimeUnit.SECONDS))

                .addLast(new HttpServerCodec())  //建立WebSocket需要Http支持
                .addLast(new HttpObjectAggregator(65536))

                .addLast(new WebSocketServerProtocolHandler("/ws", null, true))
                .addLast(new MessageToMessageDecoder<WebSocketFrame>() {

                    @Override
                    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> objs) {
                        ByteBuf buf = frame.content();
                        objs.add(buf);
                        buf.retain();
                    }
                })  //解析WebSocket帧

                .addLast(new ProtobufDecoder(ServerTransferData.ServerTransferDataProtoc.getDefaultInstance()))
                .addLast(new ProtobufEncoder() {
                    @Override
                    protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) {
                        if (msg instanceof MessageLite) {
                            out.add(new BinaryWebSocketFrame(wrappedBuffer(((MessageLite) msg).toByteArray())));
                            return;
                        }
                        if (msg instanceof MessageLite.Builder) {
                            out.add(new BinaryWebSocketFrame(wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray())));
                        }
                    }
                })  //封装WebSocket帧

                .addLast(new TransferHandler());
    }
}
