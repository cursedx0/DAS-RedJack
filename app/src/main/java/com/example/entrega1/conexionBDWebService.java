package com.example.entrega1;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class conexionBDWebService extends Worker {

    public conexionBDWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //--PARÁMETROS--\\
        String direccion = "";
        if(getInputData().getString("url")!=null){
            direccion = getInputData().getString("url");
        }else{
            direccion = "http://servidor/servicioweb.php"; //POR DEFECTO
        }

        if(getInputData().getString("codigoFunc")!=null){
            String cf = getInputData().getString("codigoFunc");
            switch (cf){
                case "cu": //crear usuario
                    //POST, parametros etc
                    break;
                case "vu": //verificar usuario, para inicio de sesión
                    //POST
                    break;
                case "bu": //borrar usuario
                    //POST
                    break;
            }
        }

        HttpURLConnection urlConnection = null;
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
