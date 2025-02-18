package com.hgvmerge.spmebis;

import ca.weblite.codename1.json.JSONArray;
import ca.weblite.codename1.json.JSONException;
import ca.weblite.codename1.json.JSONObject;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.NetworkManager;
import com.codename1.l10n.ParseException;
import java.util.HashSet;

public class Holidays {
    
    // year the school year started in
    public static int getSchoolYear() {
        Time time = new Time().now();
        int year = (int) time.get(Time.YEARS);
        return time.get(Time.MONTHS) < 9 ? year - 1 : year;
    }
    
    public static JSONObject downloadHolidays() {
        try {
            JSONObject vacations = new JSONObject("{\"holidays\":[],\"fromTo\":[]}");
            
            int year = getSchoolYear();
            ConnectionRequest con = new ConnectionRequest();
            con.setPost(false);
            
            int holidayIndex = 0;
            
            Time start, end;
            
            con.setUrl("https://openholidaysapi.org/SchoolHolidays?countryIsoCode=DE&subdivisionCode=DE-BY&languageIsoCode=DE&validFrom=" + year + "-09-01&validTo=" + (year + 1) + "-8-31");        
            NetworkManager.getInstance().addToQueueAndWait(con);
                        
            JSONArray arrSchool = new JSONArray(new String(con.getResponseData()));
            for(int i = 0; i < arrSchool.length(); i++) {
                JSONObject obj = arrSchool.getJSONObject(i);
                start = new Time(obj.getString("startDate"));
                end = new Time(obj.getString("endDate"));
                int holidayStart = holidayIndex;
                while(start.compare("before/equals", end)) {
                    vacations.accumulate("holidays", start.format());
                    start.add(Time.DAYS, 1);
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
        } catch (JSONException ex) {
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
