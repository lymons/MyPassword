package cn.xing.mypassword.adapter;

import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.xing.mypassword.R;
import cn.xing.mypassword.activity.EditPasswordActivity;
import cn.xing.mypassword.model.PasswordItem;
import cn.xing.mypassword.service.Mainbinder;
import cn.zdx.lib.annotation.FindViewById;

public class ViewHolder implements android.view.View.OnClickListener {
    @FindViewById(R.id.main_item_title)
    public TextView titleView;

    @FindViewById(R.id.main_item_date)
    public TextView dateView;

    @FindViewById(R.id.main_item_name)
    public TextView nameView;

    @FindViewById(R.id.main_item_password)
    public TextView passwordView;

    @FindViewById(R.id.main_item_note)
    public TextView noteView;

    @FindViewById(R.id.main_item_note_container)
    public View noteConainer;

    @FindViewById(R.id.main_item_top)
    public View topIconView;

    @FindViewById(R.id.main_item_copy)
    public View copyView;

    @FindViewById(R.id.main_item_delete)
    public View deleteView;

    @FindViewById(R.id.main_item_edit)
    public View editView;

    private PasswordItem passwordItem;
    
    private Context context;
    private String passwordGroup;
    private Mainbinder mainbinder;

    public ViewHolder(Context c, String group, Mainbinder binder) {
        context = c;
        passwordGroup = group;
        mainbinder = binder;
    }
    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_item_copy:
                onCopyClick();
                break;
            case R.id.main_item_delete:
                onDeleteClick();
                break;
            case R.id.main_item_edit:
                onEditClick();
                break;

            default:
                break;
        }
    }

    private void onCopyClick() {
        Builder builder = new Builder(context);

        String[] item = new String[] { context.getResources().getString(R.string.copy_name),
                context.getResources().getString(R.string.copy_password) };

        builder.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // 复制名字
                        ClipboardManager cmbName = (ClipboardManager) context
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipDataName = ClipData.newPlainText(null, passwordItem.password.getUserName());
                        cmbName.setPrimaryClip(clipDataName);
                        Toast.makeText(context, R.string.copy_name_toast, Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        // 复制密码
                        ClipboardManager cmbPassword = (ClipboardManager) context
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(null, passwordItem.password.getPassword());
                        cmbPassword.setPrimaryClip(clipData);
                        Toast.makeText(context, R.string.copy_password_toast, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    private void onEditClick() {
        Intent intent = new Intent(context, EditPasswordActivity.class);
        intent.putExtra(EditPasswordActivity.ID, passwordItem.password.getId());
        intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, passwordGroup);
        context.startActivity(intent);
    }

    private void onDeleteClick() {
        Builder builder = new Builder(context);
        builder.setMessage(R.string.alert_delete_message);
        builder.setTitle(passwordItem.password.getTitle());
        builder.setNeutralButton(R.string.yes, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainbinder.deletePassword(passwordItem.password.getId());
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    public void bindView(PasswordItem passwordItem) {
        this.passwordItem = passwordItem;
        titleView.setText(passwordItem.password.getTitle());
        dateView.setText(passwordItem.dataString);
        nameView.setText(passwordItem.password.getUserName());
        passwordView.setText(passwordItem.password.getPassword());

        String note = passwordItem.password.getNote();
        if (TextUtils.isEmpty(note)) {
            noteConainer.setVisibility(View.GONE);
        } else {
            noteConainer.setVisibility(View.VISIBLE);
            noteView.setText(note);
        }

        if (passwordItem.password.isTop()) {
            topIconView.setVisibility(View.VISIBLE);
            dateView.setTextColor(context.getResources().getColor(R.color.title_color));
        } else {
            topIconView.setVisibility(View.GONE);
            dateView.setTextColor(context.getResources().getColor(R.color.text_color));
        }
    }
}
