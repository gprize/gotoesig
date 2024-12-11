package com.example.gotoesig.ui.logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gotoesig.AuthActivity;
import com.example.gotoesig.databinding.FragmentLogoutBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutFragment extends Fragment {

    private FragmentLogoutBinding binding;
    private FirebaseAuth auth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Initialisation du ViewModel (si nécessaire)
        LogoutViewModel slideshowViewModel = new ViewModelProvider(this).get(LogoutViewModel.class);

        // Récupération du binding pour lier le layout XML
        binding = FragmentLogoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialisation de Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Ajouter un bouton pour gérer la déconnexion
        Button btnLogout = binding.btnLogout;
        btnLogout.setOnClickListener(v -> logoutUser());

        return root;
    }

    // Méthode pour déconnecter l'utilisateur et revenir à l'écran de connexion
    private void logoutUser() {
        // Déconnexion de Firebase
        auth.signOut();

        // Fermer toutes les activités
        if (getActivity() != null) {
            getActivity().finishAffinity(); // Ferme toutes les activités

            // Lancer l'activité d'authentification
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Assure que l'AuthActivity soit la seule activité ouverte
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
