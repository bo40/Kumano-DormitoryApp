package com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues;

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

import com.kumano_ryo.shijubo.kumano_dormitoryapp.MainActivity;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.R;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues.IssueItem;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues.IssuesAdapter;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.data.IssueData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private IssuesAdapter adapter;
    private OnSearchItemClickedListener mListener;
    private ProgressBar mProgressbar;
    private SearchView mSearchView;
    private static long autoScrollPosition;
    private static boolean isLoading;
    private static boolean isNewSearch;
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

        // 検索した議案を格納する変数の初期化など
        isLoading = false;
        if(isNewSearch || issueData.searchData == null)
        {
            issueData.searchData = new ArrayList<>();
        }
        autoScrollPosition = issueData.searchData.size();
        adapter = new IssuesAdapter(this.getContext(), issueData.searchData, false);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(new IssuesAdapter.onItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                mSearchView.setVisibility(View.GONE);
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
    public void onStart()
    {
        super.onStart();
        assert mSearchView != null;
        mSearchView.setVisibility(View.VISIBLE);
        if(isNewSearch)
        {
            mSearchView.setIconified(false);
        }
        else
        {
            mSearchView.clearFocus();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // load fragment as blank search
        isNewSearch = true;
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
        mSearchView.clearFocus();
        mSearchView.setVisibility(View.GONE);
        mSearchView = null;
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
        // プログレスバーの取得
        mProgressbar = (ProgressBar) view.findViewById(R.id.issuesProgressBar);
        mProgressbar.setVisibility(View.GONE);

        // toolbarに検索欄を表示
        Toolbar  toolbar = (Toolbar)getActivity().findViewById(R.id.toolbar);
        mSearchView = (SearchView) toolbar.findViewById(R.id.search);
        if(mSearchView == null)
        {
            toolbar.inflateMenu(R.menu.search);
            mSearchView = (SearchView) toolbar.getMenu().findItem(R.id.search).getActionView();
        }
        mSearchView.setVisibility(View.VISIBLE);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String encodedQuery = "";
                query = query.replace("　", " ").trim();
                try {
                    encodedQuery = URLEncoder.encode(query, "utf-8");
                }catch (UnsupportedEncodingException ue)
                {
                    System.out.println(ue);
                    return false;
                }
                // 検索文字の保存
                searchQuery = encodedQuery;
                autoScrollPosition = 0;
                // set already searched
                isNewSearch = false;
                searchIssueData(encodedQuery, 0, 50);
                mSearchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        // フラグメントのアプリバーのタイトルを設定
        assert ((MainActivity) getActivity()).getSupportActionBar() != null;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("議案の検索");
    }

    private void searchIssueData(final String query, final int start, final int num)
    {
        // 不正な引数に対しては処理を行わない
        if(start < 0 || num <= 0)
        {
            return;
        }
        // プログレスバーを表示する
        mProgressbar.setVisibility(View.VISIBLE);
        if(start == 0)
        {
            // startが0の場合は最初に検索をした場合
            if(issueData.searchData.size() > 0)
            {
                int size = issueData.searchData.size();
                for(int i = size -1 ; i >= 0 ; i--)
                {
                    issueData.searchData.remove(i);
                    adapter.notifyItemRemoved(i);
                }
            }
        }
        else
        {
            // startが0ではない場合はスクロールエンドで次の要素の読み込みをする場合
            if(start + num <= issueData.searchData.size())
            {
                // already existing data
                mProgressbar.setVisibility(View.GONE);
                return;
            }
        }
        // 検索結果のページ数
        final int page = start / 50 + 1;
        // UI操作のためのハンドラー
        final android.os.Handler handler = new android.os.Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isLoading = true;
                    URL url = new URL("http://docs.kumano-ryo.com/search_issue/?page=" + Integer.toString(page) + "&keywords=" + query);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    String str = InputStreamToString(con.getInputStream());
                    ArrayList<IssueItem> issueItems = new ArrayList<>();

                    // 議案の情報を読み込み
                    ReadIssueData(str, page, start, num, issueItems);
                    // 共有のデータに議案情報を格納
                    for (int i = 0; i < issueItems.size(); i++) {
                        issueData.searchData.add(issueItems.get(i));
                        adapter.notifyItemInserted(issueData.searchData.size());
                    }
                    isLoading = false;
                    autoScrollPosition += num;
                } catch(Exception ex) {
                    System.out.println(ex);
                } finally {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressbar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();


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

    private void ReadIssueData(String str, int page, int start, int num, ArrayList<IssueItem> issueItems)
    {
        int position = start - 50 * (page - 1);
        int sp = 0;
        for (int i = 0; i < position; i++) {
            sp = str.indexOf("</dt>", sp) + 1;
        }
        while (true) {
            // start from 0
            // positionの値をスクレイイピング
            // urlの値を使う
            int p1 = str.indexOf("<dt>", sp);
            if (p1 == -1) {
                break;
            }
            int p2 = str.indexOf("</dt>", sp);
            sp = p2 + 1;
            String part = str.substring(p1 + 4, p2 - 1);
            p1 = part.indexOf("href=\"");
            p2 = part.indexOf("\">");
            String path = part.substring(p1 + 6, p2).replace("&amp;", "&").replace("&quot;", "\"")
                    .replace("&lt;", "<").replace("&gt;", ">").trim(); // get path to issue page
            p1 = part.indexOf(">");
            p2 = part.lastIndexOf("<");
            String title = part.substring(p1 + 1, p2).replace("&amp;", "&").replace("&quot;", "\"")
                    .replace("&lt;", "<").replace("&gt;", ">").trim(); // get title

            p1 = str.indexOf("<small>", sp);
            p2 = str.indexOf("<br>", sp);
            String detail = str.substring(p1 + 7, p2).replaceAll("<.+?>", "").replace("&amp;", "&").replace("&quot;", "\"")
                    .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ")
                    .replace("&rarr;", "→").replace("&uarr;", "↑").trim(); // get detail
            detail = detail.replace("\n", "");
            if (detail.length() > 130) {
                detail = detail.substring(0, 130) + "...";
            }

            p1 = str.indexOf("<b>", sp);
            p2 = str.indexOf("</b>", sp);
            String info = str.substring(p1 + 3, p2).replace("&amp;", "&").replace("&quot;", "\"")
                    .replace("&lt;", "<").replace("&gt;", ">").trim(); // get info
            if (position + 50 * (page - 1) + 1 > issueData.searchData.size()) {
                issueItems.add(new IssueItem(0, title, detail, info, path));
            }
            position++;
            if (position + 50 * (page + 1) == start + num) {
                // end of addition
                break;
            }
            if (position == 50) {
                position = 0;
                break;
            }

        }
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
