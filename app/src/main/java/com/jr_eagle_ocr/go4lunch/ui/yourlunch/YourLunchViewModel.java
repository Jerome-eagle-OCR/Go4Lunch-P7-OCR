package com.jr_eagle_ocr.go4lunch.ui.yourlunch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class YourLunchViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public YourLunchViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}