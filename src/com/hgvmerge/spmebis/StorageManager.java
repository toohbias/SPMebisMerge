package com.hgvmerge.spmebis;

import ca.weblite.codename1.json.JSONException;
import ca.weblite.codename1.json.JSONObject;
import com.codename1.io.Preferences;
import com.codename1.io.Storage;

public class StorageManager {
    
    public static final String MEBIS = "MDL";
    public static final String SCHUELERPORTAL = "SP";
    private static final String LAST_USED = "LU";
    private static final String HOLIDAYS = "HD";
    
    public static void saveCredentials(String key, String value, String use) {
        Preferences.set(use + "K", key);
        Preferences.set(use + "V", value);
        System.out.println("saveCredentials success");
    }
    
    public static String[] loadCredentials(String use) {
        String[] result = { Preferences.get(use + "K", ""), Preferences.get(use + "V", "") };
        System.out.print("loadCredentials ");
        if(result[0].length() != 0 && result[1].length() != 0) { System.out.println("success"); } else { System.out.println("failed"); }
        return result;
    }
    
    //TODO: on app close
    public static void saveLastUsed(Time last) {
        Preferences.set(LAST_USED, last.getMillis());
        System.out.println("saveLastUsed success");
    }
    
    //TODO: on app start
    public static Time loadLastUsed(Time defaultvalue) {
        long lu = Preferences.get(LAST_USED, 0);
        System.out.print("loadLastUsed ");
        if(lu != 0) { System.out.println("success"); } else { System.out.println("failed: using default time " + defaultvalue.format()); return defaultvalue; }
        return new Time(lu);
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
