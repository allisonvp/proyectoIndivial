package com.example.feriapucp.Roles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.feriapucp.Autenticacion.InicioSesion;
import com.example.feriapucp.Autenticacion.Usuario;
import com.example.feriapucp.MainActivity;
import com.example.feriapucp.Producto.Producto;
import com.example.feriapucp.Producto.ProductoAdapter;
import com.example.feriapucp.Producto.ProductoDetalle;
import com.example.feriapucp.Producto.ProductoDto;
import com.example.feriapucp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClienteActivity extends AppCompatActivity {

    private static final int IMAGE_PERMISSION = 3;
    StorageReference storageReference;
    FirebaseUser currentUser;
    DatabaseReference databaseReference;
    ProductoDto productoDto;
    List<Producto> prod;
    String nameUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        productoDto = new ProductoDto();
        prod = new ArrayList<>();
        Intent intent = getIntent();
        nameUser = intent.getStringExtra("nameUser");
        TextView nombre = findViewById(R.id.idHolaUsuario);
        String nomConcat = "Hola, " + nameUser;
        nombre.setText(nomConcat);

        descargarImagenes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar,menu);
        return true;
    }

    public void cerrarSesion(MenuItem item){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        startActivity(new Intent(ClienteActivity.this, InicioSesion.class));
        finish();
    }

    public void descargarImagenes(){

        databaseReference.child("productosDeTodosLosUsuarios")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot children : snapshot.getChildren()){
                                Producto producto = children.getValue(Producto.class);
                                prod.add(producto);
                            }

                            productoDto.setListaProductos(prod);
                            ProductoAdapter adapter = new ProductoAdapter(productoDto.getListaProductos(), ClienteActivity.this, nameUser);
                            RecyclerView recyclerView = findViewById(R.id.product_recycler);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ClienteActivity.this));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == IMAGE_PERMISSION) {
                descargarImagenes();
            }
        }
    }
}