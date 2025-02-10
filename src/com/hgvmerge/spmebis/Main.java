package com.hgvmerge.spmebis;

import com.codename1.io.Cookie;
import com.codename1.ui.CN;

public class Main {

    public void init(Object context) {
        CN.updateNetworkThreadCount(2);
        Cookie.clearCookiesFromStorage();
        Schuelerportal sp = new Schuelerportal("email", "password");
                
        Mebis mebis = new Mebis("username", "password");
        mebis.getCourses();

        Backend.getInstance().updateChats();
    }
    
    public void start() {

    }

    public void stop() {

    }
    
    public void destroy() {
        
    }

}
