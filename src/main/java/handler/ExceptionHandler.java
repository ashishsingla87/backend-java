package handler;

import io.netty.channel.*;

public class ExceptionHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // DO NOT inline channel future listener must be called after write and flush
        ChannelFutureListener channelFutureListener =createErrorListener();
        ctx.writeAndFlush(msg).addListeners(channelFutureListener);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(ctx.channel().isOpen()){
            // DO NOT inline channel future listener must be called after write and flush
            ChannelFutureListener errorListener = createErrorListener();
            ctx.writeAndFlush(cause).addListener(errorListener);
        }
    }

    private ChannelFutureListener createErrorListener(){
        return future -> {
            if(future.isSuccess()){
                Channel channel = future.channel();
                if(channel.isOpen()){
                    channel.close();
                }
            }
        };
    }
}
