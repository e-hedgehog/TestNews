package com.ehedgehog.android.testnews.presenter;

public abstract class BasePresenter<M, V> {

    protected M mModel;
    private V mView;

    public void bindView(V view) {
        mView = view;
    }

    public void unbindView() {
        mView = null;
    }

    public void setModel(M model) {
        mModel = model;
    }

    public V getView() {
        return mView;
    }
}
