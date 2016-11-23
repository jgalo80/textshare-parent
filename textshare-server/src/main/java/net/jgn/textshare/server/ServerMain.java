package net.jgn.textshare.server;

import io.netty.channel.Channel;
import net.jgn.textshare.server.cfg.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * ServerMain. Spring bootstraping. Starts the server.
 */
public class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(ServerConfig.class);

        UserDbInit userDbInit = context.getBean(UserDbInit.class);
        userDbInit.createDbIfNotExists();

        TextShareServer server = context.getBean(TextShareServer.class);
        Channel channel = server.start();

        // Wait until the server socket is closed.
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Server interrupted!", e);
        }
    }
}
