package net.jgn.textshare.server.cfg;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import net.jgn.textshare.server.ServerParams;
import net.jgn.textshare.server.SslContextWrapper;
import net.jgn.textshare.server.TextShareServer;
import net.jgn.textshare.server.TextShareServerImpl;
import net.jgn.textshare.server.TextShareServerInitializer;
import net.jgn.textshare.server.session.SessionManager;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;
import org.springframework.jmx.support.MBeanServerFactoryBean;

import javax.management.MBeanServer;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;

/**
 * @author jose
 */
@Configuration
@ComponentScan(value = "net.jgn.textshare.server")
@Import(DbConfig.class)
@PropertySource("classpath:server.properties")
public class ServerConfig {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment env;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private SessionManager sessionManager;


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean(name = "privateKey")
    public File privateKey() {
        ServerParams serverParams = serverParams();
        if (serverParams.getSslMode().equals(ServerParams.SslMode.CERT)) {
            return new File(env.getProperty("PRIV_KEY_FILE"));
        } else if (serverParams.getSslMode().equals(ServerParams.SslMode.SELF_SIGNED_CERT)) {
            return new File("src/main/resources/self-signed-certs/test.key");
        }
        return null;
    }

    @Bean(name = "certificate")
    public File certificate() {
        ServerParams serverParams = serverParams();
        if (serverParams.getSslMode().equals(ServerParams.SslMode.CERT)) {
            return new File(env.getProperty("CERT_CHAIN_FILE"));
        } else if (serverParams.getSslMode().equals(ServerParams.SslMode.SELF_SIGNED_CERT)) {
            return new File("src/main/resources/self-signed-certs/test-self-signed.crt");
        }
        return null;
    }

    @Bean
    public ServerParams serverParams() {
        ServerParams.Builder builder = new ServerParams.Builder()
                .bindingHost(env.getProperty("server.host"))
                .bindingPort(env.getProperty("server.port", Integer.class, 0))
                .workerGroupThreadCount(env.getProperty("server.workerGroupThreadCount", Integer.class, 0))
                .websocketPath(env.getProperty("server.websocketPath"));
        String sslModeStr = env.getProperty("server.sslMode");
        if (sslModeStr != null) {
            try {
                ServerParams.SslMode sslMode = ServerParams.SslMode.valueOf(sslModeStr);
                builder.sslMode(sslMode);
            } catch (IllegalArgumentException e) {
                logger.warn("server.sslMode not valid: [{}]. Valid values are NOSSL, CERT, and SELF_SIGNED_CERT", sslModeStr);
            }
        }
        return builder.build();
    }

    @Bean
    public TextShareServer server() throws IOException, CertificateException {
        return new TextShareServerImpl(serverParams(), textShareServerInitializer());
    }

    @Bean
    public SslContextWrapper sslCtxWrapper() throws SSLException, CertificateException {
        ServerParams serverParams = serverParams();
        if (serverParams.getSslMode().equals(ServerParams.SslMode.NOSSL)) {
            return new SslContextWrapper(null);
        } else {
            SslContext sslCtx = SslContextBuilder.forServer(certificate(), privateKey()).build();
            return new SslContextWrapper(sslCtx);
        }
    }

    @Bean
    public TextShareServerInitializer textShareServerInitializer() throws IOException, CertificateException {
        return new TextShareServerInitializer(sqlSessionFactory,
                sslCtxWrapper().getSslContext(),
                sessionManager,
                env.getProperty("websocketPath"));
    }

    @Bean
    public SessionManager sessionManager() {
        return new SessionManager();
    }

    @Bean
    public MBeanServer mBeanServer() {
        MBeanServerFactoryBean mBeanServerFactoryBean = new MBeanServerFactoryBean();
        return mBeanServerFactoryBean.getObject();
    }

    @Bean
    public MBeanExporter mBeanExporter() {
        MBeanExporter mBeanExporter = new MBeanExporter();
        JmxAttributeSource jmxAttributeSource = new AnnotationJmxAttributeSource();
        mBeanExporter.setNamingStrategy(new MetadataNamingStrategy(jmxAttributeSource));
        mBeanExporter.setAssembler(new MetadataMBeanInfoAssembler(jmxAttributeSource));
        return mBeanExporter;
    }

}
