package net.jgn.textshare.server;

/**
 * @author jose
 */
public class ServerParams {

    public enum SslMode {
        NOSSL,
        CERT,
        SELF_SIGNED_CERT
    }

    private ServerParams() {
    }

    private String bindingHost;
    private int bindingPort;
    private SslMode sslMode;
    private int workerGroupThreadCount;
    private String websocketPath;

    public String getBindingHost() {
        return bindingHost;
    }

    public int getBindingPort() {
        return bindingPort;
    }

    public SslMode getSslMode() {
        return sslMode;
    }

    public int getWorkerGroupThreadCount() {
        return workerGroupThreadCount;
    }

    public String getWebsocketPath() {
        return websocketPath;
    }

    /**
     * Builder
     */
    public static class Builder {

        private ServerParams params;

        public Builder() {
            params = new ServerParams();
        }

        public ServerParams build() {
            if (params.getSslMode() == null) {
                params.sslMode = SslMode.NOSSL;
            }
            if (params.getBindingHost() == null) {
                params.bindingHost = "localhost";
            }
            if (params.getBindingPort() == 0) {
                params.bindingPort = params.getSslMode().equals(SslMode.NOSSL) ? 8080 : 8443;
            }
            if (params.getWorkerGroupThreadCount() == 0) {
                params.workerGroupThreadCount = 3;
            }
            if (params.getWebsocketPath() == null) {
                params.websocketPath = "/websocket";
            }
            return params;
        }

        public Builder bindingHost(String bindingHost) {
            params.bindingHost = bindingHost;
            return this;
        }

        public Builder bindingPort(int bindingPort) {
            params.bindingPort = bindingPort;
            return this;
        }

        public Builder sslMode(SslMode sslMode) {
            params.sslMode = sslMode;
            return this;
        }

        public Builder workerGroupThreadCount(int workerGroupThreadCount) {
            params.workerGroupThreadCount = workerGroupThreadCount;
            return this;
        }

        public Builder websocketPath(String websocketPath) {
            params.websocketPath = websocketPath;
            return this;
        }
    }

}
