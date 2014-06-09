/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.user.TemporaryUser;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author fel
 */
public class TemporaryKeyManager {
    private static final int checkInterval = 900000;

    static class AccessListCleaner extends TimerTask {

        @Override
        public void run() {
            Date currentDate = new Date();
            Set<String> removableKeys = new HashSet<String>();
            for (Map.Entry<String, TemporaryUser> e  : accessList.entrySet()) {
                Date exDate = e.getValue().getExpiryDate();

                if (currentDate.compareTo(exDate) > 0) {
                    removableKeys.add(e.getKey());
                }
            }
            System.out.println("Cleaning temporary keys");
            for (String key : removableKeys) {
                accessList.remove(key);
            }
        }
    }

    private static Map<String, TemporaryUser> accessList = new HashMap<String, TemporaryUser>();

    public static String addUser( TemporaryUser user ) {
        String key = getUnusedKey();
        accessList.put(key, user);

        return key;
    }

    public static TemporaryUser checkKey( String key, String id, String version ) {
        TemporaryUser tu = accessList.get(key);

        if ( tu != null ) {
            Date currDate = new Date();
            if (tu.getExpiryDate().compareTo(currDate) >= 0) 
                if (id.equals(tu.getModelId()) && Integer.parseInt(version) == tu.getModelVersion())
                    return tu;
        }

        return null;
    }

    private static String getUnusedKey() {
        Random ran = new Random(System.currentTimeMillis());
        long l = ran.nextLong();

        String newKey = ProcessEditorServerUtils.getMD5Hash(String.valueOf(l));
        
        while (accessList.containsKey(newKey)) {
            l = ran.nextLong();
            newKey = ProcessEditorServerUtils.getMD5Hash(String.valueOf(l));
        }

        return newKey;
    }

    public static void initialize() {
        Timer timer = new Timer(true);
        timer.schedule(new AccessListCleaner(), new Date(), checkInterval);
    }
}