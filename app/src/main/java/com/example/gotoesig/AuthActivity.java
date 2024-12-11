package com.example.gotoesig;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText editTextName, editTextSurname, editTextPhone, editTextCity, editTextEmail, editTextPassword;
    private Button buttonRegister, buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextCity = findViewById(R.id.editTextCity);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Gestion des clics
        buttonRegister.setOnClickListener(view -> registerUser());
        buttonLogin.setOnClickListener(view -> loginUser());
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String surname = editTextSurname.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String city = editTextCity.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validation des champs
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(surname) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(city) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Tous les champs sont obligatoires.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Création de l'utilisateur avec FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Ajout des infos utilisateur dans Firestore
                        String userId = mAuth.getCurrentUser().getUid();
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

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validation des champs
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email et mot de passe requis.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Connexion utilisateur avec FirebaseAuth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                        // Ici, vous pouvez récupérer les données utilisateur depuis Firestore si nécessaire.
                    } else {
                        Log.e("FirebaseAuth", "Erreur : " + task.getException().getMessage());
                        Toast.makeText(this, "Erreur de connexion : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
