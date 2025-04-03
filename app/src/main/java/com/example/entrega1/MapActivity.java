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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;

public class MapActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private MapView map;
    private Button buttonAsk;
    private TextView textAsk;
    private Button buttonAtras;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de osmdroid
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

        // Inicializar UI
        textAsk = findViewById(R.id.textViewRequisito);
        buttonAsk = findViewById(R.id.buttonAskMe);
        buttonAtras = findViewById(R.id.buttonAtrasMap);
        map = findViewById(R.id.map);

        // Configurar el mapa de OSM
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        map.setMultiTouchControls(true);

        // Inicializar el administrador de ubicación
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Verificar y solicitar permisos de ubicación
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
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Si no tiene permisos, solicitarlos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Si ya tiene permisos, iniciar la ubicación
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            buttonAsk.setVisibility(View.INVISIBLE);
            textAsk.setVisibility(View.INVISIBLE);
            map.setVisibility(View.VISIBLE);

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.i("UBICACION", "DESCONOCIDA");
            }

            if (lastKnownLocation != null) {
                Log.i("UBICACIÓN", "Última ubicación conocida: " + lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude());
                updateMapLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            }

            // Definir el listener de ubicación
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Log.i("UBICACIÓN", "Latitud: " + latitude + ", Longitud: " + longitude);

                    // Actualizar el mapa con la ubicación
                    updateMapLocation(latitude, longitude);
                }
            };

            // Solicitar actualizaciones de ubicación con GPS
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
    }

    private void updateMapLocation(double latitude, double longitude) {
        if(map!=null) {
            GeoPoint userLocation = new GeoPoint(latitude, longitude);

            // Centrar el mapa en la ubicación del usuario
            map.getController().setCenter(userLocation);
            map.getController().setZoom(18.0);

            // Eliminar marcadores previos
            map.getOverlays().clear();

            // Crear y agregar un nuevo marcador en la ubicación actual
            Marker marker = new Marker(map);
            marker.setPosition(userLocation);
            marker.setTitle("Ubicación actual");
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            map.getOverlays().add(marker);
            map.invalidate();
        }
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
