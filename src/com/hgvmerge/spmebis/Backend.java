package com.hgvmerge.spmebis;

import ca.weblite.codename1.json.JSONArray;
import ca.weblite.codename1.json.JSONException;
import ca.weblite.codename1.json.JSONObject;
import com.codename1.io.Cookie;
import com.codename1.io.Util;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.CN;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;

public class Backend {
    
    private static Backend instance;
    
    private final Schuelerportal schuelerportal;
    private final Mebis mebis;
    private final DB database;
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final Calendar lastUsed;   
    private final HashSet<String> schoolFreeDays;
    
    private Backend() {
        CN.updateNetworkThreadCount(2);
        Cookie.clearCookiesFromStorage();
        String[] sp = StorageManager.loadCredentials(StorageManager.SCHUELERPORTAL);
        String[] mdl = StorageManager.loadCredentials(StorageManager.MEBIS);
        database = new DB();
        schuelerportal = new Schuelerportal(sp[0], sp[1]);
        mebis = new Mebis(mdl[0], mdl[1]);
        
        lastUsed = Calendar.getInstance();
//        lastUsed.setTime(new Date(StorageManager.loadLastUsed()));
        
        schoolFreeDays = Holidays.getSchoolFreeDays(StorageManager.loadHolidays());
    }
    
    public static Backend getInstance() {
        if(instance == null) {
            instance = new Backend();
        }
        return instance;
    }
    
    private static String formatString(String input, boolean encoded) {
        String result = "";
        if(encoded) {
            result = Util.encodeBody(input);
        }
        return "\"" + result + "\"";
    }
    
