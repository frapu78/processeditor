/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server;

import com.inubit.research.server.exchange.AbstractHandler;
import com.inubit.research.server.exchange.DynamicHandler;
import com.inubit.research.server.exchange.StaticHandler;
import com.inubit.research.server.manager.TemporaryKeyManager;
import com.inubit.research.server.request.handler.PersistentModelLocationsRequestHandler;
import com.inubit.research.server.request.handler.PersistentModelRequestHandler;
import com.inubit.research.server.request.handler.RootRequestHandler;
import com.inubit.research.server.request.handler.TemporaryModelRequestHandler;
import com.inubit.research.server.request.handler.UserRequestHandler;
import com.inubit.research.server.request.handler.UtilsRequestHandler;
import com.inubit.research.server.request.handler.AdminRequestHandler;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.inubit.research.server.request.handler.EditorRequestHandler;
import com.inubit.research.server.request.handler.MailRequestHandler;
import com.inubit.research.server.request.handler.PluginRequestHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import java.net.BindException;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.*;

/**
 * Provides a Back-End for the ProcessEditor Web Front-End.
 *
 * @author fpu
 * @author fel
 */
public class ProcessEditorServer {

    public static final String CONF_SERVER_START_AT_WB_STARTUP = "StartServerAtStartUp";
    public static final String CONF_SERVER_PORT = "ServerPort";
    public static final int DEFAULT_PORT = ProcessEditorServerHelper.getPort();
    public static final boolean DEFAULT_PORTLET_MODE = false;
    public static final int MAX_PORT = 65535;
    private static final String RELATIVE_WWW_FOLDER = "www";
    private static final String LOG_CONFIG = "/config/serverlogging.properties";
    private static final String SSL_CONFIG = "/config/ssl_config.properties";
    private static final String SSL_CONFIG_KEY_STORE = "KEY_STORE";
    private static final String SSL_CONFIG_KEY_STORE_ALIAS = "KEY_STORE_ALIAS";
    private static final String SSL_CONFIG_KEY_PASSWORD = "KEY_PASSWORD";
    private static final String SSL_CONFIG_KEY_STORE_PASSWORD = "KEY_STORE_PASSWORD";
    private static final AbstractHandler[] DEFAULT_HANDLER_SET = new AbstractHandler[]{
        new DynamicHandler(new RootRequestHandler(), "/"),
        new DynamicHandler(true, new AdminRequestHandler(), "/admin"),
        new DynamicHandler(true, new EditorRequestHandler(), "/editor"),
        new DynamicHandler(true, new EditorRequestHandler(), "/models/new"),
        new StaticHandler("/html"),
        new StaticHandler("/js"),
        new StaticHandler("/css"),
        new DynamicHandler(true, new MailRequestHandler(), "/mail"),
        new DynamicHandler(true, new PersistentModelRequestHandler(), "/models"),
        new DynamicHandler(true, new PersistentModelLocationsRequestHandler(), "/models/locations"),
        new DynamicHandler(true, new TemporaryModelRequestHandler(), "/models/tmp"),
        new StaticHandler("/pics", true),
        new DynamicHandler(true, new PluginRequestHandler(), "/plugins"),
        new DynamicHandler(new UserRequestHandler(), "/users"),
        new DynamicHandler(new UtilsRequestHandler(), "/utils"),
    };
    private int port;
    private static ProcessEditorServer workbenchInstance;
    private static LogManager logManager;
    private static final Logger logger = Logger.getLogger("research.server.ProcessEditorServer");
    private InetSocketAddress address;
    private Set<AbstractHandler> handlers = new HashSet<AbstractHandler>();
    public static HashMap<Object, Long> startTimes = new HashMap<Object, Long>();
    private boolean setup = false;
    private boolean running = false;
    private boolean secure = false;
    private HttpServer server;

    static {
        //configure log manager
        logManager = LogManager.getLogManager();
        try {
            // Try loading from filesystem first...
            File f = new File(RELATIVE_WWW_FOLDER + LOG_CONFIG);
            if (f.exists()) {
                logManager.readConfiguration(new FileInputStream(RELATIVE_WWW_FOLDER + LOG_CONFIG));
            } else {
                logManager.readConfiguration(ProcessEditorServer.class.getResourceAsStream(LOG_CONFIG));
            }
        } catch (Exception e) {
            System.out.println("Could not read logging configuration. File " + LOG_CONFIG + " not found.");
        }

        logManager.addLogger(logger);
    }

