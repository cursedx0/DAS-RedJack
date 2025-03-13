package com.example.entrega1;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class HistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textHistory = findViewById(R.id.textHistory);
        textHistory.setMovementMethod(new ScrollingMovementMethod());
        textHistory.setText(leerHistorial());

        Button buttonAtras = findViewById(R.id.buttonAtras);
        buttonAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String leerHistorial() { //código similar al de play_activity, pero sin la parte de escritura claro
        StringBuilder historial = new StringBuilder();
        try {
            FileInputStream fis = openFileInput(FILE_NAME);
            BufferedReader lector = new BufferedReader(new InputStreamReader(fis));
            String linea;
            while ((linea = lector.readLine()) != null) {
                historial.append(linea).append("\n");
            }
            lector.close();
        } catch (IOException e) {
            historial.append("No hay historial disponible.");
        }
        return historial.toString();
    }
}