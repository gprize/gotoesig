package com.example.gotoesig.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<Map<String, Object>> userData = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public ProfileViewModel() {
        loadUserData();
    }

    public LiveData<Map<String, Object>> getUserData() {
        return userData;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            firestore.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("name", documentSnapshot.getString("name"));
                            data.put("surname", documentSnapshot.getString("surname"));
                            data.put("phone", documentSnapshot.getString("phone"));
                            data.put("city", documentSnapshot.getString("city"));
                            userData.setValue(data);
                        }
                    })
                    .addOnFailureListener(e -> error.setValue("Erreur lors de la récupération des informations."));
        }
    }

    public void saveUserInfo(String firstName, String lastName, String phone, String city) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", firstName);
            userInfo.put("surname", lastName);
            userInfo.put("phone", phone);
            userInfo.put("city", city);

            firestore.collection("users").document(user.getUid())
                    .update(userInfo)
                    .addOnSuccessListener(aVoid -> successMessage.setValue("Informations enregistrées avec succès."))
                    .addOnFailureListener(e -> error.setValue("Erreur lors de l'enregistrement des informations."));
        }
    }
}
