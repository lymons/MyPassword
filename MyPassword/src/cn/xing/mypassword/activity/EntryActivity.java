package cn.xing.mypassword.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import cn.xing.mypassword.R;
import cn.xing.mypassword.app.BaseActivity;
import cn.xing.mypassword.view.LockPatternUtil;
import cn.xing.mypassword.view.LockPatternView;
import cn.xing.mypassword.view.LockPatternView.Cell;
import cn.xing.mypassword.view.LockPatternView.DisplayMode;
import cn.xing.mypassword.view.LockPatternView.OnPatternListener;
import cn.zdx.lib.annotation.FindViewById;

/**
 * ��ڣ���ӭҳ
 * 
 * @author zengdexing
 * 
 */
public class EntryActivity extends BaseActivity implements Callback, OnPatternListener {
    public static final String KEY_LOAD_AUTO = "KEY_LOAD_AUTO";
    
	@FindViewById(R.id.entry_activity_iconview)
	private View iconView;
	private Handler handler;

	private final int MESSAGE_START_MAIN = 1;
	private final int MESSAGE_CLEAR_LOCKPATTERNVIEW = 3;
	private final int MESSAGE_START_SETLOCKPATTERN = 4;
	private final int MESSAGE_START_BACK = 5;

	@FindViewById(R.id.entry_activity_bg)
	private View backgroundView;

	@FindViewById(R.id.entry_activity_lockPatternView)
	private LockPatternView lockPatternView;

	@FindViewById(R.id.entry_activity_tips)
	private TextView tipsView;
	
	private boolean isAutoLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry);
		
		Intent it = getIntent();
		isAutoLoading = it.getBooleanExtra(KEY_LOAD_AUTO, false);

		handler = new Handler(this);
		lockPatternView.setOnPatternListener(this);

		List<Cell> cells = LockPatternUtil.getLocalCell(this);
		if (cells.size() == 0) {
			// �״�ʹ�ã�û���������룬��ת��������ҳ
			lockPatternView.setEnabled(false);
			handler.sendEmptyMessageDelayed(MESSAGE_START_SETLOCKPATTERN, 2000);
		}

		tipsView.setText("");
		initAnimation();
		checkPackageName();
	}
	
	

	@Override
    protected void onResume() {
        super.onResume();
    }

    /**
	 * ����������ֹ�������򵥵����δ��
	 */
	private void checkPackageName() {
		if (!getPackageName().equals(getString(R.string.package_name)))
			finish();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MESSAGE_START_SETLOCKPATTERN:
				startActivity(new Intent(this, SetLockpatternActivity.class));
				finish();
				break;

			case MESSAGE_START_MAIN:
				Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				break;
				
			case MESSAGE_START_BACK:
			    finish();
			    break;

			case MESSAGE_CLEAR_LOCKPATTERNVIEW:
				lockPatternView.clearPattern();
				tipsView.setText("");
				break;

			default:
				break;
		}
		return true;
	}

	/**
	 * ͼ�궯��
	 */
	private void initAnimation() {
		Animation iconAnimation = AnimationUtils.loadAnimation(this, R.anim.entry_animation_icon);
		iconView.startAnimation(iconAnimation);

		backgroundView.startAnimation(getAlpAnimation());
		lockPatternView.startAnimation(getAlpAnimation());
		tipsView.startAnimation(getAlpAnimation());
	}

	private Animation getAlpAnimation() {
		return AnimationUtils.loadAnimation(this, R.anim.entry_animation_alpha_from_0_to_1);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPatternStart() {
		handler.removeMessages(MESSAGE_CLEAR_LOCKPATTERNVIEW);
		tipsView.setText("");
	}

	@Override
	public void onPatternCleared() {
	}

	@Override
	public void onPatternCellAdded(List<Cell> pattern) {
	}

	@Override
	public void onPatternDetected(List<Cell> pattern) {
		if (LockPatternUtil.checkPatternCell(LockPatternUtil.getLocalCell(this), pattern)) {
			// ��֤ͨ��
			lockPatternView.setDisplayMode(DisplayMode.Correct);
			if (isAutoLoading) {
			    handler.sendEmptyMessage(MESSAGE_START_BACK);
            } else {
                handler.sendEmptyMessage(MESSAGE_START_MAIN);
            }
		} else {
			// ��֤ʧ��
			lockPatternView.setDisplayMode(DisplayMode.Wrong);
			tipsView.setText(R.string.lock_pattern_error);
			handler.sendEmptyMessageDelayed(MESSAGE_CLEAR_LOCKPATTERNVIEW, 1000);
		}

	}
}
