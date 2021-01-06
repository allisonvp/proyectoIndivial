package com.example.feriapucp.Autenticacion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.feriapucp.Roles.ClienteActivity;
import com.example.feriapucp.MainActivity;
import com.example.feriapucp.R;
import com.example.feriapucp.Roles.VendedorActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InicioSesion extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        ValidacionUsuario(currentUser);
    }

    public void IniciarSesion(View view){

        EditText eCorreo = findViewById(R.id.eCorreo);
        EditText eContrasenha = findViewById(R.id.eContrasenha);
        ProgressBar progressIniciarSesion = findViewById(R.id.progressIniciarSesion);


        String correo = eCorreo.getText().toString().trim();
        String pwd = eContrasenha.getText().toString().trim();

        if (TextUtils.isEmpty(correo)){
            eCorreo.setError("Ingrese un correo electronico");
            return;
        }

        if (TextUtils.isEmpty(pwd)){
            eContrasenha.setError("Ingrese un contraseña");
            return;
        }

        if (pwd.length()<8){
            eContrasenha.setError("Ingrese como mínimo 8 caracteres");
            return;
        }

        progressIniciarSesion.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(correo, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d( "infoApp", "Usuario loggeado correctamente");
                           // Toast.makeText(InicioSesion.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                            currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            ValidacionUsuario(currentUser);
                        } else {
                            Log.w("infoApp", "Usuario no registrado correctamente", task.getException());
                            Toast.makeText(InicioSesion.this, "Usuario invalido", Toast.LENGTH_SHORT).show();
                            progressIniciarSesion.setVisibility(View.GONE);
                        }
                    }
                });
    }

    public void CambiarARegistrarse(View view){
        Intent intent = new Intent(InicioSesion.this, Registro.class);
        startActivity(intent);
    }

    public void recuperarContrasenha(View view){
        EditText correoRecuperarPwd = new EditText(view.getContext());
        correoRecuperarPwd.setHint("Correo electrónico");
        AlertDialog.Builder recuperarDialog = new AlertDialog.Builder(view.getContext());
        recuperarDialog.setTitle("Recuperar contraseña");
        recuperarDialog.setMessage("Por favor ingrese su correo para recuperar su contraseña");
        recuperarDialog.setView(correoRecuperarPwd);

        recuperarDialog.setPositiveButton("Enviar correo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String correoRec = correoRecuperarPwd.getText().toString();

                if (TextUtils.isEmpty(correoRec)){
                    correoRecuperarPwd.setError("Ingrese un correo electronico");
                    return;
                }else{
                    firebaseAuth.sendPasswordResetEmail(correoRec).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(InicioSesion.this, "Se le envio un correo para recuperar su contraseña", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(InicioSesion.this, "Error en el envio de correo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        recuperarDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        recuperarDialog.show();
    }

    public void ValidacionUsuario(FirebaseUser currentUser){

       // final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                irPaginaPrincipal(currentUser);
            } else {
                currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (currentUser.isEmailVerified()) {
                            irPaginaPrincipal(currentUser);
                        } else {
                            Toast.makeText(InicioSesion.this, "Se le ha enviado un correo para verificar su cuenta", Toast.LENGTH_SHORT).show();
                            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("infoApp", "correo enviado");
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    public void irPaginaPrincipal(FirebaseUser currentUser){

        String uid = currentUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        usuario = new Usuario();
        Log.d("infoApp", uid);
        reference.child("usuarios").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    usuario = snapshot.getValue(Usuario.class);
                    if (usuario.getRol().equals(null)) {

                    } else if (usuario.getRol().equals("Cliente")){
                        Intent intent = new Intent(InicioSesion.this, ClienteActivity.class);
                        intent.putExtra("nameUser", usuario.getNombre());
                        intent.putExtra("rol", usuario.getRol());
                        intent.putExtra("numero", usuario.getNumero());
                        startActivity(intent);
                    } else if (usuario.getRol().equals("Vendedor")){
                        Intent intent = new Intent(InicioSesion.this, VendedorActivity.class);
                        intent.putExtra("nameUser", usuario.getNombre());
                        intent.putExtra("rol", usuario.getRol());
                        intent.putExtra("numero", usuario.getNumero());
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(InicioSesion.this, MainActivity.class);
                        intent.putExtra("nameUser", usuario.getNombre());
                        startActivity(intent);
                    }
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}

