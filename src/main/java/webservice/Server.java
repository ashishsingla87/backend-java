package webservice;


import endpoint.DataService;
import handler.CodecHandler;
import handler.ExceptionHandler;
import handler.RequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class Server {
    private final int port;
    private final int maxHttpContentLength;
    private final DataService dataService;
    private final PooledByteBufAllocator pooledByteBufferAllocator;
    private final boolean tcpNoDelay;
    private final Class<NioServerSocketChannel> socketChannel;
    private final NioEventLoopGroup parentGroup;
    private final NioEventLoopGroup childGroup;
    private final EventExecutorGroup eventExecutors;
    private Channel channel;

    public Server(int port, int maxHttpContentLength, DataService dataService, int ioThreads,
                  int workerThreads, boolean tcpNoDelay) {
        this.port = port;
        this.maxHttpContentLength = maxHttpContentLength;
        this.dataService = dataService;
        this.tcpNoDelay = tcpNoDelay;
        this.pooledByteBufferAllocator = new PooledByteBufAllocator();
        this.socketChannel = NioServerSocketChannel.class;
        this.parentGroup = new NioEventLoopGroup();
        this.childGroup = new NioEventLoopGroup(ioThreads);
        this.eventExecutors = new DefaultEventExecutorGroup(workerThreads);
    }

    public void start() throws InterruptedException {
        ServerBootstrap bStrap = new ServerBootstrap();
        channel = bStrap.group(parentGroup, childGroup)
                .channel(socketChannel)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .option(ChannelOption.ALLOCATOR, pooledByteBufferAllocator)
                .option(ChannelOption.TCP_NODELAY, tcpNoDelay)
                .childHandler(new Initializer(dataService, maxHttpContentLength, eventExecutors))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(port).sync().channel();
        channel.closeFuture().sync();
    }

    public void stop() throws InterruptedException {
        try {
            channel.close().sync();
        }finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }

    private static class Initializer extends ChannelInitializer<SocketChannel>{
        private final DataService dataService;
        private final int maxHttpContentLength;
        private final EventExecutorGroup eventExecutors;

        Initializer(DataService dataService, int maxHttpContentLength, EventExecutorGroup eventExecutors) {
            this.dataService = dataService;
            this.maxHttpContentLength = maxHttpContentLength;
            this.eventExecutors = eventExecutors;
        }

        @Override
        protected void initChannel(SocketChannel sc){
            ChannelPipeline channelPipeline = sc.pipeline();
            channelPipeline.addLast(eventExecutors, new HttpServerCodec());
            channelPipeline.addLast(eventExecutors, new HttpObjectAggregator(maxHttpContentLength));
            channelPipeline.addLast(eventExecutors, new CodecHandler());
            channelPipeline.addLast(eventExecutors, new RequestHandler(dataService));
            channelPipeline.addLast(eventExecutors, new ExceptionHandler());
        }
    }
}
