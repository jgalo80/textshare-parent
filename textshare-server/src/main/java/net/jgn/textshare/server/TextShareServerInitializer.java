package net.jgn.textshare.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import net.jgn.textshare.server.session.SessionManager;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @author jose
 */
public class TextShareServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslContext;
    private final String websocketPath;
    private final SqlSessionFactory sqlSessionFactory;
    private final SessionManager sessionManager;

    public TextShareServerInitializer(SqlSessionFactory sqlSessionFactory, SslContext sslContext,
                                      SessionManager sessionManager, String websocketPath) {
        this.sslContext = sslContext;
        this.sqlSessionFactory = sqlSessionFactory;
        this.sessionManager = sessionManager;
        this.websocketPath = websocketPath;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (sslContext != null) {
            p.addLast(sslContext.newHandler(ch.alloc()));
        }
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(262144));
        p.addLast(new ChunkedWriteHandler());
        p.addLast(new WebSocketServerCompressionHandler());
        p.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true));
        p.addLast(new WebSocketLoginHandler(sqlSessionFactory, sessionManager));
        p.addLast(new WebSocketFrameHandler(sessionManager));
    }

}
