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
    private OnBlockCIssueListener mListener;
    private ProgressBar mProgressBar;
    private Drawable progressBarBackground;
    private IssueData issueData;

    public BlockCIssuesFragment() {
        // Required empty public constructor
    }

    // positionにブロック会議日程のインデックスが渡ってくる
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
        if(issueData.blockCData.size() > position)
        {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(issueData.blockCTitle.get(position));
        }
        else
        {
            mListener.onBlockCIssueDataMissing();
            return view;
        }

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
                boolean fFirst = true;
                while(true)
                {
                    if(issueData.blockCData.size() <= position)
                    {
                        // ブロック会議のデータが存在しない
                        mListener.onBlockCIssueDataMissing();
                        return;
                    }
                    ArrayList<IssueItem> issueItems = new ArrayList<>();

                    if (fFirst)
                    {
                        fFirst = false;
                        // ブロック会議のゼロ番の取得
                        if(issueData.blockCTitle.size() > position +1)
                        {
                            String preBlockCTitle = issueData.blockCTitle.get(position +1);
                            String title = "【0】前回のブロック会議から";
                            String info = "資料システム";

                            URL url0 = new URL("http://docs.kumano-ryo.com/browse_issue/");
                            HttpURLConnection con = (HttpURLConnection)url0.openConnection();
                            String str0 = InputStreamToString(con.getInputStream());
                            int p1 = 0;
                            String detail = "";
                            p1 = str0.indexOf("<b>", p1);
                            while(p1 != -1)
                            {
                                int p2 = str0.indexOf(":", p1);
                                String blockC = str0.substring(p1+3, p2) + "のブロック会議";
                                if (blockC.equals(preBlockCTitle))
                                {
                                    // 前回のブロック会議の議案の場合は議事録を取得する。
                                    int p3 = str0.lastIndexOf("<a href=\"", p1);
                                    int p4 = str0.indexOf("\"", p3+9);
                                    String path = str0.substring(p3+9, p4);
                                    p3 = str0.indexOf(">", p3+1);
                                    p4 = str0.indexOf("<", p3);
                                    String issueTitle = str0.substring(p3+1, p4).replaceAll("<.+?>", "").replace("&amp;", "&").replace("&quot;", "\"")
                                            .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ").replace("&rarr;", "→").replace("&uarr;", "↑").trim(); // get detail
                                    URL urln = new URL("http://docs.kumano-ryo.com" + path);
                                    HttpURLConnection conn = (HttpURLConnection)urln.openConnection();
                                    String strn = InputStreamToString(conn.getInputStream());
                                    String comments = "";
                                    int p5 = strn.indexOf("<h3 class='page-header'>議事録");
                                    if(p5 !=  -1)
                                    {
                                        while(true)
                                        {
                                            int sp = p5;
                                            p5 = strn.indexOf("<dt>", sp);
                                            if(p5 == -1)
                                            {
                                                break;
                                            }
                                            int p6 = strn.indexOf("</dt>", p5);
                                            comments = comments + "---< " + strn.substring(p5+4,p6).trim() + " >---\n";
                                            p5 = strn.indexOf("<pre>", p6);
                                            p6 = strn.indexOf("</pre>", p5);
                                            comments = comments + strn.substring(p5+5, p6).replace("&amp;", "&").replace("&quot;", "\"")
                                                    .replace("&lt;", "<").replace("&gt;", ">").trim() + "\n\n";
                                        }
                                    }
                                    if (!comments.equals(""))
                                    {
                                        detail = detail +"================================\n《" + issueTitle + "》" + "への意見\n" + comments;
                                    }
                                }
                                p1 = str0.indexOf("<b>", p1+1);
                            }

                            String overView = detail;
                            // overViewの行数を６行以内か１３０文字以内にする。
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
                            ArrayList<String> tableTitles = new ArrayList<>();
                            // 表の配列
                            ArrayList<ArrayList<ArrayList<String>>> tables = new ArrayList<>();
                            issueItems.add(new IssueItem(0, title, overView, detail, tableTitles, tables, info, true));
                        }
                    }

                    URL url = new URL("http://docs.kumano-ryo.com" + issueData.blockCData.get(position) + "?page=" + Integer.toString(page));
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    String str = InputStreamToString(con.getInputStream());
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
                        // overViewの行数を６行以内か１３０文字以内にする。
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
                        ArrayList<String> tableTitles = new ArrayList<>();
                        // 表の配列
                        ArrayList<ArrayList<ArrayList<String>>> tables = new ArrayList<>();
                        // 表のデータを取得
                        p1 = str.indexOf("<table", sp);
                        int ep = str.indexOf("<h4>", sp);
                        if(ep == -1)
                        {
                            ep = Integer.MAX_VALUE;
                        }
                        if(p1 != -1 && p1 < ep)
                        {
                            ArrayList<ArrayList<String>> table;
                            // 表のタイトルを取得
                            int endpoint = str.indexOf("</table>", p1);
                            int startpoint = p1 + 9;
                            while(p1 != -1 && p1 < endpoint && p1 < ep)
                            {
                                table = new ArrayList<>();
                                p2 = p1;
                                p1 = str.indexOf("<caption>", p1);
                                if(p1 != -1)
                                {
                                    p2 = str.indexOf("</caption>", p1);
                                    tableTitles.add(str.substring(p1 + 9, p2).replace("&amp;", "&").replace("&quot;", "\"")
                                            .replace("&lt;", "<").replace("&gt;", ">")
                                            .replace("&nbsp;", " ").replace("&yen;", "¥").trim()); // get table title
                                }
                                else
                                {
                                    tableTitles.add("");
                                }
                                // 表の行を取得するループ
                                p1 = str.indexOf("<tr>", p2);
                                while (p1 != -1 && p1 < endpoint && p1 < ep) {
                                    p2 = str.indexOf("</tr>", p1);
                                    String part = str.substring(p1 + 4, p2).trim();
                                    boolean isTh = true;
                                    int p3 = part.indexOf("<th");
                                    ArrayList<String> row = new ArrayList<>();
                                    if(p3 == -1)
                                    {
                                        isTh = false;
                                        p3 = part.indexOf("<td");
                                    }
                                    int p4 = 0;
                                    while (p3 != -1) {
                                        p3 = part.indexOf(">", p3);
                                        p4 = isTh ? part.indexOf("</th>", p3) : part.indexOf("</td>", p3);
                                        row.add(part.substring(p3 + 1, p4).replace("\n", "").replace(" ", "").replace("<br/>", "\n")
                                                .replaceAll("<.+?>", "").replace("&amp;", "&").replace("&quot;", "\"")
                                                .replace("&lt;", "<").replace("&gt;", ">")
                                                .replace("&nbsp;", "").replace("&yen;", "¥").replace("&times;", "×").trim());
                                        isTh = true;
                                        p3 = part.indexOf("<th", p4);
                                        if(p3 == -1)
                                        {
                                            isTh = false;
                                            p3 = part.indexOf("<td", p4);
                                        }
                                    }
                                    table.add(row);
                                    p1 = str.indexOf("<tr>", p2);
                                }
                                tables.add(table);
                                p1 = str.indexOf("<table", startpoint);
                                startpoint = p1 + 9;
                                endpoint = str.indexOf("</table>", p1);
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
        if (context instanceof OnBlockCIssueListener && context instanceof IssueDetailFragment.OnIssueDataMissingListener) {
            mListener = (OnBlockCIssueListener) context;
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
    public interface OnBlockCIssueListener {
        void onBlockCIssueItemClicked(int position);
        void onBlockCIssueDataMissing();
    }
}
