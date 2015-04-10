package cn.xing.mypassword.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import cn.xing.mypassword.R;
import cn.xing.mypassword.adapter.ViewHolder;
import cn.xing.mypassword.model.Password;
import cn.xing.mypassword.model.PasswordItem;
import cn.xing.mypassword.service.Mainbinder;
import cn.zdx.lib.annotation.FindViewById;
import cn.zdx.lib.annotation.ViewFinder;
import cn.zdx.lib.annotation.XingAnnotationHelper;

/**
 * 创建密码分组对话框
 */
public class PasswordDialog extends Dialog {

    private Mainbinder mainbinder;
    private Password password;
    
    @FindViewById(R.id.btn_close)
    private View closeButton;

    public PasswordDialog(Context context, Mainbinder mainbinder, Password p) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.mainbinder = mainbinder;
        this.password = p;
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_password_detail);
        initView();
    }

    private void initView() {
        XingAnnotationHelper.findView(this, ViewFinder.create(this));
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });
        ViewHolder viewHolder = new ViewHolder(getContext(), password.getGroupName(), mainbinder);
        XingAnnotationHelper.findView(viewHolder, ViewFinder.create(this));
        viewHolder.copyView.setOnClickListener(viewHolder);
        viewHolder.deleteView.setOnClickListener(viewHolder);
        viewHolder.editView.setOnClickListener(viewHolder);
        viewHolder.bindView(new PasswordItem(getContext(), password));
        
    }

}
