package com.jr_eagle_ocr.go4lunch.ui.logout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LogOutViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LogOutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Vous allez être déconnecté.e");
    }

    public LiveData<String> getText() {
        return mText;
    }
}