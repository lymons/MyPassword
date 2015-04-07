package cn.xing.mypassword.app;

import cn.xing.mypassword.model.SettingKey;

/**
 * 用户设置变化监听器
 */
public interface OnSettingChangeListener {
	void onSettingChange(SettingKey key);
}
