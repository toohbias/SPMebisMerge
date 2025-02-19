package com.hgvmerge.spmebis;

public class Main {

    public void init(Object context) {
//        Backend.getInstance().updateChats();
//        Backend.getInstance().updateUnterrichtAndHausaufgaben();
    }
    
    public void start() {

    }

    public void stop() {

    }
    
    public void destroy() {
        StorageManager.saveLastUsed(new Time().now());
    }

}