    public ProcessEditorServer() {
        this.port = ProcessEditorServerHelper.getPort();
    }

    public ProcessEditorServer(boolean secure) {
        this();
        this.secure = secure;
    }

    public ProcessEditorServer(int port, boolean secure) {
        this.port = port;
        this.secure = secure;
    }

    public ProcessEditorServer(int port, boolean secure, Set<AbstractHandler> handlers) {
        this(port, secure);
        this.handlers = handlers;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void loadDefaultHandlers() {
        this.handlers = new HashSet(Arrays.asList(DEFAULT_HANDLER_SET));
    }

    public void addHandler(AbstractHandler ah) {
        this.handlers.add(ah);
    }

    /**
     * Initialize HttpServer instance
     * @throws Exception
     */
    public void init() throws Exception {
        InetAddress host = null;
        if (ProcessEditorServerHelper.getHost() != null) {
            host = InetAddress.getByName(ProcessEditorServerHelper.getHost());
        }

        if (host != null) {
            // Bind to specific interface
            address = new InetSocketAddress(host, port);
        } else {
            // Bind to all interfaces
            System.out.println("BINDING TO ALL INTERFACES");
            address = new InetSocketAddress(port);
        }
        //secure = true;
        if (ProcessEditorServerHelper.isSecure()) {

            // Read config from file
            Properties sslConfig = new Properties();
            try
                {
                // Try loading from filesystem first...
                File f = new File(RELATIVE_WWW_FOLDER + LOG_CONFIG);
                if (f.exists()) {
                    sslConfig.load(new FileInputStream(RELATIVE_WWW_FOLDER + SSL_CONFIG));
                } else {
                    sslConfig.load(ProcessEditorServer.class.getResourceAsStream(SSL_CONFIG));
                    }
                }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to parse SSL config (see www/config/ssl_config.properties.template");
            }

            String keyStore = sslConfig.getProperty(SSL_CONFIG_KEY_STORE);
            String alias = sslConfig.getProperty(SSL_CONFIG_KEY_STORE_ALIAS);
            String password = sslConfig.getProperty(SSL_CONFIG_KEY_PASSWORD);
            String storePassword = sslConfig.getProperty(SSL_CONFIG_KEY_STORE_PASSWORD);

            // load certificate
            char[] storepass = storePassword.toCharArray();
            char[] keypass = password.toCharArray();
            InputStream fIn = ProcessEditorServer.class.getResourceAsStream(keyStore);
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(fIn, storepass);
            // display certificate
            Certificate cert = keystore.getCertificate(alias);
            System.out.println(cert);
            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, keypass);
            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore);

            // create https server
            this.server = HttpsServer.create(address, 255);
            // create ssl context
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            ((HttpsServer) this.server).setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {

                        // get the remote address if needed
                        InetSocketAddress remote = params.getClientAddress();

                        SSLContext c = getSSLContext();

                        // get the default parameters
                        SSLParameters sslparams = c.getDefaultSSLParameters();

                        params.setSSLParameters(sslparams);

                        /*
                        // initialise the SSL context
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                        */
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Failed to create HTTPS server");
                    }
                }
            });

            /*
            KeyStore ks = KeyStore.getInstance("JKS");
            char[] pwd = "inubit".toCharArray();
            ks.load(ProcessEditorServer.class.getResourceAsStream(KEY_STORE), pwd);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, pwd);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            this.server = HttpsServer.create(address, 255);

            ((HttpsServer) this.server).setHttpsConfigurator(new HttpsConfigurator(ssl) {

                public void configure(HttpsParameters params) {
                    // get the remote address if needed
                    InetSocketAddress remote = params.getClientAddress();

                    SSLContext c = getSSLContext();

                    // get the default parameters
                    SSLParameters sslparams = c.getDefaultSSLParameters();

                    params.setSSLParameters(sslparams);
                    // statement above could throw IAE if any params invalid.
                    // eg. if app has a UI and parameters supplied by a user.

                }
            });
            */
        } else {
            this.server = HttpServer.create(address, 255);
        }

        System.out.println("[Server] Setting up server at " + address.getAddress().getHostAddress() + ":" + address.getPort());
        logger.info("[Server] Setting up server at " + address.getAddress().getHostAddress() + ":" + address.getPort());

        for (AbstractHandler h : handlers) {
            server.createContext(h.getContextUri(), h);
        }

        BlockingQueue queue = new ProcessEditorBlockingQueue(1000);
        ThreadPoolExecutor exec = new ProcessEditorThreadPoolExecutor(10, 20, 5000, TimeUnit.MILLISECONDS, queue);

        TemporaryKeyManager.initialize();

        server.setExecutor(exec); // creates executor

        this.setup = true;
    }

    /**
     * Start the server
     */
    public void start() {
        System.out.println("Server started...");
        if (this.server != null) {
            this.server.start();
        }
        this.running = true;
    }

    /**
     * Stop the server
     */
    public void stop() {
        System.out.println("Server stopped...");
        if (server != null) {
            this.server.stop(0);
            this.setup = false;
            this.server = null;
        }

        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public String getUrl() {
        if (!this.setup) {
            try {
                this.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ProcessEditorServerHelper.getProtocol() + ":/" + this.server.getAddress().toString();
    }

    /**
     * Main method for manual start up
     * 2for test purpose only
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
 
        ProcessEditorServer server = getInstanceForWorkbench();

        if (args.length > 0) {

            if (args[0].equals("-h")) {
                System.out.println("All params: \n"
                        + "\t-s Start server with HTTPS protocol\n"
                        + "\t-h<if> Use <if> as specific host interface instead of all\n"
                        + "\t-p<port> Use <port> instead of default (1205)\n"
                        + "\t-d Start server in developer/debug mode\n"
                        + "\t-l <filename> Name of the log file");

                System.exit(0);
            }

            for (int i = 0; i < args.length; i++) {

                if (args[i].equals("-l")) {
                    Properties props = new Properties();

                    props.load(new FileInputStream(LOG_CONFIG));

                    props.setProperty("java.util.logging.FileHandler.pattern", args[i + 1]);
                    i++;

                    File tmpConfig = new File(LOG_CONFIG + "_tmp");

                    props.store(new FileOutputStream(tmpConfig), null);

                    logManager.readConfiguration(new FileInputStream(tmpConfig));

                    tmpConfig.deleteOnExit();
                }

                if (args[i].equals("-s")) {
                    ProcessEditorServerHelper.setSecure(true);
                    System.out.println("SECURE MODE ACTIVATED");
                }

                if (args[i].equals("-d")) {
                    ProcessEditorServerHelper.setDebugMode(true);
                    System.out.println("DEBUG MODE ACTIVATED");
                }

                if (args[i].startsWith("-h")) {
                    String host = args[i].replace("-h", "");
                    ProcessEditorServerHelper.setHost(host);
                    System.out.println("HOST INTERFACE SET TO " + host);
                }

                if (args[i].startsWith("-p")) {
                    // Try to parse port
                    try {
                        int port = Integer.parseInt(args[i].replace("-p", ""));
                        ProcessEditorServerHelper.setPort(port);
                        System.out.println("PORT SET TO " + port);
                    } catch (Exception ex) {
                        // ignore, use default
                        System.out.println("PORT NOT RECOGNIZED, USING DEFAULT");
                    }
                }

            }
        }

        server.init();

        server.start();
    }

    public static boolean isPortInUse(int port) {
        try {
            Socket sock = new Socket();
            sock.bind(new InetSocketAddress(port));
            sock.close();
            return false;
        } catch (BindException ex) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public static ProcessEditorServer getInstanceForWorkbench() {
        if (workbenchInstance == null) {
            workbenchInstance = new ProcessEditorServer();
            workbenchInstance.loadDefaultHandlers();
        }


        return workbenchInstance;
    }

    public static void startForWorkbench() throws Exception {
        startForWorkbench(false, -1);
    }

    public static void startForWorkbench(boolean secure, int portNumber) throws Exception {
        ProcessEditorServer pes = getInstanceForWorkbench();
        if (pes.isRunning()) {
            return;
        }
        if (portNumber == -1) {
            portNumber = DEFAULT_PORT;
        }

        if (isPortInUse(portNumber)) {
            System.err.println("Unable to start server for Workbench!\nConfigured port " + portNumber + " is already in use!");
            return;
        }

        pes.setPort(portNumber);
        System.err.println("Starting");
        if (!pes.setup || secure != ProcessEditorServerHelper.isSecure()) {
            ProcessEditorServerHelper.setSecure(secure);
            pes.init();
        }

        pes.start();
        assert getInstanceForWorkbench().isRunning();
    }

    public static void addLogger(Logger l) {
        logManager.addLogger(l);
    }

    /**
     * can be used by subclasses
     * e.g. to add a new context
     * @return
     */
    public HttpServer getHttpServer() {
        return server;
    }
}