package cn.xing.mypassword.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import cn.xing.mypassword.R;
import cn.xing.mypassword.app.BaseActivity;

public class SearchResultPasswordActivity extends BaseActivity {
    private MyFragment current = null;
    
    private class MyFragment extends ListFragment {
        private final String[] rows = { "abc", "aab", "aac", "aaa", "abb",
                "acc", "cab", "ccc", "bbb" };
     
        MyFragment() {
        }
     
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
     
            // ListViewにFilterをかけれるようにする
            getListView().setTextFilterEnabled(true);
     
            // ListViewに表示するItemの設定
            setListAdapter(new ArrayAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, rows));
        }
     
        /**
         * ListViewにFilterをかける
         * @param s
         */
        public void setFilter(String s){
            getListView().setFilterText(s);
        }
     
        /**
         * ListViewのFilterをClearする
         */
        public void clearFilter(){
            getListView().clearTextFilter();
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_password);

        initActionBar();
        
        // ListViewを表示するFragment
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        current = new MyFragment();
        ft.replace(R.id.container, current, "MyFragment");
        ft.commit();

    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        doSearchQuery(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
    
    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.search_result);
    }
    
    private void doSearchQuery(Intent intent){
        if(intent == null)
            return;
        
        String queryAction = intent.getAction();
        if(Intent.ACTION_SEARCH.equals(queryAction)){
            String queryString = intent.getStringExtra(SearchManager.QUERY);
            Log.w("Search", "搜索内容：" + queryString);
        }       
    }

}
