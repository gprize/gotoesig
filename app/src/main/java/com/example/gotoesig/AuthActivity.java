package com.example.gotoesig;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    private LinearLayout formLogin, formRegister;
    private TextView tvAuthTitle;
    private Button btnShowLogin, btnShowRegister, btnLogin, btnRegister;
    private EditText etLoginEmail, etLoginPassword, etRegisterName, etRegisterSurname, etRegisterCity, etRegisterEmail, etRegisterPhone, etRegisterPassword;

    private boolean isLoginFormVisible = true; // Par défaut, formulaire de connexion visible
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Initialisation des vues
        formLogin = findViewById(R.id.layout_login);
        formRegister = findViewById(R.id.layout_register);
        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        btnShowLogin = findViewById(R.id.btn_show_login);
        btnShowRegister = findViewById(R.id.btn_show_register);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        etLoginEmail = findViewById(R.id.et_login_email);
        etLoginPassword = findViewById(R.id.et_login_password);
        etRegisterName = findViewById(R.id.et_register_name);
        etRegisterSurname = findViewById(R.id.et_register_surname);
        etRegisterCity = findViewById(R.id.et_register_city);
        etRegisterEmail = findViewById(R.id.et_register_email);
        etRegisterPhone = findViewById(R.id.et_register_phone);
        etRegisterPassword = findViewById(R.id.et_register_password);

        //Initialisation Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Par défaut, afficher le formulaire de connexion
        formLogin.setVisibility(View.VISIBLE);
        formRegister.setVisibility(View.GONE);
        tvAuthTitle.setText("Connexion");
        btnShowRegister.setText("S'inscrire");

        // Action pour le bouton "Se connecter"
        btnLogin.setOnClickListener(v -> loginUser());

        // Action pour le bouton "S'inscrire"
        btnRegister.setOnClickListener(v -> registerUser());

        // Bascule entre les formulaires de connexion et d'inscription
        btnShowLogin.setOnClickListener(v -> {
            formLogin.setVisibility(View.VISIBLE);
            formRegister.setVisibility(View.GONE);
            tvAuthTitle.setText("Connexion");
            btnShowRegister.setText("S'inscrire");
        });

        btnShowRegister.setOnClickListener(v -> {
            formLogin.setVisibility(View.GONE);
            formRegister.setVisibility(View.VISIBLE);
            tvAuthTitle.setText("Inscription");
            btnShowLogin.setText("Se connecter");
        });
    }

    private void loginUser() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Rediriger vers l'activité principale
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String name = etRegisterName.getText().toString().trim();
        String surname = etRegisterSurname.getText().toString().trim();
        String city = etRegisterCity.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String phone = etRegisterPhone.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();

        if (name.isEmpty() || surname.isEmpty() || city.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Ajout des infos utilisateur dans Firestore
                        String userId = auth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("surname", surname);
                        user.put("phone", phone);
                        user.put("city", city);
                        user.put("email", email);

                        db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AuthActivity.this, "Utilisateur enregistré avec succès !", Toast.LENGTH_SHORT).show();
                                    // Rediriger vers l'activité principale
                                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Erreur lors de l'enregistrement : " + e.getMessage());
                                    Toast.makeText(AuthActivity.this, "Erreur lors de l'enregistrement.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("FirebaseAuth", "Erreur : " + task.getException().getMessage());
                        Toast.makeText(this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
