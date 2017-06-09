package com.kumano_ryo.shijubo.kumano_dormitoryapp.data;

import android.app.Application;

import com.kumano_ryo.shijubo.kumano_dormitoryapp.IssueItem;

import java.util.ArrayList;

/**
 * Created by shijubo on 2017/05/22.
 * アプリケーション全体の共有変数
 */

public class IssueData extends Application {
    public ArrayList<IssueItem> data = new ArrayList<>();
    public ArrayList<IssueItem> bData = new ArrayList<>();
    public ArrayList<IssueItem> searchData = new ArrayList<>();
    public ArrayList<String> blockCData = new ArrayList<>();
    public ArrayList<String> blockCTitle = new ArrayList<>();
    public void clearData(){
        this.data = new ArrayList<>();
        this.bData = new ArrayList<>();
        this.blockCData = new ArrayList<>();
        this.blockCTitle = new ArrayList<>();
    }
}
