package com.example.gotoesig.ui.searchtrips;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SearchTripViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SearchTripViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Bienvenue sur la recherche de trajets");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
