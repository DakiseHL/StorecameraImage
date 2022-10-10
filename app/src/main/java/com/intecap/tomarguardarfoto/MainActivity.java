package com.intecap.tomarguardarfoto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //preparando variables para interfaz grafica
    ImageView ilustracionMostrar;
    FloatingActionButton btnEnviarCamara;
    //definir constantes con los codigos
    private static final int codPermisoCamara=205;
    private static final int codigoServicioCamara=301;

    //variable que resguardara la ruta de la fotografia
    String ubicacionFoto;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==codigoServicioCamara){
            if (resultCode== Activity.RESULT_OK){
                ilustracionMostrar.setImageURI(Uri.parse(ubicacionFoto));
            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ilustracionMostrar=findViewById(R.id.imagenGuardada);
        btnEnviarCamara=findViewById(R.id.btnAbrirCamara);
 //le insertamos evento OnClick
        btnEnviarCamara.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //aca en el evento, es una validacion para acceder al servicio de camara
                //Esto aplica  para versiones recientes de android
                //dentro del  if obtenemos la version de android y lo comparamos a la version 6
                //si  es superior o identico procedemos con otra  validacion
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    //se  valida  que en el Manifest  elpermiso esta concedido
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        capturarFoto();
                    }else{
                        //se crea el dialogo de permiso camara
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},codPermisoCamara);
                    }


                }else{
                    capturarFoto();

                }

            }
        });



    }
//ESTE METODO SE CREA CON EL AUXILIAR DE  ANDROID
    private void capturarFoto() {
        //LA PRIMERA INSTRUCCION ES ENVIARLO AL SERVICIO DE CAMARA
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //EN ALGUN MOMENTO TENDRA QUE REGRESAR ASI QUE HACEMOS  UNA VALIDACION
        //cuando regrese, aun no ha creado el archivo, asi que necesitamos validarlo
        //para proceder a capturar la foto y crearle su propio archivo y asi depositarlo
        //en la memoria interna
        if (i.resolveActivity(getPackageManager())!=null){
            //empezando a crear archivo
            File imagenArchivo=null;
            try {
                imagenArchivo=crearArchivo();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (imagenArchivo!=null){
                Uri fotoUri = FileProvider.getUriForFile(MainActivity.this,"com.Intecap.tomarguardarfoto",imagenArchivo);
                //en este  punto se guardo la  fotografia  en buestro paquete
                i.putExtra(MediaStore.EXTRA_OUTPUT,fotoUri);
                //aqui nuevamente iniciamos  nuestra aplicacion pero ya esta guardado la imagen
                //en el paquete...
                startActivityForResult(i,codigoServicioCamara);
            }


        }



    }
//este metodo le  asignara el nombre al  archivo
    //usualmente en las fotos  se le asigna la fecha y hora correspondiente
    private File crearArchivo() throws IOException {
        //empezamos a preparar el nopmbre de la imagen
        //creamos variable para recuperar hora y fecha
        String horaFecha=new SimpleDateFormat("yyyyMMdd_Hh-mm-ss", Locale.getDefault()).format(new Date());
        String nombreImagen="IMG_"+horaFecha+"_";
        //creamos la foto...
        File nuevaUbicacion=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //la imagen ya fue creada tambien depositada en nuestro paquete
        File ImagenFinal = File.createTempFile(nombreImagen,".jpg",nuevaUbicacion);
        //obtenemos la ubicacion final
        //la variable ubicacion foto deberia de depositarse en la base de datos
        ubicacionFoto=ImagenFinal.getAbsolutePath();
    return ImagenFinal;
    }



    //metodo para pedir permiso
    //esto es para la parte del 2ndo Else del OnClick event
    //para ello este metodo, para validar que el usuario presion PERMISO CONDEDIDO
    //Si lo acepta, lo enviamos a capturar foto
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == codPermisoCamara) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capturarFoto();
            } else {
                Toast.makeText(this, "Es necesario confirmar el acceso", Toast.LENGTH_LONG).show();
            }

        }
    }
}