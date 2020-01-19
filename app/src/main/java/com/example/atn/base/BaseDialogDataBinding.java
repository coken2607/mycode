package com.example.atn.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;

public abstract class BaseDialogDataBinding<B extends ViewDataBinding, V extends AndroidViewModel>
        extends Dialog {

    protected B mBinding;

    protected V mViewModel;

    protected abstract int getContentViewLayoutId();

    public abstract Context context();

    protected abstract void initListeners();

    protected abstract void initData();

    protected abstract void subscribeToViewModel();

    public BaseDialogDataBinding(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context()), getContentViewLayoutId(),
                null, false);
        setContentView(mBinding.getRoot());
        initListeners();
        initData();
        subscribeToViewModel();
    }
}

