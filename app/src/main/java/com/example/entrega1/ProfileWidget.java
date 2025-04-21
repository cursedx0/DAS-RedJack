package com.example.entrega1;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class ProfileWidget extends AppWidgetProvider {
    private static int v;
    private static int d;
    private static int e;
    private static int s;
    private static String nombre;
    private static int count = 0;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.profile_widget);

        views.setTextViewText(R.id.textWVicts,context.getString(R.string.victorias));
        views.setTextViewText(R.id.textWDerrs,context.getString(R.string.derrotas));
        views.setTextViewText(R.id.textWEmpts,context.getString(R.string.empates));
        views.setTextViewText(R.id.textWSaldo,context.getString(R.string.monedas));
        views.setTextViewText(R.id.buttonWJugar,context.getString(R.string.jugar));

        if(v!=-1) {
            views.setTextViewText(R.id.textWUser, nombre);
            views.setTextViewText(R.id.iWVicts, v + "");
            views.setTextViewText(R.id.iWDerrs, d + "");
            views.setTextViewText(R.id.iWEmpts, e + "");
            views.setTextViewText(R.id.iWSaldo, s + "");
        }else{ //carga desde las preferencias
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            String nombre2 = prefs.getString("widget_nombre", "Desconocido");
            int v2 = prefs.getInt("widget_v", 0);
            int d2 = prefs.getInt("widget_d", 0);
            int e2 = prefs.getInt("widget_e", 0);
            int s2 = prefs.getInt("widget_s", 0);

            views.setTextViewText(R.id.textWUser, nombre2);
            views.setTextViewText(R.id.iWVicts, v2 + "");
            views.setTextViewText(R.id.iWDerrs, d2 + "");
            views.setTextViewText(R.id.iWEmpts, e2 + "");
            views.setTextViewText(R.id.iWSaldo, s2 + "");
        }

        //botón para abrir la app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.buttonWJugar, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        count = count +1;
        Log.d("COUNT",count+"");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        nombre = prefs.getString("nombre", "ERROR");

        String idioma = prefs.getString("idioma", "es"); // Valor por defecto: español

        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale); // A partir de API 17

        Context contextIdioma = context.createConfigurationContext(config);
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            //cambiar texto dinámicamente


            if(!nombre.isEmpty()) {
                Data datos = new Data.Builder()
                        //.putString("url","1") //url a php gestor de monedas
                        .putString("accion", "info") //obtiene monedas de usuario
                        .putString("usuario", nombre)
                        .build();

                OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(conexionBDWebService.class)
                        .setInputData(datos)
                        .build();

                WorkManager.getInstance(context).enqueue(request);

                LiveData<WorkInfo> liveData = WorkManager.getInstance(context)
                        .getWorkInfoByIdLiveData(request.getId());

                //crear observer
                Observer<WorkInfo> observer = new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                String mensaje = workInfo.getOutputData().getString("message");
                                Log.d("WORKER", "¡200! " + mensaje);
                                String code = workInfo.getOutputData().getString("code");
                                if ("0".equals(code)) {
                                    Log.d("OBTENER INFO WIDGET", "ÉXITO");
                                    v = workInfo.getOutputData().getInt("victs",-1);
                                    d = workInfo.getOutputData().getInt("derrs",-1);
                                    e = workInfo.getOutputData().getInt("empts",-1);
                                    s = workInfo.getOutputData().getInt("monedas",-1);

                                    SharedPreferences.Editor editor = prefs.edit();

                                    editor.putString("widget_nombre", nombre);
                                    editor.putInt("widget_v", v);
                                    editor.putInt("widget_d", d);
                                    editor.putInt("widget_e", e);
                                    editor.putInt("widget_s", s);

                                    editor.apply();

                                    //actualizar
                                    //updateAppWidget(context, appWidgetManager, appWidgetId);
                                } else {
                                    Log.d("OBTENER INFO WIDGET", "FALLÓ");
                                    v=-1; //usado para disparar carga desde preferencias
                                }
                            } else {
                                Log.e("WORKER", "Algo falló (info).");
                            }

                            //cancelar el observer
                            liveData.removeObserver(this);
                        }
                    }
                };

                //registrar el observer
                liveData.observeForever(observer);

            } else {
                Toast.makeText(context, context.getString(R.string.masCampos), Toast.LENGTH_SHORT).show();
            }

            //updateAppWidget(context, appWidgetManager, appWidgetId);
            updateAppWidget(contextIdioma, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}