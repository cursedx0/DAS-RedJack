package com.example.entrega1;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.util.Locale;
import java.util.Random;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReminderReceiver", "¡onReceive llamado!");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String idioma = prefs.getString("idioma", "es"); // español por defecto

        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        Context contextoConIdioma = context.createConfigurationContext(config); //usado para que las notificaciones generadas por esta clase sean traducidas

        createNotificationChannel(contextoConIdioma); //asegura el canal en Android 8+

        Random random = new Random();
        int numero = random.nextInt(2); //devuelve 0 o 1

        String mensaje;
        if (numero == 0) {
            mensaje = contextoConIdioma.getString(R.string.dealerTriste);
        } else {
            mensaje = contextoConIdioma.getString(R.string.barajaCaliente);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "blackjack_channel")
                .setSmallIcon(R.drawable.icono_rombo)
                .setContentTitle(contextoConIdioma.getString(R.string.juegaRedJack))
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1001, builder.build());

        // Reprogramar siguiente alarma dentro de 30 minutos
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, ReminderReceiver.class);
        PendingIntent newPendingIntent = PendingIntent.getBroadcast(
                context, 0, newIntent, PendingIntent.FLAG_IMMUTABLE
        );
        long nextTrigger = System.currentTimeMillis() + 30 * 60 * 1000; //30 minutos
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextTrigger, newPendingIntent);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Recordatorios Blackjack";
            String description = "Canal para notificaciones de inactividad";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("blackjack_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
