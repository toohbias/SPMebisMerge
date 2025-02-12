package com.hgvmerge.spmebis;

import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.db.Row;
import com.codename1.io.Util;
//import com.codename1.ui.CN;
import com.codename1.ui.Display;
//import com.sun.org.apache.bcel.internal.generic.Select;
import java.io.IOException;
import java.util.ArrayList;
//import java.io.InputStream;
//import java.io.OutputStream;

public class DB {
    
    private final String name = "DATA.db";
    
    public static final String TYPE_INTEGER = "integer";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_REAL = "real";
    public static final String TYPE_TEXT = "string";
    public static final String TYPE_BLOB = "blob";
    
    public static final String TABLE_MESSAGE = "Message";
    public static final String TABLE_CHAT = "Chat";
    public static final String TABLE_FILE = "File";
    public static final String TABLE_TERMIN = "Termin";
    public static final String TABLE_HAUSAUFGABE = "Hausaufgabe";
    public static final String TABLE_UNTERRICHT = "Unterrichtsinhalt";
    public static final String TABLE_COURSE = "Course";
    public static final String TABLE_SECTION = "Section";
    public static final String TABLE_MODULE = "Module";
    
    
    public DB() {
        Database db = null;
        try {
            if(!Database.exists(name)) {
                db = Database.openOrCreate(name);
                for(String table : createTables) {
                    db.execute(table);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            Util.cleanup(db);
        }        
    }
    
    public void addEntry(String table, String... columns) {
       Database db = null;
        try {
            db = Database.openOrCreate(name);
            String values = "";
            for(String col : columns) {
                values += ",";
                values += col;
            }
            values = values.substring(1);
            db.execute("INSERT INTO " + table + " VALUES(" + values + ")");
        } catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            Util.cleanup(db);
        }
    }
    
    public void updateEntry(String table, String primaryID, String column, String value) {
        Database db = null;
        try {
            db = Database.openOrCreate(name);
            db.execute("UPDATE " + table + " SET " + column + " = " + value + " WHERE ID = " + primaryID);
        } catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            Util.cleanup(db);
        }
    }
    
    public <T> ArrayList<T> getSingleFieldFromPrompt(String query, String type) {
        Database db = null;
        Cursor cursor = null;
        try {
            db = Database.openOrCreate(name);
            cursor = db.executeQuery(query);
            
            ArrayList result;
            switch(type) {
                case TYPE_INTEGER: result = new ArrayList<Integer>(); break;
                case TYPE_LONG: result = new ArrayList<Long>(); break;
                case TYPE_REAL: result = new ArrayList<Double>(); break;
                case TYPE_TEXT: result = new ArrayList<String>(); break;
                case TYPE_BLOB: result = new ArrayList<Byte[]>(); break;
                default: return null;
            }
            boolean next = cursor.next();
            while(next) {
                Row current = cursor.getRow();
                switch(type) {
                    case TYPE_INTEGER: result.add(current.getInteger(0)); break;
                    case TYPE_LONG: result.add(current.getLong(0)); break;
                    case TYPE_REAL: result.add(current.getDouble(0)); break;
                    case TYPE_TEXT: result.add(current.getString(0)); break;
                    case TYPE_BLOB: result.add(current.getBlob(0)); break;
                    default: return null;
                }
                next = cursor.next();
            }
            return result;
        } catch(IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            Util.cleanup(db);
            Util.cleanup(cursor);
        }
    }
    
    public Integer getIntValue(String query) {
        Database db = null;
        Cursor cursor = null;
        try {
            db = Database.openOrCreate(name);
            cursor = db.executeQuery(query);
            if(cursor.next()) {
                return cursor.getRow().getInteger(0);
            }
            return null;
        } catch(IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            Util.cleanup(db);
            Util.cleanup(cursor);
        }
    }
    
    public Long getLongValue(String query) {
        Database db = null;
        Cursor cursor = null;
        try {
            db = Database.openOrCreate(name);
            cursor = db.executeQuery(query);
            if(cursor.next()) {
                return cursor.getRow().getLong(0);
            }
            return null;
        } catch(IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            Util.cleanup(db);
            Util.cleanup(cursor);
        }
    }
  
    private final String[] createTables = {"CREATE TABLE Message(ID INTEGER NOT NULL PRIMARY KEY, message TEXT, creator TEXT, date INTEGER, chatID INTEGER NOT NULL, fileID INTEGER, FOREIGN KEY(chatID) REFERENCES Chat(ID), FOREIGN KEY(fileID) REFERENCES File(ID));",
                                           "CREATE TABLE Chat(ID INTEGER NOT NULL PRIMARY KEY, name TEXT, owner TEXT, schoolyear INTEGER, lastmessagedate INTEGER);",
                                           "CREATE TABLE File(ID INTEGER NOT NULL PRIMARY KEY, url TEXT, name TEXT);",
                                           "CREATE TABLE Termin(ID INTEGER NOT NULL PRIMARY KEY, class TEXT, subject TEXT, date TEXT, type TEXT);",
                                           "CREATE TABLE Hausaufgabe(ID INTEGER NOT NULL PRIMARY KEY, class TEXT, message TEXT, teacher TEXT, date TEXT, due TEXT, files TEXT, completed INTEGER, unterrichtID INTEGER, FOREIGN KEY(unterrichtID) REFERENCES Unterrichtsinhalt(ID));",
                                           "CREATE TABLE Unterrichtsinhalt(ID INTEGER NOT NULL PRIMARY KEY, class TEXT, message TEXT, teacher TEXT, date TEXT, hour TEXT, files TEXT, hausaufgabeID INTEGER, FOREIGN KEY(hausaufgabeID) REFERENCES Hausaufgabe(ID));",
                                           "CREATE TABLE Course(ID INTEGER NOT NULL PRIMARY KEY, schoolyear INTEGER, name TEXT, sectionIDs TEXT, FOREIGN KEY(sectionIDs) REFERENCES Section(ID));",
                                           "CREATE TABLE Section(ID INTEGER NOT NULL PRIMARY KEY, number INTEGER, title TEXT, moduleIDs TEXT, FOREIGN KEY(moduleIDs) REFERENCES Module(ID));",
                                           "CREATE TABLE Module(ID INTEGER NOT NULL PRIMARY KEY, type TEXT, data TEXT, url TEXT);" };
    
}