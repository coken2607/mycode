package com.example.atn.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;

public abstract class BaseDataBindingFragment<B extends ViewDataBinding, V extends AndroidViewModel>
        extends Fragment {

    public B mBinding;

    protected V mViewModel;

    protected abstract int getContentViewLayoutId();

    public abstract Context context();

    protected abstract void initListeners();

    protected abstract void initData();

    protected abstract void subscribeToViewModel();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, getContentViewLayoutId(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListeners();
        initData();
        subscribeToViewModel();
    }

    public void onCreateFragment() {

    }
}
