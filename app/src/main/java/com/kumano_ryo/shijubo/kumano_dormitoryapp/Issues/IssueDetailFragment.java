package com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kumano_ryo.shijubo.kumano_dormitoryapp.MainActivity;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.R;
import com.kumano_ryo.shijubo.kumano_dormitoryapp.data.IssueData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class IssueDetailFragment extends Fragment implements View.OnClickListener {

    private int position = -1;
    private int viewType = 0;
    private View mView;
    private IssueData issueData;
    private OnIssueDataMissingListener mListener;
    private ProgressBar mProgressBar;

    public IssueDetailFragment() {
        // Required empty public constructor
    }


    public static IssueDetailFragment newInstance(int position, int viewType) {
        IssueDetailFragment fragment = new IssueDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("issue_position", position);
        arguments.putInt("viewType", viewType);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        issueData = (IssueData)getActivity().getApplication();
        if (getArguments() != null) {
            position = getArguments().getInt("issue_position");
            viewType = getArguments().getInt("viewType");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_issue_detail, container, false);
        mView = view;
        // プログレスバーの表示
        mProgressBar = (ProgressBar) view.findViewById(R.id.issueDetailProgressBar);

        assert ((MainActivity) getActivity()).getSupportActionBar() != null;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("議案の詳細");

        Button nextButton = (Button) view.findViewById(R.id.next);
        Button preButton = (Button) view.findViewById(R.id.previous);
        nextButton.setOnClickListener(this);
        preButton.setOnClickListener(this);

        addIssueData();

        return view;
    }

    public void addIssueData()
    {
        mProgressBar.setVisibility(View.VISIBLE);
        // 議事録を不可視化して、ボタンを無効化する。
        mView.findViewById(R.id.issue_detail_comments).setVisibility(View.GONE);
        mView.findViewById(R.id.previous).setEnabled(false);
        mView.findViewById(R.id.next).setEnabled(false);
        // 前に追加した表のデータを削除
        ((ViewGroup) mView.findViewById(R.id.issue_table_container)).removeAllViews();
        // 変更を行うビューを定数化
        final Handler handler = new Handler(); //Looper.getMainLooper());
        final TextView tx = (TextView) mView.findViewById(R.id.issue_detail_detail);
        final TextView commentTx = (TextView) mView.findViewById(R.id.issue_detail_comments);

        String tmp;
        if(viewType > 0)
        {
            if(issueData.data.size() <= position)
            {
                mListener.onIssueDataMissing(true);
                return;
            }
            if(viewType == 1)
            {
                tmp = "http://docs.kumano-ryo.com" + issueData.data.get(position).getUrl();
            }
            else
            {
                tmp = "http://docs.kumano-ryo.com" + issueData.searchData.get(position).getUrl();
            }
            final String path = tmp;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String detail = "";
                    ArrayList<String> tableTitles = null;
                    String comments = "";
                    // 表の配列を初期化
                    ArrayList<ArrayList<ArrayList<String>>> tables = new ArrayList<>();
                    try {
                        URL url = new URL(path);
                        HttpURLConnection con = (HttpURLConnection)url.openConnection();
                        String str = InputStreamToString(con.getInputStream());

                        int p1 = str.indexOf("<dt>本文</dt>");
                        if(p1 == -1)
                        {
                            return;
                        }
                        p1 = str.indexOf("<dd>",p1);
                        int p2 = str.indexOf("</dd>",p1);
                        // 議案詳細を取得
                        detail = str.substring(p1+4, p2).replaceAll("<.+?>", "").replace("&amp;", "&").replace("&quot;", "\"")
                                .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ").replace("&rarr;", "→").replace("&uarr;", "↑").trim(); // get detail
                        // 議案詳細に採決項目を追加する
                        p1 = str.indexOf("<dt>採決項目</dt>", p1);
                        if (p1 != -1)
                        {
                            p1 = str.indexOf("<dd>", p1);
                            p2 = str.indexOf("</dd>", p1);
                            detail += "\n\n【採決項目】\n" + str.substring(p1+4, p2).replaceAll("<.+?>", "").replace("&amp;", "&")
                                    .replace("&quot;", "\"").replace("&lt;", "<")
                                    .replace("&gt;", ">").replace("&nbsp;", " ")
                                    .replace("&rarr;", "→").replace("&uarr;", "↑").trim(); // get detail ;
                        }

                        // 表のデータを取得
                        p1 = str.indexOf("<table");
                        if(p1 != -1)
                        {
                            // 表のタイトルの配列の初期化
                            tableTitles = new ArrayList<>();
                            // 表のデータ
                            ArrayList<ArrayList<String>> table;
                            // 表のタイトルを取得
                            int endpoint = str.indexOf("</table>", p1);
                            int startpoint = p1 + 9;
                            while(p1 != -1 && p1 < endpoint)
                            {
                                table = new ArrayList<>();
                                p2 = p1;
                                p1 = str.indexOf("<caption>", p1);
                                if(p1 != -1 && p1 < endpoint)
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
                                while (p1 != -1 && p1 < endpoint) {
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
                        // 議事録を取得
                        comments = getComments(str);
                    }catch(IOException e)
                    {
                        System.out.println(e.toString());
                    }
                    catch (Exception ex)
                    {
                        System.out.println(ex.toString());
                    }
                    final String text = detail;
                    final String text2 = comments.trim();
                    final ArrayList<String> finalTableTitles = tableTitles;
                    final ArrayList<ArrayList<ArrayList<String>>> finalTables = tables;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tx.setText(text);
                            if(text2 != "")
                            {
                                commentTx.setVisibility(View.VISIBLE);
                                commentTx.setText(text2);
                            }
                            // 表のデータを表示する
                            if(finalTables.size() > 0)
                            {
                                ViewGroup vg = (ViewGroup) mView.findViewById(R.id.issue_table_container);
                                for(int n = 0 ; n < finalTables.size() ; n++)
                                {
                                    if(getActivity() == null) { return; }
                                    try {
                                        getActivity().getLayoutInflater().inflate(R.layout.issue_table, vg);
                                    } catch(InflateException e) {
                                        break;
                                    }
                                    ViewGroup vg_table_parent = (ViewGroup) vg.getChildAt(n);
                                    TextView tableTitle = ((TextView) vg_table_parent.getChildAt(0));
                                    tableTitle.setVisibility(View.VISIBLE);
                                    tableTitle.setText(finalTableTitles.get(n));
                                    ViewGroup vg_table = (ViewGroup) vg_table_parent.getChildAt(1);
                                    vg_table.setMinimumWidth(((MainActivity)getActivity()).getDisplaySize() - 100);
                                    // get max table column size
                                    int max = 0;
                                    for(int i = 0 ; i < finalTables.get(n).size() ; i++)
                                    {
                                        if(max < finalTables.get(n).get(i).size())
                                        {
                                            max = finalTables.get(n).get(i).size();
                                        }
                                    }
                                    // set Values to show
                                    for(int i = 0 ; i < finalTables.get(n).size() ; i++)
                                    {
                                        if(getActivity() == null) { return; }
                                        getActivity().getLayoutInflater().inflate(R.layout.table_row, vg_table);
                                        ViewGroup vg_row = (ViewGroup) vg_table.getChildAt(i);
                                        for(int j = 0 ; j < max ; j++)
                                        {
                                            if(getActivity() == null) { return; }
                                            getActivity().getLayoutInflater().inflate(R.layout.table_text, vg_row);
                                            String value = " ";
                                            if(j < finalTables.get(n).get(i).size())
                                            {
                                                value = finalTables.get(n).get(i).get(j);
                                            }
                                            ((TextView)vg_row.getChildAt(j)).setText(value);
                                        }
                                    }
                                }
                            }
                            // next previousボタンの有効・無効を設定
                            setEnableButtons(position, true);
                            int size = (viewType == 1) ? issueData.data.size() : issueData.searchData.size();
                            if(size <= position)
                            {
                                mListener.onIssueDataMissing(true);
                                return;
                            }
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
            int size = (viewType == 1) ? issueData.data.size() : issueData.searchData.size();
            if(size > position)
            {
                if(viewType == 1)
                {
                    ((TextView) mView.findViewById(R.id.issue_detail_title)).setText(issueData.data.get(position).getTitle());
                    ((TextView) mView.findViewById(R.id.issue_detail_detail)).setText(issueData.data.get(position).getDetail());
                    ((TextView) mView.findViewById(R.id.issue_detail_info)).setText(issueData.data.get(position).getInfo());
                }
                else
                {
                    ((TextView) mView.findViewById(R.id.issue_detail_title)).setText(issueData.searchData.get(position).getTitle());
                    ((TextView) mView.findViewById(R.id.issue_detail_detail)).setText(issueData.searchData.get(position).getDetail());
                    ((TextView) mView.findViewById(R.id.issue_detail_info)).setText(issueData.searchData.get(position).getInfo());
                }
            }
            else
            {
                mListener.onIssueDataMissing(true);
            }
        }
        else // fIssue == false の場合
        {
            // next previousボタンの有効・無効を設定
            setEnableButtons(position, false);
            // ブロック会議の議案詳細の場合はすでにデータが存在する
            if(issueData.bData.size() > position)
            {
                ((TextView) mView.findViewById(R.id.issue_detail_title)).setText(issueData.bData.get(position).getTitle());
                ((TextView) mView.findViewById(R.id.issue_detail_detail)).setText(issueData.bData.get(position).getDetail());
                ((TextView) mView.findViewById(R.id.issue_detail_info)).setText(issueData.bData.get(position).getInfo());
                // 表のデータがある場合は表を表示する
                if(issueData.bData.get(position).getTables() != null && issueData.bData.get(position).getTables().size() > 0)
                {
                    ViewGroup vg = (ViewGroup) mView.findViewById(R.id.issue_table_container);
                    for(int n = 0 ; n < issueData.bData.get(position).getTables().size() ; n++)
                    {
                        getActivity().getLayoutInflater().inflate(R.layout.issue_table, vg);
                        ViewGroup vg_table_parent = (ViewGroup) vg.getChildAt(n);
                        TextView tableTitle = ((TextView) vg_table_parent.getChildAt(0));
                        tableTitle.setVisibility(View.VISIBLE);
                        tableTitle.setText(issueData.bData.get(position).getTableTitles().get(n));
                        ViewGroup vg_table = (ViewGroup) vg_table_parent.getChildAt(1);
                        vg_table.setMinimumWidth(((MainActivity)getActivity()).getDisplaySize() - 100);
                        // get max table column size
                        int max = 0;
                        for(int i = 0 ; i < issueData.bData.get(position).getTables().get(n).size() ; i++)
                        {
                            if(max < issueData.bData.get(position).getTables().get(n).get(i).size())
                            {
                                max = issueData.bData.get(position).getTables().get(n).get(i).size();
                            }
                        }
                        // set Values to show
                        for(int i = 0 ; i < issueData.bData.get(position).getTables().get(n).size() ; i++)
                        {
                            getActivity().getLayoutInflater().inflate(R.layout.table_row, vg_table);
                            ViewGroup vg_row = (ViewGroup) vg_table.getChildAt(i);
                            for(int j = 0 ; j < max ; j++)
                            {
                                getActivity().getLayoutInflater().inflate(R.layout.table_text, vg_row);
                                String value = " ";
                                if(j < issueData.bData.get(position).getTables().get(n).get(i).size())
                                {
                                    value = issueData.bData.get(position).getTables().get(n).get(i).get(j);
                                }
                                ((TextView)vg_row.getChildAt(j)).setText(value);
                            }
                        }
                    }
                }
                mProgressBar.setVisibility(View.GONE);
            }
            else
            {
                mListener.onIssueDataMissing(false);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.next:
                switch (viewType)
                {
                    case 0:
                        if(position < issueData.bData.size() -1)
                        {
                            position++;
                            addIssueData();
                            ((ScrollView)getActivity().findViewById(R.id.issue_detail_scroll)).setScrollY(0);
                        }
                        break;

                    case 1:
                        if(position < issueData.data.size() -1)
                        {
                            position++;
                            addIssueData();
                            (getActivity().findViewById(R.id.issue_detail_scroll)).setScrollY(0);
                        }
                        break;

                    case 2:
                        if(position < issueData.searchData.size() -1)
                        {
                            position++;
                            addIssueData();
                            (getActivity().findViewById(R.id.issue_detail_scroll)).setScrollY(0);
                        }
                        break;
                }
                break;

            case R.id.previous:
                if(position > 0)
                {
                    position--;
                    addIssueData();
                    ((ScrollView)getActivity().findViewById(R.id.issue_detail_scroll)).setScrollY(0);
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnIssueDataMissingListener) {
            mListener = (OnIssueDataMissingListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnIssueDataMissingListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    // 文字列から議事録を取得
    static String getComments(String str)
    {
        String comments = "";
        int p1 = str.indexOf("<h3 class='page-header'>議事録");
        if(p1 !=  -1)
        {
            while(true)
            {
                int sp = p1;
                p1 = str.indexOf("<dt>", sp);
                if(p1 == -1)
                {
                    break;
                }
                int p2 = str.indexOf("</dt>", p1);
                comments = comments + "---< " + str.substring(p1+4,p2).trim() + " >---\n";
                p1 = str.indexOf("<pre>", p2);
                p2 = str.indexOf("</pre>", p1);
                comments = comments + str.substring(p1+5, p2).replace("&amp;", "&").replace("&quot;", "\"")
                        .replace("&lt;", "<").replace("&gt;", ">").trim() + "\n\n";
            }
        }
        return comments;
    }

    // 戻る進むボタンの有効・無効を設定
    void setEnableButtons(int position, boolean fIssues)
    {
        if(fIssues)
        {
            if(position == 0)
            {
                mView.findViewById(R.id.previous).setEnabled(false);
                mView.findViewById(R.id.next).setEnabled(true);
            }
            else if(position == issueData.data.size() -1)
            {
                mView.findViewById(R.id.next).setEnabled(false);
                mView.findViewById(R.id.previous).setEnabled(true);
            }
            else
            {
                mView.findViewById(R.id.next).setEnabled(true);
                mView.findViewById(R.id.previous).setEnabled(true);
            }
        }
        else
        {
            if(position == 0)
            {
                mView.findViewById(R.id.previous).setEnabled(false);
                mView.findViewById(R.id.next).setEnabled(true);
            }
            else if(position == issueData.bData.size() -1)
            {
                mView.findViewById(R.id.next).setEnabled(false);
                mView.findViewById(R.id.previous).setEnabled(true);
            }
            else
            {
                mView.findViewById(R.id.next).setEnabled(true);
                mView.findViewById(R.id.previous).setEnabled(true);
            }
        }

    }
    public interface OnIssueDataMissingListener
    {
        public void onIssueDataMissing(boolean isIssuesFragment);
    }

}
