/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server;

import com.inubit.research.server.config.ServerConfig;
import com.inubit.research.server.config.UsersConfig;
import com.inubit.research.server.manager.Location;
import com.inubit.research.server.manager.UserManager;
import com.inubit.research.server.persistence.DatabaseConnector;
import com.inubit.research.server.persistence.FileSystemConnector;
import com.inubit.research.server.persistence.PersistenceConnector;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.Configuration;

/**
 *
 * @author fel
 */
public class ProcessEditorServerHelper {
    public static final String SERVER_HOME_DIR = System.getProperty("user.home") + "/.ProcessEditorServer";
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    public static final String CONF_SERVER_SECURE = "SecureServer";
    private static final String CONF_SERVER_PORTLET_MODE = "ServerPortletMode";

    private static PersistenceConnector persistenceConnector;
    private static ServerConfig serverConfig;
    private static Set<File> additionalResourceDirectories = new HashSet<File>();

    private static boolean isPortletModeConfigured;
    private static boolean isDebugMode;
    private static boolean secure = Configuration.getInstance().getProperty(CONF_SERVER_SECURE).equals("1");
    /** The default port */
    private static int port = port = Configuration.getInstance().getProperty(ProcessEditorServer.CONF_SERVER_PORT).equals("") ?
        1205 : Integer.parseInt(Configuration.getInstance().getProperty(ProcessEditorServer.CONF_SERVER_PORT));
    private static String host = null;


    static {
        persistenceConnector = new FileSystemConnector();

        serverConfig = FileSystemConnector.loadServerConfig();

        isPortletModeConfigured = Boolean.parseBoolean( serverConfig.getStartUpOtion( CONF_SERVER_PORTLET_MODE ) );
    }

    public static UserManager getUserManager() {
        return serverConfig.getUserManager();
    }

    public static UsersConfig getUsersConfig() {
        return serverConfig.loadUsersConfig();
    }

    public static PersistenceConnector getPersistenceConnector() {
        return persistenceConnector;
    }

    public static DatabaseConnector getDatabaseConnector() {
        return serverConfig.getDatabaseConnector();
    }

    public static Class<? extends Location> getDefaultLocationClass() {
        return serverConfig.getDefaultLocationClass();
    }

    public static boolean isPortletModeConfigured() {
        return isPortletModeConfigured;
    }

    public static boolean isDebugMode() {
        return isDebugMode;
    }

    public static void setDebugMode( boolean debugMode ) {
        isDebugMode = debugMode;
    }

    public static boolean isSecure() {
        return secure;
    }

    public static void setSecure( boolean sec ) {
        secure = sec;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        ProcessEditorServerHelper.port = port;
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        ProcessEditorServerHelper.host = host;
    }

    public static String getProtocol() {
        if (secure)
            return "https";
        else
            return "http";
    }

    public static void addAdditionalResourceDirectory( String path ) {
        File f = new File(path);
        if (f.isDirectory())
            additionalResourceDirectories.add(f);
    }

    public static void addAdditionalResourceDirectory( File f ) {
        if (f.isDirectory())
            additionalResourceDirectories.add(f);
    }

    public static Set<File> getAdditionalResourceDirectories() {
        return additionalResourceDirectories;
    }
}
