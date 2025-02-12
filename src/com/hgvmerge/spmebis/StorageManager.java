package com.hgvmerge.spmebis;

import com.codename1.crypto.EncryptedStorage;
import com.codename1.io.Preferences;
import com.codename1.io.Storage;

public class StorageManager {
    
    public static final String MEBIS = "MDL";
    public static final String SCHUELERPORTAL = "SP";
    private static final String LAST_USED = "LU";
    
    public static void saveCredentials(String key, String value, String use) {
        EncryptedStorage.getInstance().writeObject(use + "_K", key);
        EncryptedStorage.getInstance().writeObject(use + "_V", value);
    }
    
    public static String[] loadCredentials(String use) {
        String[] credentials = new String[2];
        credentials[0] = (String) EncryptedStorage.getInstance().readObject(use + "_K");
        credentials[1] = (String) EncryptedStorage.getInstance().readObject(use + "_V");
        return credentials;
    }
    
    //TODO: on app close
    public static void saveLastUsed(long last) {
        Preferences.set(LAST_USED, last);
    }
    
    //TODO: on app start
    public static long loadLastUsed() {
        return Preferences.get(LAST_USED, 0);
    }
}
