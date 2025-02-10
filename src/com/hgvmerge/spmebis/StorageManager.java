package com.hgvmerge.spmebis;

import com.codename1.crypto.EncryptedStorage;
import com.codename1.io.Preferences;
import com.codename1.io.Storage;

public class StorageManager {
    
    public static final String MEBIS = "MDL";
    public static final String SCHUELERPORTAL = "SP";
    private static final String LAST_USED = "LU";
    
    public static void saveCredentials(String key, String value, String use) {
        EncryptedStorage.getInstance().writeObject(use, new String[] {key, value});
    }
    
    public static String[] loadCredentials(String key) {
        return (String[]) EncryptedStorage.getInstance().readObject(key);
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
