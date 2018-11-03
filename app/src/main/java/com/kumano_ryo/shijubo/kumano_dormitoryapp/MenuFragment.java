package com.kumano_ryo.shijubo.kumano_dormitoryapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MenuFragment extends Fragment {

    private ProgressBar mProgressBar;
    private Drawable progressBarBackground;

    public MenuFragment() {
        // Required empty public constructor
    }
    
    public static MenuFragment newInstance(String param1, String param2) {
        MenuFragment fragment = new MenuFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        // プログレスバーを表示する
        mProgressBar = (ProgressBar) view.findViewById(R.id.menusProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        progressBarBackground = view.findViewById(R.id.progressBarBackground).getBackground();
        progressBarBackground.setAlpha(120);

        assert ((MainActivity) getActivity()).getSupportActionBar() != null;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("寮食メニュー");

        final android.os.Handler handler = new android.os.Handler();
        final TextView label_monday = (TextView) view.findViewById(R.id.label_monday);
        final TextView monday_lun = (TextView) view.findViewById(R.id.monday_lun);
        final TextView monday_lun_noodle = (TextView) view.findViewById(R.id.monday_lun_noodle);
        final TextView monday_din = (TextView) view.findViewById(R.id.monday_din);
        final TextView label_tuesday = (TextView) view.findViewById(R.id.label_tuesday);
        final TextView tuesday_lun = (TextView) view.findViewById(R.id.tuesday_lun);
        final TextView tuesday_lun_noodle = (TextView) view.findViewById(R.id.tuesday_lun_noodle);
        final TextView tuesday_din = (TextView) view.findViewById(R.id.tuesday_din);
        final TextView label_wednesday = (TextView) view.findViewById(R.id.label_wednesday);
        final TextView wednesday_lun = (TextView) view.findViewById(R.id.wednesday_lun);
        final TextView wednesday_lun_noodle = (TextView) view.findViewById(R.id.wednesday_lun_noodle);
        final TextView wednesday_din = (TextView) view.findViewById(R.id.wednesday_din);
        final TextView label_thursday = (TextView) view.findViewById(R.id.label_thursday);
        final TextView thursday_lun = (TextView) view.findViewById(R.id.thursday_lun);
        final TextView thursday_lun_noodle = (TextView) view.findViewById(R.id.thursday_lun_noodle);
        final TextView thursday_din = (TextView) view.findViewById(R.id.thursday_din);
        final TextView label_friday = (TextView) view.findViewById(R.id.label_friday);
        final TextView friday_lun = (TextView) view.findViewById(R.id.friday_lun);
        final TextView friday_lun_noodle = (TextView) view.findViewById(R.id.friday_lun_noodle);
        final TextView friday_din = (TextView) view.findViewById(R.id.friday_din);
        // 寮食メニューを読み込み
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    int index = 0;
                    ArrayList<String> data = new ArrayList<>();
                    while (index < MainActivity.domains.length) {
                        URL url = new URL("http://menus." + MainActivity.domains[index] + "/");

                        HttpURLConnection con = (HttpURLConnection)url.openConnection();
                        String str = InputStreamToString(con.getInputStream());

                        data = new ArrayList<>();
                        ReadMenu(str, data);

                        if(data.size() >= 20)
                        {
                            break;
                        }
                        index++;
                    }
                    if(data.size() < 20)
                    {
                        throw new Exception();
                    }
                    final ArrayList<String> finalData = data;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            label_monday.setText(finalData.get(0));
                            monday_lun.setText(finalData.get(1));
                            monday_lun_noodle.setText(finalData.get(2));
                            monday_din.setText(finalData.get(3));
                            label_tuesday.setText(finalData.get(4));
                            tuesday_lun.setText(finalData.get(5));
                            tuesday_lun_noodle.setText(finalData.get(6));
                            tuesday_din.setText(finalData.get(7));
                            label_wednesday.setText(finalData.get(8));
                            wednesday_lun.setText(finalData.get(9));
                            wednesday_lun_noodle.setText(finalData.get(10));
                            wednesday_din.setText(finalData.get(11));
                            label_thursday.setText(finalData.get(12));
                            thursday_lun.setText(finalData.get(13));
                            thursday_lun_noodle.setText(finalData.get(14));
                            thursday_din.setText(finalData.get(15));
                            label_friday.setText(finalData.get(16));
                            friday_lun.setText(finalData.get(17));
                            friday_lun_noodle.setText(finalData.get(18));
                            friday_din.setText(finalData.get(19));
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

        return view;
    }

    /**
     * メニューのデータを文字列から読み込みます
     * @param str htmlソース
     * @param data 文字列の配列に格納されたメニューのデータ
     */
    private void ReadMenu(String str, ArrayList<String> data)
    {
        int sp = str.indexOf("<h1>");
        while(true)
        {
            int p1 = str.indexOf("<div data-role=\"collapsible\">", sp);
            int p2 = str.indexOf("</div>", sp);
            if(p1 == -1 || p2 == -1)
            {
                break;
            }
            sp = p2 + 1;
            String part = str.substring(p1+4, p2);
            p1 = part.indexOf("<h3>");
            p2 = part.indexOf("</h3>");
            String day = part.substring(p1+4, p2).replace(" ", "").replace("&amp;", "&").replace("&quot;", "\"")
                    .replace("&lt;", "<").replace("&gt;", ">").trim(); // get date
            data.add(day);
            int current = 0;
            for(int j = 0 ; j < 3 ; j++)
            {
                p1 = part.indexOf("<pre>", current);
                p2 = part.indexOf("</pre>", current);
                if(p1 == -1 || p2 == -1)
                {
                    break;
                }
                current = p2 + 1;
                String menu = part.substring(p1+5, p2).replace("&amp;", "&").replace("&quot;", "\"")
                        .replace("&lt;", "<").replace("&gt;", ">").trim(); // get menu
                data.add(menu);
            }
        }
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
}
