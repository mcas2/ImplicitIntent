package com.mcas2.implicitintent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static final int CODIGO_PERMISOS_CAMARA = 1;
    private ActivityResultLauncher<Intent> myARL;
    private Button openCamera;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imagen);

        openCamera = findViewById(R.id.openCamera);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarCamara();
            }
        });

        myARL = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent intentRetorno = result.getData();
                            Bundle extras = intentRetorno.getExtras();
                            Bitmap imagen = (Bitmap) extras.get("data");
                            imageView.setImageBitmap(imagen);
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED){
                            Log.i("Mensaje:", "Ha fallado la cámara");
                        }
                    }
                }
        );
    }

    public void lanzarCamara(){
        //Comprueba si ya había concedido el permiso previamente. Si el valor coincide con permission_granted
        //es que habría concedido permisos antes.
        int comprobacionPermisos = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (comprobacionPermisos == PackageManager.PERMISSION_GRANTED){
            lanzarIntentImplicitoCapturaImagen();
        } else { //Si no se ha hecho, solicito los permisos.
            Log.i("Mensaje:", "No se tiene acceso a la cámara");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CODIGO_PERMISOS_CAMARA);
        }
    }

    @Override //Método sobreescrito que personalizamos sobreescribiéndolos. Se ejecuta sólo una vez.
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //grantResults se llena de cero en cada aplicación con los permisos que se van concediendo, lo hace Android por nosotros.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){ //Qué permisos estoy pidiendo?
            case CODIGO_PERMISOS_CAMARA: //La cámara en este caso.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    lanzarIntentImplicitoCapturaImagen();
                } else {
                    Log.i("Mensaje:", "El usuario no da permiso para abrir la cámara");
                }
        }
    }

    public void lanzarIntentImplicitoCapturaImagen(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager())!=null) myARL.launch(intent);
        else Log.i("Mensaje:", "No hay actividad para la cámara");
    }
}