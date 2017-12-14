package com.kumano_ryo.shijubo.kumano_dormitoryapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NewsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView textView;
    // プログレスバー
    private ProgressBar progressBar;
    // プログレスバー表示時の背景
    private Drawable progressBarBackground;
    private ScrollView scrollView;

    private OnFragmentInteractionListener mListener;

    public NewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsFragment newInstance(String param1, String param2) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final android.os.Handler handler = new android.os.Handler();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        scrollView = (ScrollView) view.findViewById(R.id.newsScrollView);
        progressBar = (ProgressBar) view.findViewById(R.id.newsProgressBar);
        progressBarBackground = view.findViewById(R.id.newsProgressBarBackground).getBackground();
        textView = (TextView) view.findViewById(R.id.newsTextView);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.newsFloatingAction);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "更新中", Snackbar.LENGTH_SHORT).show();
                onFabPressed();
            }
        });

        assert ((MainActivity) getActivity()).getSupportActionBar() != null;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("寮生大会議事録");

        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // データが存在しない場合はプログレスバーを表示する
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
                try
                {
                    URL url = new URL("http://gijiroku.herokuapp.com/content");
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    final String str = InputStreamToString(con.getInputStream());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            textView.setText(str);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void onFabPressed() {
        // Inflate the layout for this fragment
        final android.os.Handler handler = new android.os.Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // データが存在しない場合はプログレスバーを表示する
                        if(progressBar != null)
                        {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                });
                try
                {
                    URL url = new URL("http://gijiroku.herokuapp.com/content");
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    final String str = InputStreamToString(con.getInputStream());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(textView != null)
                            {
                                progressBar.setVisibility(View.GONE);
                                textView.setText(str);
                                scrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.fullScroll(View.FOCUS_DOWN);
                                    }
                                });
                            }
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
    }

    /*

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    */

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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
