package com.example.entrega1;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.io.ByteArrayOutputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.Manifest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ProfileActivity extends BaseActivity {

    private int id;
    private int saldo;
    private String nombre;
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private Uri selectedGalleryImageUri;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY = 1001;
    private static final int REQUEST_PERMISSION_GALLERY = 2001;
    //UI
    ImageView pfp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getInt("id");
            nombre = extras.getString("nombre");
            saldo = extras.getInt("saldo");
        }

        TextView textUser = findViewById(R.id.textUser);
        pfp = findViewById(R.id.pfp);
        ImageButton buttonCam = findViewById(R.id.buttonCam);
        ImageButton buttonGallery = findViewById(R.id.buttonGallery);

        textUser.setText(nombre);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean readGranted = result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
                    if (readGranted) {
                        abrirGaleria();
                    } else {
                        Toast.makeText(this, "Permiso de lectura requerido", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        //--- OBTENER IMAGEN --\\
        if(id>0) {
            Data datos = new Data.Builder()
                    .putString("url","2") //url a php gestor de monedas
                    .putString("accion", "getpfp") //obtiene monedas de usuario
                    .putString("nombre", nombre)
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(conexionBDWebService.class)
                    .setInputData(datos)
                    .build();

            WorkManager.getInstance(ProfileActivity.this).enqueue(request);

            //escuchar resultado
            WorkManager.getInstance(getApplicationContext())
                    .getWorkInfoByIdLiveData(request.getId())
                    .observe(ProfileActivity.this, workInfo -> {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                String mensaje = workInfo.getOutputData().getString("message");
                                Log.d("WORKER", "¡200! " + mensaje);
                                String code = workInfo.getOutputData().getString("code");
                                if(code.equals("0")) {
                                    //coger foto
                                    //String fotoraw = workInfo.getOutputData().getString("imagen");
                                    String url = workInfo.getOutputData().getString("url");
                                    String urlConId = url + "?nocache=" + System.currentTimeMillis();
                                    if (url!=null) {
                                        //Log.d("URL_DEBUG", fotoraw);
                                        /*byte[] decodedString = Base64.decode(fotoraw, Base64.DEFAULT);
                                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        pfp.setImageBitmap(decodedByte);*/
                                        Glide.with(this)
                                                .load(urlConId)
                                                .placeholder(R.drawable.icono_rombo)
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .into(pfp); // Tu ImageView
                                    }else{
                                        pfp.setImageResource(R.drawable.icono_rombo);
                                    }
                                }else{
                                    //error total
                                    Log.d("OBETENER IMAGEN", "FALLÓ");
                                }
                            } else {
                                Log.e("WORKER", "Algo falló.");
                            }
                        }
                    });

        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.masCampos), Toast.LENGTH_SHORT).show();
        }

        //configurar launcher de camara
            cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        subirPfp();
                    }
                }
        );
        //configurar launcher de galeria
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        selectedGalleryImageUri = imageUri;

                        String path = uri2path(this, imageUri);
                        if (path != null) {
                            currentPhotoPath = path;
                            subirPfp();
                        } else {
                            Toast.makeText(this, "No se pudo obtener la ruta de la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        //onClick camara
        buttonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    //no tiene permisos, pedirlos
                    ActivityCompat.requestPermissions(ProfileActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                    Toast.makeText(ProfileActivity.this, getString(R.string.requierePermisosCam), Toast.LENGTH_SHORT).show();
                } else {
                    //ya tiene permisos, lanzar cámara
                    lanzarCamara();
                }


            }
        });

        //onClick galeria
        buttonGallery.setOnClickListener(v -> {
            String[] permissions = getStoragePermissions();
            boolean allGranted = true;
            for (String perm : permissions) {
                if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                permissionLauncher.launch(permissions);
            } else {
                abrirGaleria();
            }
        });

    }

    private void lanzarCamara(){
        Log.d("CAM MANAGER", "CLICK");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null || true) {
            Log.d("CAM MANAGER", "ENTRA");
            File photoFile = null;
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                photoFile = File.createTempFile(
                        imageFileName,
                        ".jpg",
                        storageDir
                );
                currentPhotoPath = photoFile.getAbsolutePath();
                Log.d("CAM MANAGER", currentPhotoPath);
            } catch (IOException ex) {
                Log.d("CAM MANAGER","Excepción: "+ex);
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri fotoUri = FileProvider.getUriForFile(ProfileActivity.this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private String uri2path(Context context, Uri uri) {//obtiene el path de la imagen desde la foto elegida en la galería
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor == null) return null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void subirPfp(){
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        pfp.setImageBitmap(bitmap);

        // Convertir a byte[] y guardar en base de datos
        /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        */
        if(id>0) {
            //String laimagen = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Data datos = new Data.Builder()
                    .putString("url","2") //url a php gestor de pfps
                    .putString("accion", "setpfp") //obtiene monedas de usuario
                    .putInt("id",id)
                    .putString("pic", currentPhotoPath) //se le pasa la ruta porque la imagen en sí es demasiado grande
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(conexionBDWebService.class)
                    .setInputData(datos)
                    .build();

            WorkManager.getInstance(ProfileActivity.this).enqueue(request);

            //escuchar resultado
            /*
            WorkManager.getInstance(getApplicationContext())
                    .getWorkInfoByIdLiveData(request.getId())
                    .observe(ProfileActivity.this, workInfo -> {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                String mensaje = workInfo.getOutputData().getString("message");
                                Log.d("WORKER", "¡200! " + mensaje);
                                String code = workInfo.getOutputData().getString("code");
                                if(code.equals("0")) {
                                    //coger foto
                                    String url = workInfo.getOutputData().getString("url");
                                    String urlConId = url + "?nocache=" + System.currentTimeMillis();
                                    if (url!=null) {
                                        //byte[] decodedString = Base64.decode(fotoraw, Base64.DEFAULT);
                                        //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        //pfp.setImageBitmap(decodedByte);
                                        Glide.with(this)
                                                .load(urlConId)
                                                .placeholder(R.drawable.icono_rombo)
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .into(pfp);
                                    }else{
                                        pfp.setImageResource(R.drawable.icono_rombo);
                                    }
                                }else{
                                    //error total
                                    Log.d("OBETENER IMAGEN", "FALLÓ");
                                }
                            } else {
                                Log.e("WORKER", "Algo falló.");
                            }
                        }
                    });*/

        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.masCampos), Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleria() {
        Log.d("GALLERY MANAGER","BUENAS");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        galleryLauncher.launch(intent);
    }

    private String[] getStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("saldo",saldo);
        outState.putInt("id",id);
        outState.putString("nombre", nombre);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        id = savedInstanceState.getInt("id");
        nombre = savedInstanceState.getString("nombre");
        saldo = savedInstanceState.getInt("saldo");
    }
}