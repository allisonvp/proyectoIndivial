package com.example.feriapucp.Roles;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.feriapucp.Autenticacion.InicioSesion;
import com.example.feriapucp.Autenticacion.Usuario;
import com.example.feriapucp.Producto.Producto;
import com.example.feriapucp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class VendedorActivity extends AppCompatActivity {

    FloatingActionButton btnSto, btnIm, btnCam;
    ProgressBar progressBar= null;
    EditText nombreProducto;
    EditText categoriaProducto;
    EditText descripcion;
    EditText precio;
    Boolean isAllFabsVisible;
    private static final int IMAGE_UPLOAD = 1;
    private static final int IMAGE_PERMISSION = 3;
    FirebaseUser currentUser;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    StorageTask uploadTask= null;
    public static final int CAMERA_PERMISSION = 5;
    public static final int CAMERA_UPLOAD = 7;
    ImageView selectedImage = null;
    EditText namePhoto;
    Uri imgURL;
    Bitmap bitmap;
    Boolean isCamera;
    Usuario usuario;
    Producto producto;
    String rol;
    Boolean existe;
    String nameUser;
    String numeroUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendedor);

        usuario = new Usuario();
        producto= new Producto();

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        btnSto = findViewById(R.id.add_storage);
        btnIm = findViewById(R.id.add_image);
        btnCam = findViewById(R.id.add_camera);
        progressBar = findViewById(R.id.progressBar);
        selectedImage = findViewById(R.id.selectedImage);

        nombreProducto = findViewById(R.id.idNomProd);
        categoriaProducto = findViewById(R.id.idCategoria);
        descripcion = findViewById(R.id.idDescripcion);
        precio = findViewById(R.id.idPrecio);
        namePhoto = findViewById(R.id.idNamePhoto);

        Intent intent =  getIntent();
        rol =intent.getStringExtra("rol");
        nameUser = intent.getStringExtra("nameUser");
        numeroUser = intent.getStringExtra("numero");

        setFloatingButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar,menu);
        return true;
    }

    public void cerrarSesion(MenuItem item){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        startActivity(new Intent(VendedorActivity.this, InicioSesion.class));
        finish();
    }

    public void setFloatingButton() {


        btnIm.setVisibility(View.GONE);
        btnCam.setVisibility(View.GONE);


        isAllFabsVisible = false;

        btnSto.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isAllFabsVisible) {
                            btnIm.show();
                            btnCam.show();
                            isAllFabsVisible = true;
                        } else {
                            btnIm.hide();
                            btnCam.hide();
                            isAllFabsVisible = false;
                        }
                    }
                });

        btnIm.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectImage();
                        isCamera = false;
                    }
                });
        btnCam.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        takePicture();
                        isCamera = true;
                    }
                });


    }

    public void uploadImageToFirebase(View view) {

        String nomFoto =namePhoto.getText().toString();

        if (TextUtils.isEmpty(nombreProducto.getText().toString())) {
            nombreProducto.setError("Ingrese un nombre de producto");
            return;
        } else if (TextUtils.isEmpty(categoriaProducto.getText().toString())){
            categoriaProducto.setError("Ingrese una categoria");
            return;
        } else if (TextUtils.isEmpty(descripcion.getText().toString())){
            descripcion.setError("Ingrese una descripcion");
            return;
        } else if (TextUtils.isEmpty(precio.getText().toString())){
            precio.setError("Ingrese un precio");
            return;
        }else if (TextUtils.isEmpty(nomFoto)) {
            namePhoto.setError("Ingrese un nombre para la foto");
            return;
        } else if (!TextUtils.isEmpty(nomFoto)){

            existe= false;
            databaseReference.child("productosDeTodosLosUsuarios")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot children : dataSnapshot.getChildren()){
                                Producto producto = children.getValue(Producto.class);
                                if (producto.getNombreFoto() != null) {
                                    if (producto.getNombreFoto().equals(nomFoto)) {
                                        Toast.makeText(VendedorActivity.this, "El nombre de la foto ya existe. Escoger otro nombre", Toast.LENGTH_SHORT).show();
                                        existe=true;
                                        break;
                                    }
                                }
                            }

                            if (!existe){

                                producto.setNombreProd(nombreProducto.getText().toString());
                                producto.setCategoria(categoriaProducto.getText().toString());
                                producto.setDescripcion(descripcion.getText().toString());
                                producto.setPrecio(precio.getText().toString());
                                producto.setNombreFoto(namePhoto.getText().toString());
                                producto.setMarca(nameUser);
                                producto.setNumero(numeroUser);
                                usuario.setProducto(producto);
                                usuario.setRol(rol);

                                databaseReference.child("usuarios").child(currentUser.getUid()).child("productos").push().setValue(producto)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("infoApp", "info guardada exitosamente");
                                                uploadImage();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("infoApp", "Error al guardar");
                                                e.printStackTrace();
                                            }
                                        });

                                databaseReference.child("productosDeTodosLosUsuarios").push().setValue(producto)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("infoApp", "info guardada exitosamente");

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("infoApp", "Error al guardar");
                                                e.printStackTrace();
                                            }
                                        });

                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (uploadTask != null) {
            progressBar.setVisibility(View.GONE);
            uploadTask.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (uploadTask != null && uploadTask.isInProgress()) {
            progressBar.setVisibility(View.GONE);
            uploadTask.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (uploadTask != null && uploadTask.isPaused()) {
            progressBar.setVisibility(View.VISIBLE);
            uploadTask.resume();
        }
    }


    public void uploadImage() {
        if (currentUser != null) {
            currentUser.reload();
            String uid = currentUser.getUid();
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);

            if (!isCamera) {

                if (imgURL != null) {
                    uploadTask = storageReference.child(namePhoto.getText().toString() + ".jpg")
                            .putFile(imgURL)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(VendedorActivity.this, "Archivo subido exitosamente", Toast.LENGTH_SHORT).show();
                                    Log.d("infoApp", "subida exitosa");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    Log.d("infoApp", "Error en la subida");
                                    e.printStackTrace();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                                    long bytesTransferred = snapshot.getBytesTransferred();
                                    long totalByteCount = snapshot.getTotalByteCount();

                                    double progreso = (int) Math.round((100.0 * bytesTransferred) / totalByteCount);

                                    Log.d("infoApp", "progreso: " + progreso + "%");

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        progressBar.setProgress((int) progreso, true);
                                    } else {
                                        progressBar.setProgress((int) progreso);
                                    }
                                }
                            });
                }
            } else {

                if (bitmap != null) {
                    Log.d("infoApp", "No sube la foto");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    uploadTask = storageReference.child(namePhoto.getText().toString() + ".jpg")
                            .putBytes(data)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(VendedorActivity.this, "Archivo subido exitosamente", Toast.LENGTH_SHORT).show();
                                    Log.d("infoApp", "subida exitosa");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    Log.d("infoApp", "Error en la subida");
                                    e.printStackTrace();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    long bytesTransferred = snapshot.getBytesTransferred();
                                    long totalByteCount = snapshot.getTotalByteCount();

                                    double progreso = (int) Math.round((100.0 * bytesTransferred) / totalByteCount);

                                    Log.d("infoApp", "progreso: " + progreso + "%");

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        progressBar.setProgress((int) progreso, true);
                                    } else {
                                        progressBar.setProgress((int) progreso);
                                    }
                                }
                            });

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_UPLOAD && data != null && data.getData() != null) {
                Log.d("infoApp", "Foto seleccionada");
                imgURL = data.getData();
                Picasso.with(this).load(imgURL).into(selectedImage);
            }else if (requestCode == CAMERA_UPLOAD && data != null ){
                Log.d("infoApp", "Foto tomada");
                bitmap = (Bitmap) data.getExtras().get("data");
                selectedImage.setImageBitmap(bitmap);
            }
        }
    }

    /*private Uri getImageUri(VendedorActivity context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    } */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == IMAGE_PERMISSION) {
                selectImage();
            }
            if(requestCode==CAMERA_PERMISSION){
                takePicture();
            }
        }
    }

    public void selectImage() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            Log.d("infoApp", "imagen abierta");
            startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), IMAGE_UPLOAD);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_PERMISSION);
        }
    }

    public void takePicture() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (permission == PackageManager.PERMISSION_GRANTED) {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, CAMERA_UPLOAD);
                Log.d("infoApp", "camara abierta");
            }

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
    }


}