package com.kumano_ryo.shijubo.kumano_dormitoryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.data.IssueData;
import com.facebook.crypto.*;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,
        IssuesFragment.OnIssueItemClickedListener,
        BlockCFragment.OnBlockCItemClickedListener,
        BlockCIssuesFragment.OnBlockCIssueItemClickedListener,
        IssueDetailFragment.OnIssueDataMissingListener,
        SearchFragment.OnSearchItemClickedListener
{

    private IssueData issueData;
    private boolean isCheckStop;
    private String[] mNavMenu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private int displaySize = 400;
    final String[] fragments = {
            "com.kumano_ryo.shijubo.kumano_dormitoryapp.NewsFragment",
            "com.kumano_ryo.shijubo.kumano_dormitoryapp.IssuesFragment",
            "com.kumano_ryo.shijubo.kumano_dormitoryapp.BlockCFragment",
            "com.kumano_ryo.shijubo.kumano_dormitoryapp.SearchFragment",
            "com.kumano_ryo.shijubo.kumano_dormitoryapp.MenuFragment",
            "com.kumano_ryo.shijubo.kumano_dormitoryapp.IssueDetailFragment",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        issueData = (IssueData)getApplication();
        super.onCreate(savedInstanceState);

        // ナビゲーションドロワーの設定
        if(Build.VERSION.SDK_INT >= 21)
        {
            // only available for  api 21+
            setContentView(R.layout.activity_main);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
        }
        else
        {
            // for below api 20
            setContentView(R.layout.activity_main_older);
            mNavMenu = getResources().getStringArray(R.array.nav_menu_array);
            mDrawerList = (ListView) findViewById(R.id.nav_view_older);
            // Set the adapter for the list view
            mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mNavMenu));
            // Set the list's click listener
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 前のアクティビティに戻らないようにする
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();


        SharedPreferences pref = getSharedPreferences("a1", MODE_PRIVATE);
        final String id = pref.getString("id", "id");
        final String pass = getToken();
        if(pass == null)
        {
            ((TextView)findViewById(R.id.caution)).setText("右上メニューの設定からIDとパスワードを設定して下さい。");
        }
        java.net.Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(id, pass.toCharArray());
            }
        });

        // 初期画面を設定
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[1])).commit();
        if(Build.VERSION.SDK_INT >= 21)
        {
            ((NavigationView)findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_issues);
        }
        else
        {
            mDrawerList.setItemChecked(1, true);
        }

    }

    @Override
    public void onStart()
    {
        super.onStart();
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point(400, 0);
        display.getSize(point);
        displaySize = point.x;
        isCheckStop = true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int backStackCnt = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackCnt != 0) {
                getSupportFragmentManager().popBackStack();
            }
            else if(isCheckStop)
            {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.content_main);
                Snackbar.make(relativeLayout, "もう一度戻るボタンを押すとアプリを終了します", Snackbar.LENGTH_SHORT).show();
                isCheckStop = false;
            }
            else
            {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings_password) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // api 19以下の場合のナビゲーション
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            isCheckStop = true;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            SearchView searchView = (SearchView) findViewById(R.id.search);
            if(searchView != null)
            {
                searchView.clearFocus();
                searchView.setVisibility(View.GONE);
            }
            switch(position)
            {
                // 寮内周知
                case 0:
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[0]))
                            //.addToBackStack(null)
                            .commit();
                    break;

                // 新着議案
                case 1:
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[1]))
                            //.addToBackStack(null)
                            .commit();
                    break;

                // ブロック会議一覧
                case 2:
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[2]))
                            //.addToBackStack(null)
                            .commit();
                    break;

                // 議案の検索
                case 3:
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[3]))
                            //.addToBackStack(null)
                            .commit();
                    break;

                // 今週の寮食
                case 4:
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[4]))
                            //.addToBackStack(null)
                            .commit();
                    break;
            }
            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    // api 21以上のナビゲーション
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // RelativeLayout layout = (RelativeLayout)findViewById(R.id.content_main);
        // layout.removeAllViews();
        isCheckStop = true;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        SearchView searchView = (SearchView) findViewById(R.id.search);
        if(searchView != null)
        {
            searchView.clearFocus();
            searchView.setVisibility(View.GONE);
        }
        switch(id)
        {
            // 寮内周知
            case R.id.nav_new:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[0]))
                        //.addToBackStack(null)
                        .commit();
                break;

            // 新着議案
            case R.id.nav_issues:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[1]))
                        //.addToBackStack(null)
                        .commit();
                break;

            // ブロック会議一覧
            case R.id.nav_block_c:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[2]))
                        //.addToBackStack(null)
                        .commit();
                break;

            // 議案の検索
            case R.id.nav_search:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[3]))
                        //.addToBackStack(null)
                        .commit();
                break;

            // 今週の寮食
            case R.id.nav_menu:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_main, Fragment.instantiate(MainActivity.this, fragments[4]))
                        //.addToBackStack(null)
                        .commit();
                break;
        }
        /*
            RelativeLayout layout = (RelativeLayout)findViewById(R.id.content_main);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.fragment_issues_view,layout);

        */

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private String getToken()
    {
        SharedPreferences preferences = getSharedPreferences("a1", MODE_PRIVATE);
        String ak = preferences.getString("ak", null);
        String et = preferences.getString("ek", null);
        byte[] rawEt = null;
        String rawDt = null;

        if(et != null && ak != null) {
            rawEt = Base64.decode(et, Base64.DEFAULT);

            Crypto crypto = new Crypto(
                    new SharedPrefsBackedKeyChain(this),
                    new SystemNativeCryptoLibrary());
            if (!crypto.isAvailable()) {
                return null;
            }
            try {
                byte[] decryptedToken = crypto.decrypt(rawEt, new Entity(ak));
                rawDt = new String(decryptedToken);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (CryptoInitializationException e) {
                e.printStackTrace();
            } catch (KeyChainException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rawDt;
    }

    public int getDisplaySize() { return displaySize; }
    @Override
    public void onIssueItemClicked(int position){
        isCheckStop = true;
        IssueDetailFragment issueDetailFragment = IssueDetailFragment.newInstance(position, 1);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_main, issueDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBlockCItemClicked(int position)
    {
        isCheckStop = true;
        BlockCIssuesFragment blockCIssuesFragment = BlockCIssuesFragment.newInstance(position);
        issueData.bData = new ArrayList<>();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_main, blockCIssuesFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBlockCIssueItemClicked(int position)
    {
        isCheckStop = true;
        IssueDetailFragment issueDetailFragment = IssueDetailFragment.newInstance(position, 0);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_main, issueDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    // 検索画面の議案をクリックした時
    @Override
    public void onSearchItemClicked(int position)
    {
        isCheckStop = true;
        IssueDetailFragment issueDetailFragment = IssueDetailFragment.newInstance(position, 2);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_main,issueDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onIssueDataMissing(boolean isIssuesFragment)
    {
        isCheckStop = true;
        issueData.clearData();
        if(isIssuesFragment)
        {
            IssuesFragment issuesFragment = IssuesFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_main, issuesFragment).commit();
        }
        else
        {
            BlockCFragment blockCFragment = BlockCFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_main, blockCFragment).commit();
        }
    }
}
