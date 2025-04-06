package com.example.entrega1;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

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
            Log.d("WORKER", "Usando url por defecto.");
            direccion = "http://51.44.167.78:80/lbilbao040/WEB/api.php"; //POR DEFECTO
        }

        if(getInputData().getString("accion")!=null){ //código función
            String accion = getInputData().getString("accion");
            boolean paramsValidos = false;
            switch (accion){
                case "insertar": //crear usuario
                    //POST, parametros etc
                    HttpURLConnection urlConnection = null;
                    URL destino = null;
                    try {
                        destino = new URL(direccion);
                        urlConnection = (HttpURLConnection) destino.openConnection();
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setReadTimeout(5000);
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                        // Crear JSON con los parámetros
                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("accion", accion);

                        if(getInputData().getString("usuario")!=null && getInputData().getString("pw")!=null){
                            jsonParam.put("usuario", getInputData().getString("usuario"));
                            jsonParam.put("pw", getInputData().getString("pw"));
                            paramsValidos = true;
                        }
                        if(getInputData().getString("monedas")!=null) {
                            jsonParam.put("monedas", getInputData().getInt("monedas",10));
                        }else{
                            jsonParam.put("monedas", 10);
                        }
                        Log.d("WORKER", "JSON definido");
                        Log.d("WORKER", "JSON a enviar: " + jsonParam.toString());
                        if (paramsValidos) {
                            // Escribir el JSON en el cuerpo de la solicitud
                            OutputStream os = urlConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(jsonParam.toString());
                            writer.flush();
                            writer.close();
                            os.close();

                            // Enviar la solicitud y recibir la respuesta
                            int responseCode = urlConnection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = br.readLine()) != null) {
                                    response.append(line);
                                }
                                br.close();

                                // Parsear respuesta JSON
                                JSONObject respuestaJson = new JSONObject(response.toString());
                                String mensaje = respuestaJson.optString("message", "Sin mensaje");
                                String codigo = respuestaJson.optString("code", "-1");

                                Log.d("RESPUESTA", response.toString()); // Imprimir respuesta del servidor
                                return Result.success(new Data.Builder()
                                        .putString("message", mensaje)
                                        .putString("code", codigo)
                                        .build());
                            } else {
                                Log.e("ERROR", "Error en la solicitud: " + responseCode);
                                return Result.failure();
                            }
                        }else{
                            return Result.failure();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e("WORKER", "Excepción en doWork: " + e.getMessage(), e);
                        return Result.failure(new Data.Builder()
                                .putString("message", "Excepción: " + e.getMessage())
                                .putString("code", "-1")
                                .build());
                    } //break
                case "vu": //verificar usuario, para inicio de sesión
                    //POST
                    break;
                case "bu": //borrar usuario
                    //POST
                    break;
            }
        }

        /*HttpURLConnection urlConnection = null;
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        return null;
    }
}
