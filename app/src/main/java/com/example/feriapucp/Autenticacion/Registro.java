package com.example.feriapucp.Autenticacion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.feriapucp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registro extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
    }

    public void Registrarse(View view){

        EditText eCorreoRegis = findViewById(R.id.eCorreoRegis);
        EditText eNumero = findViewById(R.id.idNumero);
        EditText eContrasenhaRegis = findViewById(R.id.eContrasenhaRegis);
        EditText eNombre = findViewById(R.id.eNombre);
        RadioGroup radioGroup = findViewById(R.id.idRol);
        firebaseAuth = FirebaseAuth.getInstance();
        RadioButton btnVendedor = findViewById(R.id.idVendedor);
        String vendedor = btnVendedor.getText().toString();
        RadioButton btnCliente = findViewById(R.id.idCliente);
        String cliente = btnCliente.getText().toString();
        usuario = new Usuario();

        String correoRegis = eCorreoRegis.getText().toString().trim();
        String pwdRegis = eContrasenhaRegis.getText().toString().trim();
        String nombre = eNombre.getText().toString();
        String numero = eNumero.getText().toString();


        if (TextUtils.isEmpty(nombre)) {
            eNombre.setError("Ingrese un nombre de marca o usuario");
            return;
        }else{
            usuario.setNombre(nombre);
        }

        if (TextUtils.isEmpty(numero)){
            eNumero.setError("Ingrese un correo electronico");
            return;
        }else{
            usuario.setNumero(numero);
        }

        if (TextUtils.isEmpty(correoRegis)){
            eCorreoRegis.setError("Ingrese un correo electronico");
            return;
        }

        if (TextUtils.isEmpty(pwdRegis)){
            eContrasenhaRegis.setError("Ingrese un contraseña");
            return;
        }

        if (pwdRegis.length()<8){
            eContrasenhaRegis.setError("Ingrese como mínimo 8 caracteres");
            return;
        }

        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(Registro.this, "Por favor seleccione entre vendedor o cliente", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnVendedor.isChecked()){
            usuario.setRol(vendedor);
        }else{
            usuario.setRol(cliente);
        }


        firebaseAuth.createUserWithEmailAndPassword(correoRegis, pwdRegis)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d( "infoApp", "Usuario registrado correctamente");
                            currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("infoApp", "correo enviado");
                                }
                            });
                            Toast.makeText(Registro.this, "Se le ha enviado un correo para verificar su cuenta", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Registro.this, InicioSesion.class);
                            startActivity(intent);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                            String uid = currentUser.getUid();
                            reference.child("usuarios").child(uid).setValue(usuario)
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

                        } else {
                            Log.w("infoApp", "Usuario no registrado correctamente", task.getException());
                            Toast.makeText(Registro.this, "Usuario no registrado correctamente", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    public void CambiarAIniciarSesion(View view){
        Intent intent = new Intent(Registro.this, InicioSesion.class);
        startActivity(intent);
    }


}