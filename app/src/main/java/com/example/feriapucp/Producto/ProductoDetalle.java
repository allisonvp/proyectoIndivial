package com.example.feriapucp.Producto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.feriapucp.Autenticacion.InicioSesion;
import com.example.feriapucp.R;
import com.example.feriapucp.Roles.ClienteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;

public class ProductoDetalle extends AppCompatActivity {

    ImageView img;
    TextView nom;
    TextView marca;
    TextView cat;
    TextView descrip;
    TextView num;
    TextView precio;
    ImageButton btnWhatsapp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_detalle);
        setTitle("Detalle del producto");

        img = findViewById(R.id.idImgDet);
        nom = findViewById(R.id.idNombreProDet);
        marca = findViewById(R.id.idMarcaDet);
        cat = findViewById(R.id.idCatDet);
        descrip = findViewById(R.id.idDescripDet);
        num = findViewById(R.id.idNumeroCelularDet);
        precio = findViewById(R.id.idPrecioDet);
        btnWhatsapp = findViewById(R.id.btnWhatsAppDet);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        Producto producto = (Producto) intent.getSerializableExtra("producto");

        StorageReference sto = storageReference.child(producto.getNombreFoto() + ".jpg");
        Glide.with(ProductoDetalle.this).load(sto).into(img);


        nom.setText(producto.getNombreProd());
        marca.setText(producto.getMarca());
        cat.setText(producto.getCategoria());
        descrip.setText(producto.getDescripcion());
        num.setText(producto.getNumero());
        precio.setText(producto.getPrecio());
        String msg = "Hola! Me gustaría saber más acerca de su producto y cómo podria adquirirlo. Muchas gracias!";
        btnWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean installed = isInstalled("com.whatsapp");
                if(installed){
                    Intent intent1 = new Intent(Intent.ACTION_VIEW);
                    intent1.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + "+51" + producto.getNumero() + "&text=" + msg));
                    startActivity(intent1);
                }else{
                    Toast.makeText(ProductoDetalle.this, "WhatsApp no se encuentra instalado en su dispositivo. Instalelo para enviar un mensaje al vendedor", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean isInstalled(String url){
        PackageManager packageManager = getPackageManager();
        boolean appInstalled;
        try{
            packageManager.getPackageInfo(url, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        }catch (PackageManager.NameNotFoundException e){
            appInstalled = false;
        }
        return appInstalled;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar,menu);
        return true;
    }

    public void cerrarSesion(MenuItem item){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        startActivity(new Intent(ProductoDetalle.this, InicioSesion.class));
        finish();
    }

}