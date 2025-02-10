package com.hgvmerge.spmebis;

import ca.weblite.codename1.json.JSONArray;
import ca.weblite.codename1.json.JSONException;
import ca.weblite.codename1.json.JSONObject;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.Cookie;
import com.codename1.io.NetworkManager;
import com.codename1.util.StringUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Mebis {
    
    private String moodleSession, sesskey = "";
    
    private final ConnectionRequest con;
    
    public Mebis(String username, String password) {
        String url = "https://lernplattform.mebis.bycs.de/";
        con = new ConnectionRequest() {
            @Override
            protected void cookieReceived(Cookie c) {
                if("MoodleSession".equals(c.getName())) {
                    moodleSession = c.getValue();
                }
            }     
        };
        try {
//          Loading login page
            con.setUrl(url);
            con.setPost(false);
            NetworkManager.getInstance().addToQueueAndWait(con);

//          Fetching redirect url from response         
            String s = new String(con.getResponseData());
            int pos = s.indexOf("action=\"") + 8;
            String redirect = s.substring(pos, pos + 229);

            redirect = StringUtil.replaceAll(redirect, "amp;", "");  
    
            
//          Finishing login with credentials
            String payload = "username=" + username + "&password=" + password + "&credentialId=";

            con.setUrl(redirect);
            con.setPost(true);
            con.setRequestBody(payload);
            con.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            con.addRequestHeader("Accept-Encoding", "deflate, br, zstd");
            con.addRequestHeader("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7");
            con.addRequestHeader("Cache-Control", "max-age=0");
            con.addRequestHeader("Connection", "keep-alive");
            con.addRequestHeader("Content-Length", String.valueOf(payload.length()));
            con.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            con.addRequestHeader("Host", "auth.bycs.de");
            con.addRequestHeader("Origin", "null");
            NetworkManager.getInstance().addToQueueAndWait(con);           
            
//          get Sesskey            
            try (
                InputStream isSesskey = new ByteArrayInputStream(con.getResponseData())) {
                isSesskey.skip(1100);
                while(true) {
                    if((char) isSesskey.read() == '\n') break;
                }
                isSesskey.skip(162);
                for(int i = 0; i < 10; i++) {
                    sesskey += (char) isSesskey.read();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public String[] getCourses() {
//      get course info
        String url = "https://lernplattform.mebis.bycs.de/lib/ajax/service.php?sesskey=" + sesskey + "&info=core_course_get_enrolled_courses_by_timeline_classification";
        String payload = "[{\"index\":0,\"methodname\":\"core_course_get_enrolled_courses_by_timeline_classification\",\"args\":{\"offset\":0,\"limit\":0,\"classification\":\"all\",\"sort\":\"fullname\",\"customfieldname\":\"schule\",\"customfieldvalue\":\"\",\"requiredfields\":[\"id\",\"fullname\",\"shortname\",\"showcoursecategory\",\"showshortname\",\"visible\",\"enddate\"]}}]";
            
        ConnectionRequest conCourses = new ConnectionRequest();
        conCourses.setUrl(url);
        conCourses.setPost(true);
        conCourses.setRequestBody(payload);
        conCourses.addRequestHeader("Content-Length", String.valueOf(payload.length()));
        conCourses.addRequestHeader("Cookie", moodleSession);
        NetworkManager.getInstance().addToQueueAndWait(conCourses);

        try {
            JSONArray courses = new JSONArray(new String(conCourses.getResponseData()))
                    .getJSONObject(0)
                    .getJSONObject("data")
                    .getJSONArray("courses");
            String[] ids = new String[courses.length()];
            for(int i = 0; i < courses.length(); i++) {
                ids[i] = courses.getJSONObject(i).getString("id");
            }
            return ids;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public void getCourse(String id) {
//      get info for set course
        String url = "https://lernplattform.mebis.bycs.de/lib/ajax/service.php?sesskey=" + sesskey + "&info=core_courseformat_get_state";
        String payload = "[{\"index\":0,\"methodname\":\"core_courseformat_get_state\",\"args\":{\"courseid\":" + id + "}}]";
        
        ConnectionRequest conCourse = new ConnectionRequest();
        conCourse.setUrl(url);
        conCourse.setPost(true);
        conCourse.setRequestBody(payload);
        conCourse.addRequestHeader("Content-Length", String.valueOf(payload.length()));
        conCourse.addRequestHeader("Cookie", moodleSession);
        NetworkManager.getInstance().addToQueueAndWait(conCourse);
                        
        String courseJsonString = new String(conCourse.getResponseData());
        //TODO for later
    }
}
