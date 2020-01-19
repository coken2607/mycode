package com.example.atn.dialog;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import androidx.lifecycle.AndroidViewModel;

import com.example.atn.R;
import com.example.atn.base.BaseDialogDataBinding;
import com.example.atn.databinding.DialogRecoverPassBinding;

import java.util.Objects;

public class DialogRecoverPass extends BaseDialogDataBinding<DialogRecoverPassBinding, AndroidViewModel> {

    private DialogRecoverPass(Context context) {
        super(context);
    }

    public static DialogRecoverPass newInstance(Context context) {
        return new DialogRecoverPass(context);
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.dialog_recover_pass;
    }

    @Override
    public Context context() {
        return getContext();
    }

    @Override
    protected void initListeners() {
        mBinding.setOnClickCancel(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mBinding.setOnClickRecover(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Objects.requireNonNull(mBinding.emailEt.getText()).toString().trim();


            }
        });
    }

    @Override
    protected void initData() {
        if (getWindow() != null) {
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawableResource(R.color.white);
        }
    }

    @Override
    protected void subscribeToViewModel() {

    }
}
