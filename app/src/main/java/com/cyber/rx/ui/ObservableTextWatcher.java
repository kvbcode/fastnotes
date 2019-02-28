package com.cyber.rx.ui;

import android.text.Editable;
import android.text.TextWatcher;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ObservableTextWatcher implements TextWatcher {
    private PublishSubject<String> pubChanged = PublishSubject.create();

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override public void afterTextChanged(Editable s) { pubChanged.onNext(s.toString()); }

    public Observable<String> getOnChangedObservable(){ return pubChanged; }
}
