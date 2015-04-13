package cn.xing.mypassword.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import cn.xing.mypassword.R;
import cn.xing.mypassword.activity.fragment.PasswordGroupFragment;
import cn.xing.mypassword.activity.fragment.PasswordListFragment;
import cn.xing.mypassword.activity.fragment.PasswordGroupFragment.OnPasswordGroupSelected;
import cn.xing.mypassword.app.MyApplication;
import cn.xing.mypassword.dialog.ExportDialog;
import cn.xing.mypassword.dialog.ImportDialog;
import cn.xing.mypassword.dialog.PasswordDialog;
import cn.xing.mypassword.model.Password;
import cn.xing.mypassword.model.SettingKey;
import cn.xing.mypassword.service.MainService;
import cn.xing.mypassword.service.Mainbinder;
import cn.xing.mypassword.service.OnGetAllPasswordCallback;
import cn.zdx.lib.annotation.FindViewById;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * 主界面
 * 
 * @author zengdexing
 * 
 */
public class MainActivity extends BaseActivity implements OnQueryTextListener, OnCloseListener, OnActionExpandListener {
	/** 数据源 */
	private Mainbinder mainbinder;
	private long lastBackKeyTime;

	@FindViewById(R.id.drawer_layout)
	private DrawerLayout drawerLayout;

	private ActionBarDrawerToggle mDrawerToggle;

	private PasswordListFragment passwordListFragment;
	private PasswordGroupFragment passwordGroupFragment;
	private SearchListFragment searchListFragment;

	@FindViewById(R.id.navigation_drawer)
	private View drawerView;
	
	private boolean isRecoveriedFromSearch;
	private String lastQueryString;

