package com.kumano_ryo.shijubo.kumano_dormitoryapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.kumano_ryo.shijubo.kumano_dormitoryapp.data.IssueData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BlockCIssuesFragment extends Fragment {

    private int position;
    private IssuesAdapter adapter;
    private OnBlockCIssueItemClickedListener mListener;
    private ProgressBar mProgressBar;
    private Drawable progressBarBackground;
    private IssueData issueData;

    public BlockCIssuesFragment() {
        // Required empty public constructor
    }

    public static BlockCIssuesFragment newInstance(int position) {
        BlockCIssuesFragment fragment = new BlockCIssuesFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        issueData = (IssueData)getActivity().getApplication();
        if (getArguments() != null) {
            position = getArguments().getInt("position");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_block_c_issues, container, false);

        // プログレスバーを表示
        mProgressBar = (ProgressBar) view.findViewById(R.id.blockCIssuesProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        progressBarBackground = view.findViewById(R.id.blockCIssuesProgressBarBackground).getBackground();
        progressBarBackground.setAlpha(120);

        assert ((MainActivity) getActivity()).getSupportActionBar() != null;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(issueData.blockCTitle.get(position));

        // RecyclerViewでブロック会議に含まれる議案一覧を表示する
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_block_c_issues);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llManager = new LinearLayoutManager(getActivity());
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llManager);

        ArrayList<IssueItem> issueItems = new ArrayList<>();
        if(issueData.bData == null)
        {
            issueData.bData = issueItems;
        }
        adapter = new IssuesAdapter(this.getContext(), issueData.bData, true);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(new IssuesAdapter.onItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(mListener != null)
                {
                    mListener.onBlockCIssueItemClicked(position);
                }
            }
        });
        if(issueData.bData == null || issueData.bData.size() == 0)
        {
            addIssueData();
        }
        else
        {
            mProgressBar.setVisibility(View.GONE);
            progressBarBackground.setAlpha(0);
        }
        return view;
    }

    public void addIssueData()
    {
        final android.os.Handler handler = new android.os.Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                int page = 1;
                while(true)
                {
                    if(issueData.blockCData.size() <= position)
                    {
                        // ブロック会議のデータが存在しない
                        return;
                    }
                    URL url = new URL("http://docs.kumano-ryo.com" + issueData.blockCData.get(position) + "?page=" + Integer.toString(page));
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    String str = InputStreamToString(con.getInputStream());
                    ArrayList<IssueItem> issueItems = new ArrayList<>();
                    int sp = 0;
                    while(true)
                    {
                        int p1 = str.indexOf("<h4>", sp);
                        if(p1 == -1)
                        {
                            page++;
                            break;
                        }
                        int p2 = str.indexOf("</h4>", sp);
                        sp = p2 + 1;
                        String title = str.substring(p1+4, p2).replace("&amp;", "&").replace("&quot;", "\"")
                                .replace("&lt;", "<").replace("&gt;", ">")
                                .replace("&rarr;", "→").replace("&uarr;", "↑").trim(); // get title
                        p1 = str.indexOf("<dd>", sp);
                        p2 = str.indexOf("</dd>", sp);
                        String info = "文責者 : " + str.substring(p1+4, p2).replace("&amp;", "&").replace("&quot;", "\"")
                                .replace("&lt;", "<").replace("&gt;", ">").trim(); // get editor for info
                        p1 = str.indexOf("<pre>", sp);
                        p2 = str.indexOf("</pre>", sp);
                        String detail = str.substring(p1+5, p2).replace("&amp;", "&").replace("&quot;", "\"")
                                .replace("&lt;", "<").replace("&gt;", ">")
                                .replace("&rarr;", "→").replace("&uarr;", "↑").trim(); // get detail
                        String overView = detail;
                        // overViewの行数を６行以内で１３０文字以内にする。
                        int pLine = 0;
                        for(int i = 0 ; i < 6 ; i++)
                        {
                            pLine = overView.indexOf("\n", pLine) + 1;
                            if(pLine == 0) { break; }
                        }
                        if(pLine != 0)
                        {
                            overView = overView.substring(0, pLine);
                            if(overView.length() > 90)
                            {
                                overView = overView.substring(0, 90) + "...";
                            }
                        }
                        else if(overView.length() > 130)
                        {
                            overView = overView.substring(0, 130) + "...";
                        }
                        // 表のデータを取得
                        // 表のタイトルの配列
                        ArrayList<String> tableTitles = null;
                        // 表の配列
                        ArrayList<ArrayList<ArrayList<String>>> tables = null;
                        p1 = str.indexOf("<dt>表</dt>", sp);
                        int ep = str.indexOf("<h4>", sp);
                        if(ep == -1)
                        {
                            ep = Integer.MAX_VALUE;
                        }
                        if(p1 != -1 && p1 < ep) {
                            // 表の配列を初期化
                            tables = new ArrayList<>();
                            // 表のタイトルの配列の初期化
                            tableTitles = new ArrayList<>();
                            // 表のデータ
                            ArrayList<ArrayList<String>> table;
                            // 表のタイトルを取得
                            int endpoint = str.indexOf("</dd>", p1);
                            p1 = str.indexOf("<caption>", p1);
                            int startpoint = p1 + 9;
                            while(p1 != -1 && p1 < endpoint)
                            {
                                table = new ArrayList<>();
                                p2 = str.indexOf("</caption>", p1);
                                tableTitles.add(str.substring(p1 + 9, p2).replace("&amp;", "&").replace("&quot;", "\"")
                                        .replace("&lt;", "<").replace("&gt;", ">").trim()); // get table title
                                // 表の行を取得するループ
                                p1 = str.indexOf("<tr>", p2);
                                while (p1 != -1 && p1 < ep) {
                                    p2 = str.indexOf("</tr>", p1);
                                    String part = str.substring(p1 + 4, p2).trim();
                                    int p3 = part.indexOf("<div");
                                    int p4 = 0;
                                    ArrayList<String> row = new ArrayList<>();
                                    while (p3 != -1) {
                                        p3 = part.indexOf(">", p3);
                                        p4 = part.indexOf("</div>", p3);
                                        row.add(part.substring(p3 + 1, p4).replace("&amp;", "&").replace("&quot;", "\"")
                                                .replace("&lt;", "<").replace("&gt;", ">").trim());
                                        p3 = part.indexOf("<div", p4);
                                    }
                                    table.add(row);
                                    p1 = str.indexOf("<tr>", p2);
                                }
                                tables.add(table);
                                p1 = str.indexOf("<caption>", startpoint);
                                startpoint = p1 + 9;
                            }
                        }
                        issueItems.add(new IssueItem(0, title, overView, detail, tableTitles, tables, info, true));
                    }
                    for(int i = 0 ; i < issueItems.size() ; i++)
                    {
                        issueData.bData.add(issueItems.get(i));
                        adapter.notifyItemInserted(issueData.bData.size());
                    }
                    int p3 = str.indexOf("?page=" + Integer.toString(page) + "\">" + Integer.toString(page) + "<");
                    if(p3 == -1)
                    {
                        break;
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        progressBarBackground.setAlpha(0);
                    }
                });
            } catch(Exception ex) {
                System.out.println(ex);
            }
            }
        }).start();
    }

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBlockCIssueItemClickedListener) {
            mListener = (OnBlockCIssueItemClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBlockCIssueItemClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnBlockCIssueItemClickedListener {
        void onBlockCIssueItemClicked(int position);
    }
}
