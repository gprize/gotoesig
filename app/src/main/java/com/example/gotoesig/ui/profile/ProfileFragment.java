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
import androidx.lifecycle.ViewModelProvider;

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
    private ProfileViewModel profileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initUI();
        observeViewModel();

        binding.btnSave.setOnClickListener(v -> profileViewModel.saveUserInfo(
                binding.etFirstName.getText().toString().trim(),
                binding.etLastName.getText().toString().trim(),
                binding.etPhone.getText().toString().trim(),
                binding.etCity.getText().toString().trim()
        ));

        return binding.getRoot();
    }

    private void initUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            binding.etEmail.setText(user.getEmail()); // Email non modifiable
        }
    }

    private void observeViewModel() {
        profileViewModel.getUserData().observe(getViewLifecycleOwner(), userData -> {
            if (userData != null) {
                binding.etFirstName.setText((String) userData.get("name"));
                binding.etLastName.setText((String) userData.get("surname"));
                binding.etPhone.setText((String) userData.get("phone"));
                binding.etCity.setText((String) userData.get("city"));

                String firstName = (String) userData.get("name");
                String lastName = (String) userData.get("surname");
                binding.ivProfilePhoto.setText(
                        firstName.substring(0, 1).toUpperCase() + " " +
                                lastName.substring(0, 1).toUpperCase()
                );
            }
        });

        profileViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null) {
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        profileViewModel.loadUserData(); // Recharger les données utilisateur
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