	private OnPasswordGroupSelected onPasswordGroupSelected = new OnPasswordGroupSelected() {
		@Override
		public void onPasswordGroupSelected(String passwordGroupName) {
			drawerLayout.closeDrawer(drawerView);
			if (passwordListFragment != null)
				passwordListFragment.showPasswordGroup(passwordGroupName);
		}
	};

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mainbinder = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mainbinder = (Mainbinder) service;
			initFragment();
		}
	};

	private void initFragment() {
		FragmentManager fragmentManager = getFragmentManager();

		passwordListFragment = (PasswordListFragment) fragmentManager.findFragmentByTag("PasswordListFragment");
		if (passwordListFragment == null)
			passwordListFragment = new PasswordListFragment();
		passwordListFragment.setDataSource(mainbinder);

		passwordGroupFragment = (PasswordGroupFragment) fragmentManager.findFragmentByTag("PasswordGroupFragment");
		if (passwordGroupFragment == null)
			passwordGroupFragment = new PasswordGroupFragment();
		passwordGroupFragment.setDataSource(mainbinder, onPasswordGroupSelected);

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.navigation_drawer, passwordGroupFragment, "PasswordGroupFragment");
		fragmentTransaction.replace(R.id.container, passwordListFragment, "PasswordListFragment");
		fragmentTransaction.commitAllowingStateLoss();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		setContentView(R.layout.activity_main);

		initDrawer();

		Intent intent = new Intent(this, MainService.class);
		this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

		// 友盟自动升级
		UmengUpdateAgent.update(this);
	}

	private void initDrawer() {
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.drawable.ic_drawer,
				R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActivity().invalidateOptionsMenu();
				getActionBar().setTitle(R.string.app_name);
			}

			@Override
			public void onDrawerClosed(View drawerView) {
			    onClose();
				super.onDrawerClosed(drawerView);
				getActivity().invalidateOptionsMenu();
				if (passwordListFragment != null && !passwordListFragment.getPasswordGroupName().equals(""))
					getActionBar().setTitle(passwordListFragment.getPasswordGroupName());
				else {
					getActionBar().setTitle(R.string.app_name);
				}
			}
		};

		drawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});
		drawerLayout.setDrawerListener(mDrawerToggle);

		if (getSetting(SettingKey.IS_SHOWED_DRAWER, "false").equals("false")) {
			putSetting(SettingKey.IS_SHOWED_DRAWER, "true");
			drawerLayout.openDrawer(drawerView);
		} else {
			String lastGroupName = getSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME, "");
			if (lastGroupName.equals(""))
				lastGroupName = getString(R.string.app_name);
			getActionBar().setTitle(lastGroupName);
		}
		
		isRecoveriedFromSearch = true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}

	private boolean isExistSDCard() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		int id = item.getItemId();
		switch (id) {
			case R.id.action_add_password:
				if (mainbinder == null)
					break;
				Intent intent = new Intent(this, EditPasswordActivity.class);
				if (passwordListFragment != null)
					intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, passwordListFragment.getPasswordGroupName());
				startActivity(intent);
				break;

			case R.id.action_import:
				// 密码导入
				if (mainbinder == null)
					break;
				ImportDialog importDialog = new ImportDialog(getActivity(), mainbinder);
				importDialog.show();
				break;

			case R.id.action_export:
				// 密码导出
				if (mainbinder == null)
					break;
				if (!isExistSDCard()) {
					showToast(R.string.export_no_sdcard);
					break;
				}
				ExportDialog exportDialog = new ExportDialog(this, mainbinder);
				exportDialog.show();
				break;

			case R.id.action_set_lock_pattern:
				// 软件锁
				startActivity(new Intent(this, SetLockpatternActivity.class));
				break;
			case R.id.action_set_effect:
				// 列表特效
				onEffectClick();
				break;
			case R.id.action_about:
				// 关于
				startActivity(new Intent(this, AboutActivity.class));
				break;
			case R.id.action_exit:
				// 退出
				finish();
				break;
			case R.id.menu_search:
			    break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!drawerLayout.isDrawerOpen(drawerView)) {
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		} else {
//			return super.onCreateOptionsMenu(menu);
			getMenuInflater().inflate(R.menu.group_password, menu);
			setUpSearchView(menu);
			return true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				long delay = Math.abs(System.currentTimeMillis() - lastBackKeyTime);
				if (delay > 4000) {
					// 双击退出程序
					showToast(R.string.toast_key_back);
					lastBackKeyTime = System.currentTimeMillis();
					return true;
				}
				break;

			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void setUpSearchView(Menu menu){
	    MenuItem mi = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) mi.getActionView();
        if(searchView == null){
            return;
        }
        searchView.setIconifiedByDefault(true);
        searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        mi.setOnActionExpandListener(this);
        
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        ComponentName cn = new ComponentName(this, SearchResultPasswordActivity.class);
//        
//        SearchableInfo info = searchManager.getSearchableInfo(cn);
//        if(info == null){
//            return;
//        }
//        
//        searchView.setSearchableInfo(info);
    }

	private void onEffectClick() {
		if (getSetting(SettingKey.JAZZY_EFFECT_INTRODUCTION, "false").equals("false")) {
			putSetting(SettingKey.JAZZY_EFFECT_INTRODUCTION, "true");
			Builder builder = new Builder(this);
			builder.setMessage(R.string.action_jazzy_effect_introduction);
			builder.setNeutralButton(R.string.i_known, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onEffectClick();
				}
			});
			builder.show();
		} else {
			Builder builder = new Builder(this);
			builder.setTitle(R.string.action_jazzy_effect);

			final String[] effectArray = getResources().getStringArray(R.array.jazzy_effects);
			builder.setItems(effectArray, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getActivity().putSetting(SettingKey.JAZZY_EFFECT, which + "");
					onEventEffect(effectArray[which]);
				}
			});
			builder.show();
		}
	}

	/**
	 * 友盟的事件统计“effect”
	 * 
	 * @param effect
	 */
	private void onEventEffect(String effect) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("effect", effect);
		MobclickAgent.onEvent(getActivity(), "effect", map);
	}
	
	/**
	 * SearchViewに入力するたびに入力される
	 */
	@Override
	public boolean onQueryTextChange(String newText) {
	    if (TextUtils.isEmpty(newText)) {
	        onClose();
	    } else {
	        this.lastQueryString = newText;
	        showSearchResultFragment();
	        if (searchListFragment != null) {
	            searchListFragment.setFilter(newText.toString());
	        }
	    }
        
	    return true;
	}
	 
	/**
	 * SearchViewのSubmitを押下したときに呼び出される
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {
	    if (TextUtils.isEmpty(query) == false && searchListFragment != null && searchListFragment.isVisible()) {
            searchListFragment.searchTitle(query);
        }
	    return false;
	}
	
	@Override
    public boolean onClose() {
	    if (isRecoveriedFromSearch) {
            return false;
        }
	    isRecoveriedFromSearch = true;
	    
	    FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag("SearchListFragment");
        if (fragment != null) {
            fragmentTransaction.hide(fragment);
        }
        fragmentTransaction.show(passwordGroupFragment);
        fragmentTransaction.commitAllowingStateLoss();
        
	    return false;
	}
	
	@Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        onClose();
        return true;
    }
    
    private void showSearchResultFragment() {
        if (isRecoveriedFromSearch == false) {
            return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag("PasswordGroupFragment");
        if (fragment != null) {
            fragmentTransaction.hide(fragment);
        }
        searchListFragment = (SearchListFragment) fragmentManager.findFragmentByTag("SearchListFragment");
        if (searchListFragment == null || searchListFragment.isAdded() == false) {
            searchListFragment = new SearchListFragment();
            fragmentTransaction.add(R.id.navigation_drawer, searchListFragment, "SearchListFragment");
        }
        fragmentTransaction.show(searchListFragment);
        fragmentTransaction.commitAllowingStateLoss();
        isRecoveriedFromSearch = false;
        if (((MyApplication)getApplication()).isPasswordChanged()) {
            searchListFragment.getAllPasswordTitle();
            ((MyApplication)getApplication()).setPasswordChanged(false);
        }
    }
	
	private class SearchListFragment extends ListFragment implements OnGetAllPasswordCallback {
	    private String[] rows;
	    private List<Password> mPasswords;
	    private boolean showingOriginalData;
	 
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        // TODO Auto-generated method stub
	        View view = inflater.inflate(R.layout.search_result, container, false);
	        return view;
	    }
	    
	    @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	        super.onActivityCreated(savedInstanceState);
	        
	        getAllPasswordTitle();
	 
	        // ListViewにFilterをかけれるようにする
	        getListView().setTextFilterEnabled(true);
	    }
	    
	    @Override
	    public void onListItemClick(ListView l, View v, int position, long id) {
	        Password p = mPasswords.get(position);
	        PasswordDialog cDialog = new PasswordDialog(getActivity(), mainbinder, p);
            cDialog.show();
	    }
	 
	    /**
	     * ListViewにFilterをかける
	     * @param s
	     */
	    public void setFilter(String s){
	        if (rows == null) {
                return;
            }
	        if (showingOriginalData == false && rows.length > 0) {
	            setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.search_result_item, R.id.sitename, rows));
	            this.showingOriginalData = true;
            }
	        if (rows.length > 0) {
	            getListView().setFilterText(s);
            }
	    }
	 
	    /**
	     * ListViewのFilterをClearする
	     */
	    public void clearFilter(){
	        getListView().clearTextFilter();
	    }
	    
	    public void getAllPasswordTitle() {
	        mainbinder.getAllPassword(this);
	    }

        @Override
        public void onGetAllPassword(String froupName, List<Password> passwords) {
            this.mPasswords = passwords;
            int count = passwords.size();
            rows = new String[count];
            for (int i = 0; i < count; i ++) {
                Password p = passwords.get(i);
                rows[i] = p.getTitle();
            }
            
            // ListViewに表示するItemの設定
            setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.search_result_item, R.id.sitename, rows));
            this.showingOriginalData = true;
            setFilter(lastQueryString);
        }
        
        public void searchTitle(String target) {
            ArrayList<String> meets = new ArrayList<String>();
            for (int i = 0; i < rows.length; i ++) {
                String title = rows[i];
                if (title.indexOf(target) >= 0) {
                    meets.add(title);
                }
            }
            // ListViewに表示するItemの設定
            setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.search_result_item, R.id.sitename, meets.toArray(new String[0])));
            this.showingOriginalData = false;
        }
	}
	
}
