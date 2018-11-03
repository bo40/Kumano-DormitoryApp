package com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kumano_ryo.shijubo.kumano_dormitoryapp.MainActivity;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.R;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues.IssueItem;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues.IssuesAdapter;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.data.IssueData;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class IssuesFragment extends Fragment {

    private IssuesAdapter adapter;
    private OnIssueItemClickedListener mListener;
    private ProgressBar mProgressbar;
    private static long autoScrollPosition;
    private static boolean isLoading;
    private IssueData issueData;

    public IssuesFragment() {
        // Required empty public constructor
    }

    public static IssuesFragment newInstance() {
        IssuesFragment fragment = new IssuesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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
        View view = inflater.inflate(R.layout.fragment_issues, container, false);

        // UIの設定を行う
        setUI(view);

        // RecyclerViewにIssuesAdapterをセット
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_issues);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llManager = new LinearLayoutManager(getActivity());
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llManager);

        isLoading = false;
        if(issueData.data == null)
        {
            issueData.data = new ArrayList<>();
            autoScrollPosition = 0;
        }
        else
        {
            autoScrollPosition = issueData.data.size();
        }
        adapter = new IssuesAdapter(this.getContext(), issueData.data, false);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(new IssuesAdapter.onItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                mListener.onIssueItemClicked(position);
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
    public void onStart() {
        super.onStart();
        if (autoScrollPosition == 0) {
            int start = 0, num = 50;
            // 新着議案一覧を読み込んで保存
            addIssueData(start, num);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnIssueItemClickedListener) {
            mListener = (OnIssueItemClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnIssueItemClickedListener");
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
        // プログレスバーの表示
        mProgressbar = (ProgressBar) view.findViewById(R.id.issuesProgressBar);
        mProgressbar.setVisibility(View.GONE);

        // フラグメントのアプリバーのタイトルを設定
        assert ((MainActivity) getActivity()).getSupportActionBar() != null;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("議案一覧");
    }

    /**
     * startからnum個の議案を取得して追加する
     * @param start 読み込みを行う議案の先頭のインデックス　0以上の整数値
     * @param num 読み込む議案の個数
     */
    private void addIssueData(final int start, final int num)
    {
        // 不正な引数に対しては処理を行わない
        if(start < 0 || num <= 0)
        {
            return;
        }
        // プログレスバーを表示する
        mProgressbar.setVisibility(View.VISIBLE);
        if(start + num <= issueData.data.size())
        {
            // already existing data
            mProgressbar.setVisibility(View.GONE);
            return;
        }
        final int page = start / 50 + 1;

        final android.os.Handler handler = new android.os.Handler();
        final Activity activity = getActivity();
        new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                isLoading = true;
                int index = 0;
                ArrayList<IssueItem> issueItems = new ArrayList<>();
                while (index < MainActivity.domains.length) {
                    URL url = new URL("http://docs." + MainActivity.domains[index] + "/browse_issue/?page=" + Integer.toString(page));
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    String str = InputStreamToString(con.getInputStream());
                    issueItems = new ArrayList<>();
                    // 議案の情報を読み込み
                    ReadIssueData(str, page, start, num, issueItems);
                    if (issueItems.size() > 0) {
                        break;
                    }
                    index++;
                }
                // 共有のデータに議案情報を格納
                for (int i = 0; i < issueItems.size(); i++) {
                    issueData.data.add(issueItems.get(i));
                    adapter.notifyItemInserted(issueData.data.size());
                }
                isLoading = false;
                autoScrollPosition += num;
            }catch(ProtocolException pe) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity,"IDとパスワードが違います。設定し直してください", Toast.LENGTH_LONG).show();
                    }
                });
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

    /**
     * 文字列から指定された位置の議案情報を読み込む
     * @param str 入力文字列 HTMLソース
     * @param page ページ数
     * @param start 読み込む議案の先頭のインデックス
     * @param num 読み込む議案の数
     * @param issueItems 読み込んだ議案の情報を格納する配列
     */
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
            String detail = str.substring(p1 + 7, p2).replace("&amp;", "&").replace("&quot;", "\"")
                    .replace("&lt;", "<").replace("&gt;", ">").trim(); // get detailint pLine = 0;
            // 概要表示なので改行をなくす
            detail = detail.replace("\n", " ");
            if (detail.length() > 130) {
                detail = detail.substring(0, 130) + "...";
            }

            p1 = str.indexOf("<b>", sp);
            p2 = str.indexOf("</b>", sp);
            String info = str.substring(p1 + 3, p2).replace("&amp;", "&").replace("&quot;", "\"")
                    .replace("&lt;", "<").replace("&gt;", ">").trim(); // get info
            if (position + 50 * (page - 1) + 1 > issueData.data.size()) {
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
        if (issueData.data.size() >= 1000) {
            return;
        }
        addIssueData(issueData.data.size(), 50);
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
    public interface OnIssueItemClickedListener {
        void onIssueItemClicked(int position);
    }

}
