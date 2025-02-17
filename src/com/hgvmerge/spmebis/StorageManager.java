package com.hgvmerge.spmebis;

import ca.weblite.codename1.json.JSONException;
import ca.weblite.codename1.json.JSONObject;
import com.codename1.crypto.EncryptedStorage;
import com.codename1.io.Preferences;
import com.codename1.io.Storage;

public class StorageManager {
    
    public static final String MEBIS = "MDL";
    public static final String SCHUELERPORTAL = "SP";
    private static final String LAST_USED = "LU";
    private static final String HOLIDAYS = "HD";
    
    public static void saveCredentials(String key, String value, String use) {
        boolean k = EncryptedStorage.getInstance().writeObject(use + "_K", key);
        boolean v = EncryptedStorage.getInstance().writeObject(use + "_V", value);
        System.out.print("saveCredentials ");
        if(k && v) { System.out.println("success"); } else { System.out.println("failed"); }
    }
    
    public static String[] loadCredentials(String use) {
        String[] credentials = new String[2];
        credentials[0] = (String) EncryptedStorage.getInstance().readObject(use + "_K");
        credentials[1] = (String) EncryptedStorage.getInstance().readObject(use + "_V");
        boolean k = credentials[0] != null;
        boolean v = credentials[1] != null;
        System.out.print("loadCredentials ");
        if(k && v) { System.out.println("success"); } else { System.out.println("failed"); }
        return credentials;
    }
    
    //TODO: on app close
    public static void saveLastUsed(long last) {
        Preferences.set(LAST_USED, last);
        System.out.println("saveLastUsed success");
    }
    
    //TODO: on app start
    public static long loadLastUsed() {
        int lu = Preferences.get(LAST_USED, 0);
        System.out.print("loadLastUsed ");
        if(lu != 0) { System.out.println("success"); } else { System.out.println("failure"); }
        return lu;
    }
    
    public static void saveHolidays(JSONObject holidays) {
        boolean h = Storage.getInstance().writeObject(HOLIDAYS, holidays.toString());
        System.out.print("saveHolidays ");
        if(h) { System.out.println("success"); } else { System.out.println("failed"); }
    }
    
    public static JSONObject loadHolidays() {
        try {
            System.out.print("loadHolidays ");
            JSONObject h;
            if(Storage.getInstance().exists(HOLIDAYS)) {
                System.out.println("success");
                h = new JSONObject((String) Storage.getInstance().readObject(HOLIDAYS));
            } else {
                System.out.print("failed: downloading holidays... ");
                h = Holidays.downloadHolidays();
                saveHolidays(h);
            }
            return h;
        } catch (JSONException ex) {
            System.out.println("failed");
            ex.printStackTrace();
            return null;
        }
    }
}
