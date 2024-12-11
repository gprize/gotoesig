package com.example.gotoesig.ui.searchtrips;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gotoesig.databinding.FragmentSearchTripBinding;

public class SearchTripFragment extends Fragment {

    private FragmentSearchTripBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialiser le ViewModel
        SearchTripViewModel searchTripViewModel = new ViewModelProvider(this).get(SearchTripViewModel.class);

        // Lier le layout via ViewBinding
        binding = FragmentSearchTripBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Exemple d'interaction avec le ViewModel
        final TextView textView = binding.textSearchTrip;
        searchTripViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}