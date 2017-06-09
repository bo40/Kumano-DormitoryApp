package com.kumano_ryo.shijubo.kumano_dormitoryapp;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.kumano_ryo.shijubo.kumano_dormitoryapp.data.IssueData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private IssuesAdapter adapter;
    private OnSearchItemClickedListener mListener;
    private ProgressBar mProgressbar;
    private SearchView mSearchView;
    private static long autoScrollPosition;
    private static boolean isLoading;
    private IssueData issueData;
    private String searchQuery = "";

    public SearchFragment() {
        // Required empty public constructor
    }


    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        issueData = (IssueData) getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // UIの設定を行う
        setUI(view);

        // RecyclerViewにIssuesAdapterをセット
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_issues);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llManager = new LinearLayoutManager(getActivity());
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llManager);

        isLoading = false;
        issueData.searchData = new ArrayList<>();
        autoScrollPosition = 0;
        adapter = new IssuesAdapter(this.getContext(), issueData.searchData, false);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(new IssuesAdapter.onItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                mListener.onSearchItemClicked(position);
            }
        });

        // 最も下までスクロールした場合に次の議案を読み込む動作を設定
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = recyclerView.getChildCount();
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisibleItem = manager.findFirstVisibleItemPosition();
                int lastInScreen = firstVisibleItem + visibleItemCount;

                if (isAutoScroll(lastInScreen)) {
                    isLoading = true;
                    load();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSearchItemClickedListener) {
            mListener = (OnSearchItemClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSearchItemClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * onCreateViewにおいてUIの設定を行う
     * @param view このフラグメントのビュー
     */
    private void setUI(View view)
    {
        // ドロワーナビゲーションメニューの選択を設定
        NavigationView navigation = ((NavigationView)view.findViewById(R.id.nav_view));
        if(navigation != null)
        {
            navigation.setCheckedItem(R.id.nav_issues);
        }

        // toolbarに検索欄を表示
        Toolbar  toolbar = (Toolbar)getActivity().findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.search);
        mSearchView = (SearchView) toolbar.getMenu().findItem(R.id.search).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchIssueData(query, 0, 50);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });
        mSearchView.setIconified(false);
        /*
        // プログレスバーの表示
        mProgressbar = (ProgressBar) view.findViewById(R.id.issuesProgressBar);
        mProgressbar.setVisibility(View.GONE);
        */

        // フラグメントのアプリバーのタイトルを設定
        assert ((MainActivity) getActivity()).getSupportActionBar() != null;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("議案の検索");
    }

    private void searchIssueData(final String query, final int start, final int num)
    {

    }

    // ストリームを読み込んで文字列を返す。HTML読み込みで使う
    static String InputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        return sb.toString();
    }

    private boolean isAutoScroll(int lastInScreen) {
        // If you have never loaded,  Auto Scroll do not do
        if (autoScrollPosition == 0) {
            return false;
        }

        // loading中はAutoScrollしない
        if (isLoading) {
            return false;
        }

        //　画面下でない場合は、AutoScrollしない
        if (autoScrollPosition != lastInScreen) {
            return false;
        }

        return true;
    }

    private void load() {
        if (issueData.searchData.size() >= 1000) {
            return;
        }
        searchIssueData(searchQuery, issueData.searchData.size(), 50);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSearchItemClickedListener {
        void onSearchItemClicked(int position);
    }
}
