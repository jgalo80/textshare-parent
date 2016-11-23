package net.jgn.textshare.server;

import io.netty.channel.Channel;

/**
 * Created by jose on 5/11/16.
 */
public interface TextShareServer {

    /**
     * Starts the server
     */
    Channel start();

    /**
     * Stops the server
     */
    void stop();

}