package com.example.entrega1;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        miBD GestorDB = new miBD(this,"miBD",null,1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();

        /*
        ContentValues nuevo = new ContentValues();
        nuevo.put("Nombre", "user");
        nuevo.put("Pw", "pw");
        nuevo.put("Coins", 100000);
        bd.insert("Usuarios", null, nuevo);
        */

        /*Cursor testCursor = bd.rawQuery("SELECT * FROM Usuarios", null);
        if (testCursor.moveToFirst()) {
            do {
                Log.i("DEBUG_DB", "Usuario: " + testCursor.getString(1) + ", Pw: " + testCursor.getString(2) + ", Coins: " + testCursor.getInt(3));
            } while (testCursor.moveToNext());
        } else {
            Log.e("DEBUG_DB", "No se encontraron usuarios en la base de datos.");
        }
        testCursor.close();

         */

        //RecyclerView lalista= findViewById(R.id.elRecyclerView);

        //int[] portadas= {R.drawable.mcicon1000x1000, R.drawable.mkicon413x431};
        //String[] titulos={"Minecraft PE","Mario Kart Tour"};
        //ElAdaptadorRecycler eladaptador = new ElAdaptadorRecycler(titulos,portadas);
        //lalista.setAdapter(eladaptador);

        //LinearLayoutManager elLayoutLineal= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        //lalista.setLayoutManager(elLayoutLineal);



        View button = findViewById(R.id.buttonEntrar);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText iuser = findViewById(R.id.inputUser);
                String user = iuser.getText().toString();
                EditText ipw = findViewById(R.id.inputPw);
                String pw = ipw.getText().toString();

                Cursor c = bd.rawQuery("SELECT * FROM Usuarios WHERE Nombre=? AND Pw=?", new String[]{user, pw});
                if (c.getCount()==1 && c.moveToFirst()){
                    int userid = c.getInt(0);
                    int usercoins = c.getInt(3);
                    Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                    intent.putExtra("id", userid);
                    intent.putExtra("name", user);
                    intent.putExtra("coins", usercoins);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.loginincorrecto), Toast.LENGTH_SHORT).show();
                }
                c.close();
            }
        });

    }
}