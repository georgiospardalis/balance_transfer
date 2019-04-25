package pardalis;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Properties;

import static org.h2.tools.Server.createTcpServer;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Properties properties = null;

        try {
            properties = loadProperties();
        } catch (IOException e) {
            LOGGER.error("Could not load application properties", e);
            System.exit(1);
        }

        Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                new AppModule(properties.getProperty("persistenceUnit")));

        Server jettyServer = setupJetty(
                Integer.parseInt(properties.getProperty("jetty.server.port")),
                Integer.parseInt(properties.getProperty("jetty.threads.max")),
                Integer.parseInt(properties.getProperty("jetty.threads.min")),
                Integer.parseInt(properties.getProperty("jetty.idle.timeout")));


        try {
            setupH2(properties.getProperty("h2.tcp.port"));
            jettyServer.start();
            jettyServer.join();
        } catch (Exception e) {
            LOGGER.error("Server(s) Startup Failed", e);
            System.exit(1);
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        InputStream in = App.class.getResourceAsStream("/app.properties");

        properties.load(in);

        return properties;
    }

    private static Server setupJetty(int serverPort, int maxThreads, int minThreads, int idleInMillis) {
        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleInMillis);
        Server server = new Server(threadPool);
        ServerConnector serverConnector = new ServerConnector(server);

        serverConnector.setPort(serverPort);
        server.setConnectors(new Connector[]{serverConnector});

        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);

        servletContextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.of(javax.servlet.DispatcherType.REQUEST, javax.servlet.DispatcherType.ASYNC));
        servletContextHandler.addServlet(DefaultServlet.class, "/*");

        return server;
    }

    private static void setupH2(String h2TcpPort) throws SQLException {
        createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", h2TcpPort).start();
    }
}