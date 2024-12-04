package com.example.contactformapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class ContactsDB extends SQLiteOpenHelper {
    public ContactsDB(Context context) {
        super(context, "ContactDB.db", null, 2); // Increment the version number
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE contacts  ("
                + "ID TEXT PRIMARY KEY,"
                + "name TEXT,"
                + "email TEXT,"
                + "homePhn TEXT,"
                + "officePhone TEXT,"  // Add this line
                + "img TEXT"
                + ")";
        db.execSQL(sql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add the "officePhone" column to the existing table
            db.execSQL("ALTER TABLE contacts ADD COLUMN officePhone TEXT");
        }
    }
    public void insertContact(String id, String name, String email, String homePhn, String officePhone, String img) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cols = new ContentValues();
        cols.put("ID", id);
        cols.put("name", name);
        cols.put("email", email);
        cols.put("homePhn", homePhn);
        cols.put("officePhone", officePhone);  // Make sure "officePhone" column is included
        cols.put("img", img);

        try {
            long result = db.insertOrThrow("contacts", null, cols);
            if (result == -1) {
                // Failed to insert data
                db.insert("contacts", null ,  cols);
                // Handle the error as needed, e.g., log an error message or throw an exception
            } else {
                // Data inserted successfully
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the SQLiteException here, e.g., log an error message
        } finally {
            db.close();
        }
    }


    public void updateContact(String id, String name, String email, String homePhn, String officePhone, String img) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cols = new ContentValues();
        cols.put("ID", id); // Use "ID" instead of "id"
        cols.put("name", name);
        cols.put("email", email);
        cols.put("homePhn", homePhn);
        cols.put("officePhone", officePhone);
        cols.put("img", img);
        db.update("contacts", cols, "ID=?", new String[ ] {id} ); // Use "ID" instead of "id"
        db.close();
    }

    public void deleteContacts(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("contacts", "id=?", new String[ ] {id} );
        db.close();
    }
    public Cursor selectContacts(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res=null;
        try {
            res = db.rawQuery(query, null);
        } catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }
}