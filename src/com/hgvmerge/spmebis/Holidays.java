package com.hgvmerge.spmebis;

import ca.weblite.codename1.json.JSONArray;
import ca.weblite.codename1.json.JSONException;
import ca.weblite.codename1.json.JSONObject;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.NetworkManager;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class Holidays {
    
    // year the school year started in
    public static int getSchoolYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return cal.get(Calendar.MONTH) < 9 ? year - 1 : year;
    }
    
    public static JSONObject downloadHolidays() {
        try {
            JSONObject vacations = new JSONObject("{\"holidays\":[],\"fromTo\":[]}");
            
            int year = getSchoolYear();
            ConnectionRequest con = new ConnectionRequest();
            con.setPost(false);
            
            int holidayIndex = 0;
            
            Date start, end;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            con.setUrl("https://openholidaysapi.org/SchoolHolidays?countryIsoCode=DE&subdivisionCode=DE-BY&languageIsoCode=DE&validFrom=" + year + "-09-01&validTo=" + (year + 1) + "-8-31");        
            NetworkManager.getInstance().addToQueueAndWait(con);
                        
            JSONArray arrSchool = new JSONArray(new String(con.getResponseData()));
            for(int i = 0; i < arrSchool.length(); i++) {
                JSONObject obj = arrSchool.getJSONObject(i);
                start = sdf.parse(obj.getString("startDate"));
                end = sdf.parse(obj.getString("endDate"));
                int holidayStart = holidayIndex;
                while(start.getTime() <= end.getTime()) {
                    vacations.accumulate("holidays", sdf.format(start));
                    start.setTime(start.getTime() + (1000 * 60 * 60 * 24));
                    holidayIndex++;
                }
                vacations.accumulate("fromTo", new JSONObject("{\"" + obj.getJSONArray("name").getJSONObject(0).getString("text") + "\":{\"fromIndex\":" + holidayStart + ",\"toIndex\":" + (holidayIndex - 1) + "}}"));
            }
            
            con.setUrl("https://openholidaysapi.org/PublicHolidays?countryIsoCode=DE&subdivisionCode=DE-BY&languageIsoCode=DE&validFrom=" + year + "-09-01&validTo=" + (year + 1) + "-8-31");
            NetworkManager.getInstance().addToQueueAndWait(con);
            
            JSONArray arrPublic = new JSONArray(new String(con.getResponseData()));
            for(int i = 0; i < arrPublic.length(); i++) {
                JSONObject obj = arrPublic.getJSONObject(i);
                if(!"Local".equals(obj.getString("regionalScope"))) {
                    vacations.accumulate("holidays", obj.getString("startDate"));
                    vacations.accumulate("fromTo", new JSONObject("{\"" + obj.getJSONArray("name").getJSONObject(0).getString("text") + "\":{\"fromIndex\":" + holidayIndex + ",\"toIndex\":" + holidayIndex + "}}"));
                    holidayIndex++;
                }
            }
            
            System.out.println("success");
            return vacations;
        } catch (JSONException | ParseException ex) {
            ex.printStackTrace();
            System.out.println("failed");
            return null;
        }
    }
    
    public static HashSet<String> getSchoolFreeDays(JSONObject holidays) {
        HashSet<String> result = new HashSet<>();
        try {
            JSONArray days = holidays.getJSONArray("holidays");
            for(int i = 0; i < days.length(); i++) {
                result.add(days.getString(i));
            } 
        }   catch (JSONException ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
}
