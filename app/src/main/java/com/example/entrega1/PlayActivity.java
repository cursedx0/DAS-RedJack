package com.example.entrega1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayActivity extends BaseActivity {

    private boolean jugandoFlag = false;
    private int saldo;
    private int id;
    private String nombre;
    private int puntos;
    private int[] posiblespuntos;
    private int[] posiblesdealer;
    private int totalElegido = 2; //0: total 1 (más bajo), 1: total 2 (más alto), 2: desactivado
    private boolean turnoJugador = false;
    private Baraja miBaraja = new Baraja();
    private int apuesta;
    private miBD GestorDB;
    private SQLiteDatabase bd;
    private ElAdaptadorRecycler rvadapterJugador;
    private ElAdaptadorRecycler rvadapterDealer;
    private int numAses = 0;


    //===== BOTONES =======
    private Button buttonApostar;
    private Button buttonPlantarse;
    private Button buttonCarta;
    private Button buttonTotal1;
    private Button buttonTotal2;
    private Button buttonReplay;
    private Button buttonCompartir;

    //===== TextViews ======
    private TextView tuapuesta;
    private TextView tusaldo;
    private TextView textPuntosJugador;
    private TextView textPuntosDealer;


    //===== Edittext ======
    EditText iapuesta;

    //===== MenuItem ======
    private MenuItem buttonPrefs;

    private boolean enablePrefs = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);
        setSupportActionBar(findViewById(R.id.toolbar));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //===== Notificación ========\\
        if(NOTIS_LOGIN){
            Context context = getApplicationContext(); // 🔹 Asegurar que tenemos un contexto válido
            NotificationManager elManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (elManager == null) {
                Log.e("Notificación", "NotificationManager es null. No se puede crear la notificación.");
                return;
            }

            String canalID = "LogIn";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel elCanal = new NotificationChannel(
                        canalID, "Canal de Inicio de Sesión",
                        NotificationManager.IMPORTANCE_HIGH
                );
                elCanal.setDescription("Notificación recibida al iniciar sesión.");
                elCanal.setVibrationPattern(new long[]{0, 500, 500, 500});
                elCanal.enableVibration(true);

                elManager.createNotificationChannel(elCanal); // 🔹 Crear el canal antes de usarlo
            }

            // 🔹 Se necesita un ícono obligatorio en Android 8+ o la notificación fallará
            NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(context, canalID)
                    .setSmallIcon(R.drawable.card_b_da_large)
                    .setContentTitle("Inicio de sesión exitoso")
                    .setContentText("Has iniciado sesión correctamente.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            elManager.notify(1, elBuilder.build());
        }

        //===== inic UI =====\\
        buttonApostar = findViewById(R.id.buttonApostar);
        buttonPlantarse = findViewById(R.id.buttonPlantarse);
        buttonCarta = findViewById(R.id.buttonCarta);
        buttonTotal1 = findViewById(R.id.buttonTotal1);
        buttonTotal2 = findViewById(R.id.buttonTotal2);
        buttonReplay = findViewById(R.id.buttonReplay);
        buttonCompartir = findViewById(R.id.buttonCompartir);

        tuapuesta = findViewById(R.id.textApuesta);
        tusaldo = findViewById(R.id.textSaldo);
        textPuntosJugador = findViewById(R.id.textPuntosJugador);
        textPuntosDealer = findViewById(R.id.textPuntosDealer);

        iapuesta = findViewById(R.id.inputApuesta);

        GestorDB = new miBD(this,"miBD",null,1);
        bd = GestorDB.getWritableDatabase();
        rvadapterJugador = new ElAdaptadorRecycler(miBaraja.getManoJugador());
        rvadapterDealer = new ElAdaptadorRecycler(miBaraja.getManoDealer());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            saldo = extras.getInt("coins");
            id = extras.getInt("id");
            nombre = extras.getString("name");
        }else{
            saldo = 0;
            //si esto ocurre, el programa está condenado a fallar de todas formas
        }

        RecyclerView rvJugador = findViewById(R.id.rvJugador);
        RecyclerView rvDealer = findViewById(R.id.rvDealer);

        rvJugador.setAdapter(rvadapterJugador);
        LinearLayoutManager elLayoutLineal= new LinearLayoutManager(getBaseContext(),LinearLayoutManager.HORIZONTAL,false);
        rvJugador.setLayoutManager(elLayoutLineal);

         rvDealer.setAdapter(rvadapterDealer);
        LinearLayoutManager elLayoutLinealDealer= new LinearLayoutManager(getBaseContext(),LinearLayoutManager.HORIZONTAL,false);
        rvDealer.setLayoutManager(elLayoutLinealDealer);

        tusaldo.setText(getString(R.string.tusaldo)+": "+Integer.toString(saldo));

        buttonApostar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText iapuesta = findViewById(R.id.inputApuesta);
                if (!String.valueOf(iapuesta.getText()).equals("")) {
                    apuesta = Integer.parseInt(String.valueOf(iapuesta.getText()));
                    if (apuesta < 1) {
                        Toast.makeText(getApplicationContext(), getString(R.string.apuestainvalida), Toast.LENGTH_SHORT).show();
                    } else if (apuesta > saldo) {
                        Toast.makeText(getApplicationContext(), getString(R.string.saldoinsuficiente), Toast.LENGTH_SHORT).show();
                    } else {
                        //Quitar el teclado si está "sacado"
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        View currentFocus = getCurrentFocus();
                        if (currentFocus != null && imm != null) {
                            //oOcultar el teclado
                            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                        }
                        empezarPartida();
                    }
                }
            }
        });


        //listener carta
        buttonCarta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int posicion = miBaraja.getManoJugador().size(); // Antes de repartir
                miBaraja.repartirJugador();
                rvadapterJugador.notifyItemInserted(posicion); // Solo notifica la nueva carta
                posiblespuntos = miBaraja.calcMano(miBaraja.getManoJugador());
                comprobarAses();
            }
        });
        buttonTotal1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalElegido = 0;
                puntos = posiblespuntos[0];
                buttonTotal1.setVisibility(View.INVISIBLE);
                buttonTotal2.setVisibility(View.INVISIBLE);
                numAses = numAses + 1;
                comprobarAses();
            }
        });

        buttonTotal2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalElegido = 1;
                puntos = posiblespuntos[1];
                buttonTotal1.setVisibility(View.INVISIBLE);
                buttonTotal2.setVisibility(View.INVISIBLE);
                numAses = numAses + 1;
                comprobarAses();
            }
        });

        buttonPlantarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnoJugador = false;
                jugarDealer();
            }
        });

        buttonReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finDelJuego();
            }
        });

        buttonCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compartirResultado(puntos);
            }
        });
    }


    private void empezarPartida() {
        jugandoFlag = true;
        totalElegido = 2;
        numAses = 0;
        //======= UI ========\\
        buttonPrefs.setEnabled(false);
        tusaldo.setVisibility(View.INVISIBLE);
        findViewById(R.id.fragmentContainer).setVisibility(View.INVISIBLE);
        enablePrefs = false;

        // 1. SE RESTA LA APUESTA DEL SALDO
        saldo = saldo - apuesta;
        ContentValues modificacion = new ContentValues();
        modificacion.put("Coins",Integer.toString(saldo));
        bd.update("Usuarios", modificacion, "Id=?",new String[]{Integer.toString(id)});
        // 2. IU Y SE REPARTEN CARTAS
        buttonApostar.setVisibility(View.INVISIBLE);

        iapuesta.setVisibility(View.INVISIBLE);
        tuapuesta.setText(getString(R.string.tuapuesta)+ ": " + Integer.toString(apuesta));

        miBaraja.barajar();

        //testing
        //miBaraja.getMazo().add(0,"ca");
        //miBaraja.getMazo().add(0,"ck");
        //miBaraja.getMazo().add(0,"ck");
        //miBaraja.getMazo().add(0,"ca");

        int posicionInicial = miBaraja.getManoJugador().size();
        miBaraja.repartirJugador();
        miBaraja.repartirJugador();

        rvadapterJugador.notifyItemRangeInserted(posicionInicial, 2);

        // 2.2 ESCUCHAR EVENTOS DE CARTA Y PLANTARSE + UI
        buttonPlantarse.setVisibility(View.VISIBLE);
        buttonCarta.setVisibility(View.VISIBLE);

        posiblespuntos = miBaraja.calcMano(miBaraja.getManoJugador());
        comprobarAses();

        //jugarJugador();
    }

    private void jugarJugador(){
        turnoJugador = true;
        totalElegido = 2;

        while(turnoJugador){
            posiblespuntos = miBaraja.calcMano(miBaraja.getManoJugador());
            if (posiblespuntos[1] != 0) {
                if (totalElegido==2) {
                    //se deberá elegir una puntuación antes de plantarse
                    buttonPlantarse.setEnabled(false);
                    textPuntosJugador.setText(getString(R.string.selecPuntuacion));
                }
            }else{
                puntos = posiblespuntos[0];
            }
        }
    }

    private void comprobarAses() {
        puntos = posiblespuntos[0];
        if (totalElegido!=2){
            puntos = posiblespuntos[totalElegido];
        }
        textPuntosJugador.setText(getString(R.string.tupuntuacion) + ": " + puntos);
        if (posiblespuntos[0]>21 || puntos>21) {
            jugadorPierde();
        }else{
            buttonTotal1.setText("" + posiblespuntos[0]);
            buttonTotal2.setText("" + posiblespuntos[1]);
            if (posiblespuntos[2] != 0) { //si hay ases

                if (totalElegido == 2 || (posiblespuntos[2] != numAses && totalElegido == 0)) { //si no se ha elegido puntuación
                    //se deberá elegir una puntuación antes de plantarse
                    buttonPlantarse.setEnabled(false); //desactivar boton de plantarse
                    buttonTotal1.setVisibility(View.VISIBLE);
                    buttonTotal2.setVisibility(View.VISIBLE);
                    //buttonTotal1.setText("" + posiblespuntos[0]);
                    //buttonTotal2.setText("" + posiblespuntos[1]);
                    textPuntosJugador.setText(getString(R.string.selecPuntuacion));
                } else {
                    buttonPlantarse.setEnabled(true); //activar boton de plantarse
                    buttonTotal1.setVisibility(View.INVISIBLE);
                    buttonTotal2.setVisibility(View.INVISIBLE);
                    textPuntosJugador.setText(Integer.toString(puntos)); //innecesario?
                    puntos = posiblespuntos[totalElegido];
                    textPuntosJugador.setText(getString(R.string.tupuntuacion) + ": " + puntos);
                }
            } else {
                puntos = posiblespuntos[0];
                textPuntosJugador.setText(getString(R.string.tupuntuacion) + ": " + puntos);
            }
            textPuntosJugador.setVisibility(View.VISIBLE);
        }
    }

    private void jugarDealer(){
        //ocultar controles de jugador
        buttonPlantarse.setVisibility(View.INVISIBLE);
        buttonCarta.setVisibility(View.INVISIBLE);

        miBaraja.repartirDealer();
        int posicion = miBaraja.getManoDealer().size(); // Antes de repartir
        rvadapterDealer.notifyItemInserted(posicion); // Solo notifica la nueva carta
        posiblesdealer = miBaraja.calcMano(miBaraja.getManoDealer());
        textPuntosDealer.setVisibility(View.VISIBLE);

        if(posiblesdealer[0]!=posiblesdealer[1]){
            textPuntosDealer.setText(getString(R.string.dealer)+": "+posiblesdealer[0]+" "+getString(R.string.o)+" "+posiblesdealer[1]);
        }else{
            textPuntosDealer.setText(getString(R.string.dealer)+": "+posiblesdealer[0]);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Código a ejecutar después de 2 segundos
                if (posiblesdealer[0]>21){
                    jugadorGana();
                }else if ((posiblesdealer[0]>puntos) || (posiblesdealer[1]>puntos && posiblesdealer[1]<22)){
                    jugadorPierde();
                }else if ((posiblesdealer[0]==21 || posiblesdealer[1]==21) && puntos==21) {
                    empate();
                }else{
                    jugarDealer();
                }
            }
        }, 2000);

    }

    private void finDelJuego(){
        miBaraja.getManoJugador().clear();
        miBaraja.getManoDealer().clear();
        rvadapterJugador.notifyDataSetChanged();
        rvadapterDealer.notifyDataSetChanged();

        textPuntosJugador.setVisibility(View.INVISIBLE);
        textPuntosDealer.setVisibility(View.INVISIBLE);
        buttonPlantarse.setVisibility(View.INVISIBLE);
        buttonTotal1.setVisibility(View.INVISIBLE);
        buttonTotal2.setVisibility(View.INVISIBLE);
        buttonCarta.setVisibility(View.INVISIBLE);
        buttonCompartir.setVisibility(View.INVISIBLE);
        buttonReplay.setVisibility(View.INVISIBLE);

        //hacer aparecer:

        tusaldo.setVisibility(View.VISIBLE);
        iapuesta.setVisibility(View.VISIBLE);
        buttonApostar.setVisibility(View.VISIBLE);

        enablePrefs = true;
    }

    private void finComun(){
        jugandoFlag = false;

        buttonReplay.setVisibility(View.VISIBLE);
        buttonCompartir.setVisibility(View.VISIBLE);

        textPuntosJugador.setText(getString(R.string.tupuntuacion) + ": " + puntos);
        buttonCarta.setVisibility(View.INVISIBLE);
        buttonPlantarse.setVisibility(View.INVISIBLE);
        buttonTotal1.setVisibility(View.INVISIBLE);
        buttonTotal2.setVisibility(View.INVISIBLE);
    }

    private void jugadorPierde(){
        finComun();
        EndDialog endDialog = new EndDialog(0, saldo, apuesta);
        endDialog.show(getSupportFragmentManager(), "lost_dialog");
        tusaldo.setText(getString(R.string.tusaldo)+": "+saldo);
        //el dinero se quita al empezar la partida
    }

    private void jugadorGana(){
        finComun();
        saldo = saldo + apuesta*2;
        EndDialog endDialog = new EndDialog(1, saldo, apuesta);
        endDialog.show(getSupportFragmentManager(), "win_dialog");
        //dar dinero
        tusaldo.setText(getString(R.string.tusaldo)+": "+saldo);
        ContentValues modificacion = new ContentValues();
        modificacion.put("Coins",Integer.toString(saldo));
        bd.update("Usuarios", modificacion, "Id=?",new String[]{Integer.toString(id)});
    }

    private void empate(){
        finComun();
        saldo = saldo + apuesta;
        EndDialog endDialog = new EndDialog(2, saldo, apuesta);
        endDialog.show(getSupportFragmentManager(), "draw_dialog");
        tusaldo.setText(getString(R.string.tusaldo)+": "+saldo);
        ContentValues modificacion = new ContentValues();
        modificacion.put("Coins",Integer.toString(saldo));
        bd.update("Usuarios", modificacion, "Id=?",new String[]{Integer.toString(id)});
    }

    private void compartirResultado(int resultado) {
        String msg = getString(R.string.recomendacion);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg+" "+getString(R.string.tupuntuacion)+": "+resultado);

        // Verifica que haya una app que pueda manejar el Intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.compartir)));
        }
    }

    private final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(jugandoFlag) {
                ExitDialog exitDialog = new ExitDialog();
                exitDialog.show(getSupportFragmentManager(), "exit_dialog");
            }else{
                setEnabled(false);
                onBackPressed();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Blackjack"); // Personalizar título
        }
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        buttonPrefs = menu.findItem(R.id.prefs);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!enablePrefs) {
            SpannableString s = new SpannableString(getString(R.string.prefsDeshabilitadas));
            s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
            menu.findItem(R.id.prefs).setEnabled(false)
                    .setTitle(s);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.prefs:{
                Preferencias prefs = new Preferencias();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
                transaction.replace(R.id.fragmentContainer, prefs);
                transaction.addToBackStack(null); // Para poder regresar al fragmento anterior
                transaction.commit();
                break;
            }
            case R.id.logout:{
                if(jugandoFlag){
                    ExitDialog exitDialog = new ExitDialog();
                    exitDialog.show(getSupportFragmentManager(), "exit_dialog2");
                }else{
                    finish();
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}

