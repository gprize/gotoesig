package com.example.gotoesig.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private EditText etFirstName, etLastName, etPhone, etCity, etEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialisation des EditText pour permettre la modification
        etFirstName = binding.etFirstName;
        etLastName = binding.etLastName;
        etPhone = binding.etPhone;
        etCity = binding.etCity;
        etEmail = binding.etEmail;

        // Affichage des informations de l'utilisateur
        displayUserInfo();

        // Enregistrer les nouvelles informations
        binding.btnSave.setOnClickListener(v -> saveUserInfo());

        return binding.getRoot();
    }

    private void displayUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Affichage de l'email (l'utilisateur ne peut pas le modifier)
            etEmail.setText(user.getEmail());

            // Récupérer les autres informations de l'utilisateur depuis Firestore
            DocumentReference userRef = firestore.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString("name");
                    String lastName = documentSnapshot.getString("surname");
                    String phone = documentSnapshot.getString("phone");
                    String city = documentSnapshot.getString("city");

                    // Affichage des informations récupérées dans les champs modifiables
                    etFirstName.setText(firstName);
                    etLastName.setText(lastName);
                    etPhone.setText(phone);
                    etCity.setText(city);
                    binding.ivProfilePhoto.setText(firstName.substring(0,1).toUpperCase()+" "+lastName.substring(0,1).toUpperCase());
                }
            }).addOnFailureListener(e -> {
                // Gestion des erreurs de récupération
                Toast.makeText(getContext(), "Erreur lors de la récupération des informations", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Récupérer les nouvelles informations des EditText
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String city = etCity.getText().toString().trim();

            // Créer une Map pour stocker les nouvelles valeurs
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", firstName);
            userInfo.put("surname", lastName);
            userInfo.put("phone", phone);
            userInfo.put("city", city);

            // Mettre à jour Firestore avec les nouvelles informations
            DocumentReference userRef = firestore.collection("users").document(user.getUid());
            userRef.update(userInfo).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Informations enregistrées avec succès", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Erreur lors de l'enregistrement des informations", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
