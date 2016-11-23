package net.jgn.textshare.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Server implementation
 * @author jose
 */
@ManagedResource(objectName = "net.jgn.textshare:name=textshare-server")
public class TextShareServerImpl implements TextShareServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    private TextShareServerInitializer textShareServerInitializer;

    private Channel channel;
    private ServerParams serverParams;

    /**
     * Create a new server
     * @param serverParams config parameters of the server
     */
    public TextShareServerImpl(ServerParams serverParams, TextShareServerInitializer textShareServerInitializer) {
        this.serverParams = serverParams;
        this.textShareServerInitializer = textShareServerInitializer;
    }

    @Override
    @ManagedOperation
    public Channel start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(serverParams.getWorkerGroupThreadCount());
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(textShareServerInitializer);

        try {
            // Start the server.
            channel = b.bind(serverParams.getBindingHost(), serverParams.getBindingPort()).sync().channel();
            logger.info("Text Server started on {}:{}", serverParams.getBindingHost(), serverParams.getBindingPort());

            // Wait until the server socket is closed.
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Server interrupted!", e);
        }
        return channel;
    }

    @ManagedOperation()
    @Override
    public void stop() {
        if (this.channel != null) {
            this.channel.close().addListener(ChannelFutureListener.CLOSE);
        }
        logger.info("Text Server shutdown started");
        // Shut down all event loops to terminate all threads.
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        logger.info("Text Server shutdown completed");
    }
}
