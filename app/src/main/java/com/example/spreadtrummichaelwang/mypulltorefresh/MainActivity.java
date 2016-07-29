package com.example.spreadtrummichaelwang.mypulltorefresh;

import android.app.ExpandableListActivity;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.extras.SoundPullEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends ListActivity {

    static final int MENU_MANUAL_REFRESH = 0;
    static final int MENU_DISABLE_SCROLL = 1;
    static final int MENU_SET_MODE = 2;
    static final int MENU_DEMO = 3;

    private LinkedList<String> mListItems;
    private PullToRefreshListView mPullRefreshListView;
    private ArrayAdapter<String> mAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.expand_list);

        // Set a listener to be invoked when the list should be refreshed.
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                // Do work to refresh the list here.
                new GetDataTask().execute();
            }
        });

        // Add an end-of-list listener
        mPullRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                Toast.makeText(MainActivity.this, "End of List!", Toast.LENGTH_SHORT).show();
            }
        });

        ListView actualListView = mPullRefreshListView.getRefreshableView();

        // Need to use the Actual ListView when registering for Context Menu
        registerForContextMenu(actualListView);

        mListItems = new LinkedList<String>();
        mListItems.addAll(Arrays.asList(mStrings));

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListItems);

        /**
         * Add Sound Event Listener
         */
        SoundPullEventListener<ListView> soundListener = new SoundPullEventListener<ListView>(this);
        /*soundListener.addSoundEvent(PullToRefreshBase.State.PULL_TO_REFRESH, R.raw.pull_event);
        soundListener.addSoundEvent(PullToRefreshBase.State.RESET, R.raw.reset_sound);
        soundListener.addSoundEvent(PullToRefreshBase.State.REFRESHING, R.raw.refreshing_sound);*/
        mPullRefreshListView.setOnPullEventListener(soundListener);

        // You can also just use setListAdapter(mAdapter) or
        // mPullRefreshListView.setAdapter(mAdapter)
        actualListView.setAdapter(mAdapter);
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            // Simulates a background job.
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
            return mStrings;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mListItems.addFirst("Added after refresh...");
            mAdapter.notifyDataSetChanged();

            // Call onRefreshComplete when the list has been refreshed.
            mPullRefreshListView.onRefreshComplete();

            super.onPostExecute(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_MANUAL_REFRESH, 0, "Manual Refresh");
        menu.add(0, MENU_DISABLE_SCROLL, 1,
                mPullRefreshListView.isScrollingWhileRefreshingEnabled() ? "Disable Scrolling while Refreshing"
                        : "Enable Scrolling while Refreshing");
        menu.add(0, MENU_SET_MODE, 0, mPullRefreshListView.getMode() == PullToRefreshBase.Mode.BOTH ? "Change to MODE_PULL_DOWN"
                : "Change to MODE_PULL_BOTH");
        menu.add(0, MENU_DEMO, 0, "Demo");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        menu.setHeaderTitle("Item: " + getListView().getItemAtPosition(info.position));
        menu.add("Item 1");
        menu.add("Item 2");
        menu.add("Item 3");
        menu.add("Item 4");

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem disableItem = menu.findItem(MENU_DISABLE_SCROLL);
        disableItem
                .setTitle(mPullRefreshListView.isScrollingWhileRefreshingEnabled() ? "Disable Scrolling while Refreshing"
                        : "Enable Scrolling while Refreshing");

        MenuItem setModeItem = menu.findItem(MENU_SET_MODE);
        setModeItem.setTitle(mPullRefreshListView.getMode() == PullToRefreshBase.Mode.BOTH ? "Change to MODE_FROM_START"
                : "Change to MODE_PULL_BOTH");

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case MENU_MANUAL_REFRESH:
                new GetDataTask().execute();
                mPullRefreshListView.setRefreshing(false);
                break;
            case MENU_DISABLE_SCROLL:
                mPullRefreshListView.setScrollingWhileRefreshingEnabled(!mPullRefreshListView
                        .isScrollingWhileRefreshingEnabled());
                break;
            case MENU_SET_MODE:
                mPullRefreshListView.setMode(mPullRefreshListView.getMode() == PullToRefreshBase.Mode.BOTH ? PullToRefreshBase.Mode.PULL_FROM_START
                        : PullToRefreshBase.Mode.BOTH);
                break;
            case MENU_DEMO:
                mPullRefreshListView.demo();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private String[] mStrings = { "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
            "Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
            "Allgauer Emmentaler", "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
            "Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
            "Allgauer Emmentaler" };



