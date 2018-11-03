package com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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

public class BlockCFragment extends Fragment {

    // プロック会議一覧を表示するアダプター
    private ArrayAdapter<String> adapter;
    // ブロック会議がクリックされたときのListener
    private OnBlockCItemClickedListener mListener;
    // プログレスバー
    private ProgressBar mProgressBar;
    // プログレスバー表示時の背景
    private Drawable progressBarBackground;
    // アプリケーション全体で共有される変数
    private IssueData issueData;

    public BlockCFragment() {
        // Required empty public constructor
    }


    public static BlockCFragment newInstance() {
        BlockCFragment fragment = new BlockCFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        issueData = (IssueData)getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final android.os.Handler handler = new android.os.Handler();
        View view = inflater.inflate(R.layout.fragment_block_c, container, false);

        // プログレスバーの表示
        mProgressBar = (ProgressBar) view.findViewById(R.id.blockCProgressBar);
        // プログレスバー表示時の背景を設定
        progressBarBackground = view.findViewById(R.id.blockCProgressBarBackground).getBackground();

        assert ((MainActivity) getActivity()).getSupportActionBar() != null;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("ブロック会議一覧");

        if(issueData.blockCTitle == null)
        {
            issueData.blockCTitle = new ArrayList<>();
        }
        adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, issueData.blockCTitle);
        ListView list = (ListView) view.findViewById(R.id.block_c_list);
        list.setAdapter(adapter);
        // ブロック会議がクリックされた時の処理を設定
        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        if(mListener != null)
                        {
                            mListener.onBlockCItemClicked(position);
                        }
                    }
                }
        );

        // http通信を行ってブロック会議一覧を取得
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(issueData.blockCData.size() < 17)
                        {
                            // データが存在しない場合はプログレスバーを表示する
                            mProgressBar.setVisibility(View.VISIBLE);
                            progressBarBackground.setAlpha(100);
                        }
                        else
                        {
                            mProgressBar.setVisibility(View.GONE);
                            progressBarBackground.setAlpha(0);
                        }
                    }
                });
                if(issueData.blockCData.size() >= 17)
                {
                    // データが存在するので新規取得は行わない
                    return;
                }
                try
                {
                    int index = 0;
                    ArrayList<String> data = new ArrayList<>();
                    while (index < MainActivity.domains.length) {
                        URL url = new URL("http://docs." + MainActivity.domains[index] + "/browse_document/");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        String str = InputStreamToString(con.getInputStream());

                        data = new ArrayList<>();
                        issueData.blockCData = new ArrayList<>();
                        int sp = 0;
                        int i = 0;
                        while (true) {
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
                            issueData.blockCData.add(path);

                            p1 = part.indexOf(">");
                            p2 = part.lastIndexOf("<");
                            String title = part.substring(p1 + 1, p2).replace("&amp;", "&").replace("&quot;", "\"")
                                    .replace("&lt;", "<").replace("&gt;", ">").trim(); // get title
                            data.add(title);
                        }
                        if (data.size() > 0) {
                            break;
                        }
                        index++;
                    }
                    final ArrayList<String> finalData = data;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0 ; i < finalData.size() ; i++)
                            {
                                issueData.blockCTitle.add(finalData.get(i));
                                adapter.notifyDataSetChanged();
                            }
                            mProgressBar.setVisibility(View.GONE);
                            progressBarBackground.setAlpha(0);
                        }
                    });
                }
                catch(IOException e)
                {
                    System.out.println(e);
                }
                catch(Exception ex)
                {
                    System.out.println(ex);
                }
            }
        }).start();
        adapter.notifyDataSetChanged();
        return view;
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
        if (context instanceof OnBlockCItemClickedListener) {
            mListener = (OnBlockCItemClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBlockCItemClickedListener");
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
    public interface OnBlockCItemClickedListener {
        void onBlockCItemClicked(int position);
    }
}
