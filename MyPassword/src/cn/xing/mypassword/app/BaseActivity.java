package cn.xing.mypassword.app;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import cn.xing.mypassword.activity.EntryActivity;
import cn.xing.mypassword.model.SettingKey;
import cn.zdx.lib.annotation.FindViewById;
import cn.zdx.lib.annotation.ViewFinder;
import cn.zdx.lib.annotation.XingAnnotationHelper;

import com.umeng.analytics.MobclickAgent;

/**
 * ��������࣬���е�Activity��Ҫ�̳б��࣬ʵ��������ͳ��
 *
 */
public class BaseActivity extends Activity {
    
    protected BroadcastReceiver actionCloseBroadcastReceiver = null;
    protected void creatActionCloseBroadcastReceiver() {
        actionCloseBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((MyApplication)getApplication()).setAlreadyLeftFromForeground(true);
            }
        };
    }

    protected BroadcastReceiver actionScreenOffBroadcastReceiver = null;
    protected void creatActionScreenOffBroadcastReceiver() {
        actionScreenOffBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((MyApplication)getApplication()).setAlreadyLeftFromForeground(true);
            }
        };
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (actionCloseBroadcastReceiver == null) {
            creatActionCloseBroadcastReceiver();
        }
        registerReceiver(actionCloseBroadcastReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        if (actionScreenOffBroadcastReceiver == null) {
            creatActionScreenOffBroadcastReceiver();
        }

        registerReceiver(actionScreenOffBroadcastReceiver, new IntentFilter(
                Intent.ACTION_SCREEN_OFF));
    }


    @Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		
		if (((MyApplication)getApplication()).isAlreadyLeftFromForeground()) {
		    ((MyApplication)getApplication()).setAlreadyLeftFromForeground(false);
            Intent it = new Intent(this, EntryActivity.class);
            it.putExtra(EntryActivity.KEY_LOAD_AUTO, true);
            startActivity(it);
        }
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
    protected void onDestroy() {
	    if (actionCloseBroadcastReceiver != null) {
            unregisterReceiver(actionCloseBroadcastReceiver);
            actionCloseBroadcastReceiver = null;
        }
        
        if (actionScreenOffBroadcastReceiver != null) {
            unregisterReceiver(actionScreenOffBroadcastReceiver);
            actionScreenOffBroadcastReceiver = null;
        }
        
        super.onDestroy();
    }


    public BaseActivity getActivity() {
		return this;
	}

	public void showToast(int id) {
		showToast(id, Toast.LENGTH_SHORT);
	}

	public void showToast(int id, int duration) {
		Toast.makeText(this, id, duration).show();
	}

	public MyApplication getMyApplication() {
		return (MyApplication) getApplication();
	}

	public String getSetting(SettingKey key, String defValue) {
		return getMyApplication().getSetting(key, defValue);
	}

	public void putSetting(SettingKey key, String value) {
		getMyApplication().putSetting(key, value);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		initAnnotation();
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		initAnnotation();
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		initAnnotation();
	}

	private void initAnnotation() {
		ViewFinder viewFinder = ViewFinder.create(this);
		Class<?> clazz = getClass();
		do {
			findView(clazz, viewFinder);
		} while ((clazz = clazz.getSuperclass()) != BaseActivity.class);
	}

	/** ��ʼ�� {@link FindViewById} */
	private void findView(Class<?> clazz, ViewFinder viewFinder) {
		Field[] fields = clazz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				XingAnnotationHelper.findView(this, field, viewFinder);
			}
		}
	}
}