//    private static final String KEY = "michael";
//
//    private List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
//    private List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
//
//    private PullToRefreshExpandableListView mPullRefreshListView;
//    private SimpleExpandableListAdapter mAdapter;
//
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        mPullRefreshListView = (PullToRefreshExpandableListView) findViewById(R.id.expand_list);
//        mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
//
//        // Set a listener to be invoked when the list should be refreshed.
////        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ExpandableListView>() {
////            @Override
////            public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
////                // Do work to refresh the list here.
////                //new GetDataTask().execute();
////                Log.d(KEY," setOnRefreshListener 1111  ");
////            }
////        });
//
//        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ExpandableListView>() {
//            @Override
//            public void onPullDownToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
//                Log.d(KEY," setOnRefreshListener 2222  onPullDownToRefresh ");
//                new GetDataTask().execute();
//            }
//
//            @Override
//            public void onPullUpToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
//                Log.d(KEY," setOnRefreshListener 2222 onPullUpToRefresh ");
//                new GetDataTask().execute();
//            }
//        });
//
//        for (String group : mGroupStrings) {
//            Map<String, String> groupMap1 = new HashMap<String, String>();
//            groupData.add(groupMap1);
//            groupMap1.put(KEY, group);
//
//            List<Map<String, String>> childList = new ArrayList<Map<String, String>>();
//            for (String string : mChildStrings) {
//                Map<String, String> childMap = new HashMap<String, String>();
//                childList.add(childMap);
//                childMap.put(KEY, string);
//            }
//            childData.add(childList);
//        }
//
//        mAdapter = new SimpleExpandableListAdapter(this, groupData, android.R.layout.simple_expandable_list_item_1,
//                new String[] { KEY }, new int[] { android.R.id.text1 }, childData,
//                android.R.layout.simple_expandable_list_item_2, new String[] { KEY }, new int[] { android.R.id.text1 });
//        setListAdapter(mAdapter);
//    }
//
//    private class GetDataTask extends AsyncTask<Void, Void, String[]> {
//
//        @Override
//        protected String[] doInBackground(Void... params) {
//            // Simulates a background job.
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//            }
//            return mChildStrings;
//        }
//
//        @Override
//        protected void onPostExecute(String[] result) {
//            Map<String, String> newMap = new HashMap<String, String>();
//            newMap.put(KEY, "Added after refresh...");
//            groupData.add(newMap);
//
//            List<Map<String, String>> childList = new ArrayList<Map<String, String>>();
//            for (String string : mChildStrings) {
//                Map<String, String> childMap = new HashMap<String, String>();
//                childMap.put(KEY, string);
//                childList.add(childMap);
//            }
//            childData.add(childList);
//
//            mAdapter.notifyDataSetChanged();
//
//            // Call onRefreshComplete when the list has been refreshed.
//            mPullRefreshListView.onRefreshComplete();
//
//            super.onPostExecute(result);
//        }
//    }
//
//    private String[] mChildStrings = { "Child One", "Child Two", "Child Three", "Child Four", "Child Five", "Child Six" };
//
//    private String[] mGroupStrings = { "Group One", "Group Two", "Group Three" };
/*


    private PullToRefreshListView mExpandList;
    private List<String> mDetalCommentData;
    private ArrayAdapter<String> mDetalCommentAdapter;
    private ArrayAdapter<String> mDetalCommentAdapter2;
    private String TAG = "michael";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExpandList = (PullToRefreshListView)findViewById(R.id.expand_list);
        //mExpandList.getRefreshableView().setGroupIndicator(null);
        mExpandList.getRefreshableView().setDivider(null);
        mExpandList.getRefreshableView().setSelector(android.R.color.transparent);
        //mExpandList.getRefreshableView().setOnGroupClickListener(this);
        //mExpandList.setOnRefreshListener(this);

        mExpandList.setMode(PullToRefreshBase.Mode.BOTH);
        mExpandList.getLoadingLayoutProxy(false, true).setPullLabel("pull_to_load");
        mExpandList.getLoadingLayoutProxy(false, true).setRefreshingLabel("loading");
        mExpandList.getLoadingLayoutProxy(false, true).setReleaseLabel("release_to_load");

        mDetalCommentData = new ArrayList<String>();
        for(int i = 0; i < 10; i++){
            mDetalCommentData.add("Detail comment : " + i);
        }

        mDetalCommentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mDetalCommentData);

        for(int i = 0; i < 10; i++){
            mDetalCommentData.add("Detail comment : " + i + "a");
        }
        mDetalCommentAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                                mDetalCommentData);

        mExpandList.setAdapter(mDetalCommentAdapter);

        mExpandList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                Log.d(TAG,"onRefresh ++++++ ");
                mExpandList.setAdapter(mDetalCommentAdapter2);
                mExpandList.onRefreshComplete();
            }
        });






    }*/


}
