package com.example.gotoesig.ui.logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.AuthActivity;
import com.example.gotoesig.databinding.FragmentLogoutBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutFragment extends Fragment {

    private FragmentLogoutBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogoutBinding.inflate(inflater, container, false);

        // Déconnexion lorsque le bouton est cliqué
        binding.btnLogout.setOnClickListener(v -> logoutUser());

        return binding.getRoot();
    }

    private void logoutUser() {
        // Déconnexion de Firebase
        FirebaseAuth.getInstance().signOut();

        // Retour à l'activité d'authentification
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
