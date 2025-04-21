package com.example.entrega1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private MapView map;
    private Button buttonAsk;
    private TextView textAsk;
    private Button buttonAtras;
    private Button buttonBuscar;
    private EditText ikms;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //configuración de osmdroid
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setCacheMapTileCount((short) 9);
        Configuration.getInstance().setCacheMapTileOvershoot((short) 2);

        File osmdroidBasePath = new File(getFilesDir(), "osmdroid");
        File osmdroidTileCache = new File(osmdroidBasePath, "tiles");
        Configuration.getInstance().setOsmdroidBasePath(osmdroidBasePath);
        Configuration.getInstance().setOsmdroidTileCache(osmdroidTileCache);

        setContentView(R.layout.activity_map);

        //inicializar UI
        textAsk = findViewById(R.id.textViewRequisito);
        buttonAsk = findViewById(R.id.buttonAskMe);
        buttonAtras = findViewById(R.id.buttonAtrasMap);
        buttonBuscar = findViewById(R.id.buttonBuscar);
        ikms = findViewById(R.id.inputKms);
        map = findViewById(R.id.map);

        //configurar el mapa de OSM
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        map.setMultiTouchControls(true);

        //inicializar el administrador de ubicación
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //verificar y solicitar permisos de ubicación
        buttonAsk.setOnClickListener(new View.OnClickListener() {
            @Override
             public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
             }
        });
        GeoPoint startPoint = new GeoPoint(40.416775, -3.703790); // Madrid
        map.getController().setCenter(startPoint);
        map.getController().setZoom(10.0);

        requestLocationPermission();

        buttonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rutinaUbicacion();
            }
        });

        buttonAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //si no tiene permisos, solicitarlos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            //si ya tiene permisos, iniciar la ubicación
            startLocationUpdates();
        }
    }

    private void rutinaUbicacion(){ //usada al iniciar updates y al pulsar boton
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.i("UBICACION", "DESCONOCIDA");
            }

            if (lastKnownLocation != null) {
                Log.i("UBICACIÓN", "Última ubicación conocida: " + lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude());
                updateMapLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                if (ikms.getText() != null && !String.valueOf(ikms.getText()).isEmpty()) {
                    if(String.valueOf(ikms.getText()).equals("0")){
                        ikms.setText("5");
                    }
                    obtenerCasinos(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), Integer.parseInt(String.valueOf(ikms.getText())));
                } else {
                    obtenerCasinos(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 5);
                }
            }
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            buttonAsk.setVisibility(View.INVISIBLE);
            textAsk.setVisibility(View.INVISIBLE);
            map.setVisibility(View.VISIBLE);
            buttonBuscar.setVisibility(View.VISIBLE);
            ikms.setVisibility(View.VISIBLE);

            rutinaUbicacion();

            //definir el listener de ubicación
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Log.i("UBICACIÓN", "Latitud: " + latitude + ", Longitud: " + longitude);

                    //actualizar el mapa con la ubicación
                    updateMapLocation(latitude, longitude);
                }
            };

            //solicitar actualizaciones de ubicación con GPS
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
    }

    private void updateMapLocation(double latitude, double longitude) {
        if(map!=null) {
            GeoPoint userLocation = new GeoPoint(latitude, longitude);

            //centrar el mapa en la ubicación del usuario
            map.getController().setCenter(userLocation);
            //map.getController().setZoom(18.0); //esto puede resultar incómodo

            //eliminar marcadores previos
            map.getOverlays().clear();

            //crear y agregar un nuevo marcador en la ubicación actual
            Marker marker = new Marker(map);
            marker.setPosition(userLocation);
            marker.setTitle("Ubicación actual");
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            if (ikms.getText() != null && !String.valueOf(ikms.getText()).isEmpty()) {
                obtenerCasinos(latitude, longitude, Integer.parseInt(String.valueOf(ikms.getText())));
            } else {
                obtenerCasinos(latitude, longitude, 5);
            }

            map.getOverlays().add(marker);
            map.invalidate();
        }
    }

    private void obtenerCasinos(double latitude, double longitude, int kms) {
        OkHttpClient client = new OkHttpClient();
        String query;
        if(kms==0){
            query = "[out:json];(" +
                    "node(around:5000," + latitude + "," + longitude + ")[\"amenity\"=\"casino\"];" +
                    "node(around:5000," + latitude + "," + longitude + ")[\"leisure\"=\"gambling\"];" +
                    "node(around:5000," + latitude + "," + longitude + ")[\"shop\"=\"betting\"];" +
                    "node(around:5000," + latitude + "," + longitude + ")[\"gambling\"];" +
                    ");out;";
        }else{
            query = "[out:json];(" +
                    "node(around:"+kms+"000," + latitude + "," + longitude + ")[\"amenity\"=\"casino\"];" +
                    "node(around:"+kms+"000," + latitude + "," + longitude + ")[\"leisure\"=\"gambling\"];" +
                    "node(around:"+kms+"000," + latitude + "," + longitude + ")[\"shop\"=\"betting\"];" +
                    "node(around:"+kms+"000," + latitude + "," + longitude + ")[\"gambling\"];" +
                    ");out;";
        }

        String url = "https://overpass-api.de/api/interpreter?data=" + Uri.encode(query);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MapActivity.this, "Error al obtener casinos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;

                String responseData = response.body().string();

                try {
                    JSONObject json = new JSONObject(responseData);
                    Log.d("OVERPASS",json.toString());
                    JSONArray elements = json.getJSONArray("elements");

                    runOnUiThread(() -> {
                        for (int i = 0; i < elements.length(); i++) {
                            try {
                                JSONObject element = elements.getJSONObject(i);
                                double lat = element.getDouble("lat");
                                double lon = element.getDouble("lon");

                                String name = "Casino";
                                if (element.has("tags")) {
                                    JSONObject tags = element.getJSONObject("tags");
                                    if (tags.has("name")) {
                                        name = tags.getString("name");
                                    }
                                }

                                marcarCasino(lat, lon, name);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void marcarCasino(double lat, double lon, String title) {
        Log.d("CASINO LOCATED","HERE");
        GeoPoint point = new GeoPoint(lat, lon);
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.icono_rombo)); // Usa un ícono rojo de casino
        map.getOverlays().add(marker);
        map.invalidate();
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
            locationManager = null;
        }
        map = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
