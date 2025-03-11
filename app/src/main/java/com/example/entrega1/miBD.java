package com.example.entrega1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Nullable;

public class miBD extends SQLiteOpenHelper {
    public miBD(@Nullable Context context, @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Usuarios ('Id' INTEGER PRIMARY KEY " + "AUTOINCREMENT NOT NULL, 'Nombre' VARCHAR(255), 'Pw' VARCHAR(255), 'Coins' INTEGER)");
        db.execSQL("INSERT INTO Usuarios (Nombre, Pw, Coins) VALUES ('user', 'pw', 100000)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
