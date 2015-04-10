package cn.xing.mypassword.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import cn.xing.mypassword.R;
import cn.xing.mypassword.model.Password;
import cn.xing.mypassword.model.PasswordItem;
import cn.xing.mypassword.service.Mainbinder;
import cn.zdx.lib.annotation.XingAnnotationHelper;

/**
 * 主界面密码适配器
 * 
 * @author zengdexing
 * 
 */
public class PasswordListAdapter extends BaseAdapter {
	private List<PasswordItem> passwords = new ArrayList<PasswordItem>();
	private Context context;
	
	private int padding;
	private Mainbinder mainbinder;

	private String passwordGroup;

	private Comparator<PasswordItem> comparator = new Comparator<PasswordItem>() {
		@Override
		public int compare(PasswordItem lhs, PasswordItem rhs) {
			// 置顶排序
			if (lhs.password.isTop() || rhs.password.isTop()) {
				if (lhs.password.isTop() && rhs.password.isTop()) {
					return (int) (rhs.password.getCreateDate() - lhs.password.getCreateDate());
				} else if (lhs.password.isTop()) {
					return -1;
				} else {
					return 1;
				}
			}
			long value = rhs.password.getCreateDate() - lhs.password.getCreateDate();
			if (value > 0)
				return 1;
			else if (value == 0)
				return 0;
			else
				return -1;
		}
	};

	public int dip2px(float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public PasswordListAdapter(Context context) {
		this.context = context;
		padding = dip2px(6);
	}

	public void setData(List<Password> passwords, Mainbinder mainbinder) {
		this.mainbinder = mainbinder;
		this.passwords.clear();
		for (Password password : passwords) {
			this.passwords.add(new PasswordItem(context, password));
		}
		Collections.sort(this.passwords, comparator);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return passwords.size();
	}

	@Override
	public PasswordItem getItem(int position) {
		return passwords.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void notifyDataSetChanged() {
		for (PasswordItem passwordItem : passwords) {
			passwordItem.initDataString();
		}
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder(context, passwordGroup, mainbinder);
			convertView = LayoutInflater.from(context).inflate(R.layout.main_password_item, parent, false);
			convertView.setTag(viewHolder);
			XingAnnotationHelper.findView(viewHolder, convertView);
			viewHolder.copyView.setOnClickListener(viewHolder);
			viewHolder.deleteView.setOnClickListener(viewHolder);
			viewHolder.editView.setOnClickListener(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (position == 0) {
			convertView.setPadding(padding, padding, padding, padding);
		} else {
			convertView.setPadding(padding, 0, padding, padding);
		}

		PasswordItem passwordItem = getItem(position);

		viewHolder.bindView(passwordItem);

		return convertView;
	}

	public void onNewPassword(Password password) {
		passwords.add(0, new PasswordItem(context, password));
		Collections.sort(this.passwords, comparator);
		notifyDataSetChanged();
	}

	public void onDeletePassword(int id) {
		for (int i = 0; i < passwords.size(); i++) {
			PasswordItem passwordItem = passwords.get(i);
			if (passwordItem.password.getId() == id) {
				passwords.remove(i);
				break;
			}
		}
		notifyDataSetChanged();
	}

	public void onUpdatePassword(Password newPassword) {
		boolean needSort = false;

		boolean hasFind = false;

		for (int i = 0; i < passwords.size(); i++) {
			Password oldPassword = passwords.get(i).password;
			if (oldPassword.getId() == newPassword.getId()) {
				if (newPassword.getCreateDate() != 0)
					oldPassword.setCreateDate(newPassword.getCreateDate());
				if (newPassword.getTitle() != null)
					oldPassword.setTitle(newPassword.getTitle());
				if (newPassword.getUserName() != null)
					oldPassword.setUserName(newPassword.getUserName());
				if (newPassword.getPassword() != null)
					oldPassword.setPassword(newPassword.getPassword());
				if (newPassword.getNote() != null)
					oldPassword.setNote(newPassword.getNote());
				if (oldPassword.isTop() != newPassword.isTop()) {
					oldPassword.setTop(newPassword.isTop());
					needSort = true;
				}
				if (!oldPassword.getGroupName().equals(newPassword.getGroupName()))
					passwords.remove(i);
				hasFind = true;
				break;
			}
		}

		if (!hasFind) {
			passwords.add(0, new PasswordItem(context, newPassword));
			needSort = true;
		}

		if (needSort)
			Collections.sort(this.passwords, comparator);
		notifyDataSetChanged();
	}

	public void setPasswordGroup(String passwordGroup) {
		this.passwordGroup = passwordGroup;
	}
}