    private void addFile(JSONObject file) {
        try {
            database.addEntry(DB.TABLE_FILE, file.getString("id"),      /* ID   */
                    formatString(file.getString("link"), false),        /* url  */
                    formatString(file.getString("name"), true));        /* name */  /* encoded */
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    private String buildFilesString(JSONArray files) {
        String fileIDs = "";
        if(files.length() != 0) {
            for(int j = 0; j < files.length(); j++) {
                try {
                    JSONObject file = files.getJSONObject(j);
                    addFile(file);
                    fileIDs = fileIDs + "," + file.getString("id");
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            fileIDs = fileIDs.substring(1);
        }
        fileIDs = "\"[" + fileIDs + "]\"";
        return fileIDs;
    }
    
    public void updateChats() {
        try {
            JSONArray chats = new JSONArray(schuelerportal.getRequest("chat"));
            ArrayList<Integer> chatIDsDB = database.getSingleFieldFromPrompt("SELECT ID FROM Chat", DB.TYPE_INTEGER);
            for(int i = 0; i < chats.length(); i++) {
                JSONObject chat = chats.getJSONObject(i);
                int id = chat.getInt("id");
                if(!chatIDsDB.contains(id)) {
                    addChat(chat);
                } else if(chat.isNull("latestMessage")) {
                } else if(chat.getJSONObject("latestMessage").getLong("timestamp") > database.getLongValue("SELECT lastmessagedate FROM Chat WHERE ID = " + id)) {
                    updateChat(id);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    private void updateChat(int chatId) {
        try {
            JSONObject obj = new JSONObject(schuelerportal.getRequest("chat/" + chatId));
            JSONArray messages = obj.getJSONArray("messages");
            String id = obj.getString("id");
            long  lastMessageTimestamp = database.getLongValue("SELECT lastmessagedate FROM Chat WHERE ID = " + id);
            for(int i = messages.length() - 1; i >= 0; i-- /* last messages read first */) {
                JSONObject message = messages.getJSONObject(i);
                if(message.getLong("createdAt") > lastMessageTimestamp) {
                    if(!message.isNull("file")) {
                        JSONObject file = message.getJSONObject("file");
                        database.addEntry(DB.TABLE_MESSAGE, message.getString("id"),                                                /* ID       */
                                                            formatString(message.getString("text"), true),                          /* message  */  /* encoded */
                                                            formatString(message.getJSONObject("editor").getString("name"), true),  /* creator  */  /* encoded */
                                                            message.getString("createdAt"),                                         /* date     */
                                                            id,                                                                     /* chatID   */
                                                            file.getString("id"));                                                  /* fileID   */
                        addFile(file);
                    }
                    
                } else {
                    break;
                }
            }
            if(messages.length() == 0) { return;}
            database.updateEntry(DB.TABLE_CHAT, id, "lastmessagedate", messages.getJSONObject(messages.length() - 1).getString("createdAt"));
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    private void addChat(JSONObject chat) {
        try {
            database.addEntry(DB.TABLE_CHAT, chat.getString("id"),                                              /* ID           */
                                             formatString(chat.getString("name"), true),                        /* name         */  /* encoded */
                                             formatString(chat.getJSONObject("owner").getString("name"), true), /* owner        */  /* encoded */
                                             chat.getString("createdAt"),                                       /* scholyear    */    
                                             "0");      /* updated in updateChat()  */                          /* lastmessage  */  
            
            updateChat(chat.getInt("id"));
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    public void updateUnterrichtAndHausaufgaben() {
        Calendar calIt = Calendar.getInstance();
        while(calIt.after(lastUsed)) {
            String date = sdf.format(calIt.getTime());
            if(!(calIt.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calIt.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || schoolFreeDays.contains(date))) {
                updateUnterricht(date);
            }
            calIt.add(Calendar.SECOND, -60 * 60 * 24);
        }
        updateHausaufgaben();
    }
    
    private void updateUnterricht(String date) {
        try {
            JSONArray unterrichtsinhalte = new JSONArray(schuelerportal.getRequest("unterricht/" + date));
            for(int i = 0; i < unterrichtsinhalte.length(); i++) {
                JSONObject unterricht = unterrichtsinhalte.getJSONObject(i);
                
                String unterrichtID = String.valueOf(new Random().nextInt());

                //add connected Hausaufgabe
                String hwID = "-1";
                if(!unterricht.isNull("homework")) {
                    JSONObject homework = unterricht.getJSONObject("homework");                   
                    hwID = homework.getString("id");
                    database.addEntry(DB.TABLE_HAUSAUFGABE, hwID,                                                                       /* ID           */
                                                            formatString(unterricht.getJSONObject("subject").getString("long"), true),  /* class        */  /* encoded */
                                                            formatString(homework.getString("homework"), true),                         /* message      */  /* encoded */
                                                            formatString(unterricht.getString("teacher"), true),                        /* teacher      */  /* encoded */
                                                            formatString(unterricht.getString("date"), false),                          /* date         */
                                                            formatString(homework.getString("due_at"), false),                          /* due          */
                                                            buildFilesString(homework.getJSONArray("files")),                           /* files        */
                                                            "0",                                                                        /* completed    */
                                                            unterrichtID);                                                              /* unterrichtID */
                }
                    
                database.addEntry(DB.TABLE_UNTERRICHT, unterrichtID,                                                                                /* ID               */
                                                       formatString(unterricht.getJSONObject("subject").getString("long"), true),                   /* class            */  /* encoded */
                                                       formatString(unterricht.getJSONObject("content").getString("text"), true),                   /* message          */  /* encoded */
                                                       formatString(unterricht.getString("teacher"), true),                                         /* teacher          */  /* encoded */
                                                       formatString(unterricht.getString("date"), false),                                           /* date             */
                                                       "\"[" + unterricht.getString("hour_from") + "," + unterricht.getString("hour_to") + "]\"",   /* hour             */
                                                       buildFilesString(unterricht.getJSONObject("content").getJSONArray("files")),                 /* files            */
                                                       hwID);                                                                                       /* hausaufgabenID   */
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    //TODO: submissions/back (also in DB)
    private void updateHausaufgaben() {
        try {
            JSONArray hausaufgaben = new JSONArray(schuelerportal.getRequest("hausaufgaben"));
            for(int i = 0; i < hausaufgaben.length(); i++) {
                JSONObject hausaufgabe = hausaufgaben.getJSONObject(i);
                if(!database.exists(DB.TABLE_HAUSAUFGABE, hausaufgabe.getString("id"))) {
                    database.addEntry(DB.TABLE_HAUSAUFGABE, hausaufgabe.getString("id"),                                                /* ID           */
                                                            formatString(hausaufgabe.getJSONObject("subject").getString("long"), true), /* class        */  /* encoded */
                                                            formatString(hausaufgabe.getString("homework"), true),                      /* message      */  /* encoded */
                                                            formatString(hausaufgabe.getString("teacher"), true),                       /* teacher      */  /* encoded */
                                                            formatString(hausaufgabe.getString("date"), false),                         /* date         */
                                                            formatString(hausaufgabe.getString("due_at"), false),                       /* due          */
                                                            buildFilesString(hausaufgabe.getJSONArray("files")),                        /* files        */
                                                            hausaufgabe.getBoolean("completed") ? "1" : "0",                            /* completed    */
                                                            "-1");                                                                      /* unterrichtID */
                } else {
                    database.updateEntry(DB.TABLE_HAUSAUFGABE, hausaufgabe.getString("id"), "completed", hausaufgabe.getBoolean("completed") ? "1" : "0");
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    
}
