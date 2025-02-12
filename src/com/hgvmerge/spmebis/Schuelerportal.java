package com.hgvmerge.spmebis;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.Cookie;
import com.codename1.io.NetworkManager;

public class Schuelerportal {
        
    private final String school = "hugyvat";
    private final String apiUrl = "https://api.schueler.schule-infoportal.de/" + school;
    
    private String wstoken;
    
    private final ConnectionRequest connection;
    private String[] cookies = new String[2];
    
    public Schuelerportal(String email, String password) {        
        ConnectionRequest con = new ConnectionRequest() {
            @Override
            protected void cookieReceived(Cookie c) {
                if("XSRF-TOKEN".equals(c.getName())) {
                    cookies[0] = c.getValue();
                } else {
                    cookies[1] = c.getValue();
                }
            } 
        };
            
        // school
        con.setUrl(apiUrl + "/api/school");
        con.setHttpMethod("HEAD");
        NetworkManager.getInstance().addToQueueAndWait(con);
        con.setPost(true);
           
        // login
        con.setUrl(apiUrl + "/login");
        con.setHttpMethod("POST");
        String payload = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        con.setRequestBody(payload);
        con.addRequestHeader("X-XSRF-TOKEN", cookies[0].substring(0, 339) + "=");
        con.addRequestHeader("Accept", "application/json, text/plain, */*");
        con.addRequestHeader("Accept-Encoding", "gzip, deflate, br, zstd");
        con.addRequestHeader("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7");
        con.addRequestHeader("Connection", "keep-alive");
        con.addRequestHeader("Content-Type", "application/json");
        con.addRequestHeader("Host", apiUrl);
        con.addRequestHeader("Referer", "https://schueler.schule-infoportal.de/");
        con.addRequestHeader("Sec-Fetch-Dest", "empty");
        con.addRequestHeader("Sec-Fetch-Mode", "cors");
        con.addRequestHeader("Sec-Fetch-Site", "same-site");
        NetworkManager.getInstance().addToQueueAndWait(con);
        
        // user
        connection = new ConnectionRequest() {
            @Override
            protected void cookieReceived(Cookie c) {
                if("XSRF-TOKEN".equals(c.getName())) {
                    cookies[0] = c.getValue();
                }
            } 
        };      
        connection.setUrl(apiUrl + "/api/user");
        connection.setPost(false);
        connection.addRequestHeader("cookie", cookies[0] + ";" + cookies[1]);
        connection.addRequestHeader("X-XSRF-TOKEN", cookies[0].substring(0, 339) + "=");
        connection.addRequestHeader("Accept", "application/json, text/plain, */*");
        connection.addRequestHeader("Accept-Encoding", "gzip, deflate, br, zstd");
        connection.addRequestHeader("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7");
        connection.addRequestHeader("Connection", "keep-alive");
        connection.addRequestHeader("Content-Type", "application/json");
        connection.addRequestHeader("Host", apiUrl);
        connection.addRequestHeader("Referer", "https://schueler.schule-infoportal.de/");
        connection.addRequestHeader("Sec-Fetch-Dest", "empty");
        connection.addRequestHeader("Sec-Fetch-Mode", "cors");
        connection.addRequestHeader("Sec-Fetch-Site", "same-site");
        NetworkManager.getInstance().addToQueueAndWait(connection);
    }
    
    public String getRequest(String name) {
        connection.setUrl(apiUrl + "/api/" + name);
        connection.addRequestHeader("X-XSRF-TOKEN", cookies[0].substring(0, 339) + "=");
        NetworkManager.getInstance().addToQueueAndWait(connection);
        System.out.println(connection.getResponseCode() + " - " + connection.getUrl());
        return new String(connection.getResponseData());
    }
    
//    public void getPushNotifications() {
//        
//        WebSocket ws = new WebSocket("wss://api.schueler.schule-infoportal.de:6001/app/" + wstoken + "?protocol=7&client=js&version=8.3.0&flash=false") {
//            @Override
//            protected void onOpen() {
//                System.out.println("opened");
//            }
//            
//            @Override
//            protected void onMessage(String string) {
//                System.out.println(string);
//            }
//            
//            @Override
//            protected void onMessage(byte [] bytes) {
//                
//            }
//            
//            @Override
//            protected void onError(Exception excptin) {
//                excptin.printStackTrace();
//            }
//            
//            @Override
//            protected void onClose(int statusCode, String reason) {
//                System.out.println("closed: " + reason + " (" + statusCode + ")");
//            }
//        }.autoReconnect(30000);
//        ws.connect();
//        ws.send("{\"event\":\"pusher:subscribe\",\"data\":{\"auth\":\"" + auth + "\",\"channel\":" + channel + "\"");
//        
//    }
    
}
