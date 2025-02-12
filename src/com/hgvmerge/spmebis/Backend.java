package com.hgvmerge.spmebis;

import ca.weblite.codename1.json.JSONArray;
import ca.weblite.codename1.json.JSONException;
import ca.weblite.codename1.json.JSONObject;
import com.codename1.io.Cookie;
import com.codename1.ui.CN;
import java.util.ArrayList;

public class Backend {
    
    private static Backend instance;
    
    private final Schuelerportal schuelerportal;
    private final Mebis mebis;
    private final DB database;
    
    private long lastUsed;
    
    private Backend() {
        CN.updateNetworkThreadCount(2);
        Cookie.clearCookiesFromStorage();
        String[] sp = StorageManager.loadCredentials(StorageManager.SCHUELERPORTAL);
        String[] mdl = StorageManager.loadCredentials(StorageManager.MEBIS);
        database = new DB();
        schuelerportal = new Schuelerportal(sp[0], sp[1]);
        mebis = new Mebis(mdl[0], mdl[1]);
        
        lastUsed = StorageManager.loadLastUsed();
    }
    
    public static Backend getInstance() {
        if(instance == null) {
            instance = new Backend();
        }
        return instance;
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
                } else if(/* latestMessage timestamp > last stored timestamp */ chat.getJSONObject("latestMessage").getLong("timestamp") > database.getLongValue("SELECT lastmessagedate FROM Chat WHERE ID = " + id)) {
                    updateChat(new JSONObject(schuelerportal.getRequest("chat/" + id)));
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    private void updateChat(JSONObject obj) {
        try {
            JSONArray messages = obj.getJSONArray("messages");
            String id = obj.getString("id");
            long  lastMessageTimestamp = database.getLongValue("SELECT lastmessagedate FROM Chat WHERE ID = " + id);
            System.out.println(messages.length());
            for(int i = messages.length() - 1; i >= 0; i-- /* last messages read first */) {
                JSONObject message = messages.getJSONObject(i);
                if(message.getLong("createdAt") > lastMessageTimestamp) {
                    if(!message.isNull("file")) {
                        JSONObject file = message.getJSONObject("file");
                        database.addEntry(DB.TABLE_MESSAGE, message.getString("id"),                                            /* ID       */
                                                            "\"" + message.getString("text") + "\"",                            /* message  */
                                                            "\"" + message.getJSONObject("editor").getString("name") + "\"",    /* creator  */
                                                            message.getString("createdAt"),                                     /* date     */
                                                            id,                                                                 /* chatID   */
                                                            file.getString("id"));                                              /* fileID   */
                    
                        database.addEntry(DB.TABLE_FILE, file.getString("id"),                      /* ID   */
                                                         "\"" + file.getString("link") + "\"",      /* url  */
                                                         "\"" + file.getString("name") + "\"");     /* name */
                    }
                    
                } else {
                    break;
                }
            }
            database.updateEntry(DB.TABLE_CHAT, id, "lastmessagedate", messages.getJSONObject(messages.length() - 1).getString("createdAt"));
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    private void addChat(JSONObject chat) {
        try {
            database.addEntry(DB.TABLE_CHAT, chat.getString("id"),                                          /* ID           */
                                             "\"" + chat.getString("name") + "\"",                          /* name         */
                                             "\"" + chat.getJSONObject("owner").getString("name") + "\"",   /* owner        */
                                             chat.getString("createdAt"),                                   /* scholyear    */    
                                             "0");      /* updated in updateChat()  */                      /* lastmessage  */  
            
            updateChat(new JSONObject(schuelerportal.getRequest("chat/" + chat.getString("id"))));
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }
    
}
