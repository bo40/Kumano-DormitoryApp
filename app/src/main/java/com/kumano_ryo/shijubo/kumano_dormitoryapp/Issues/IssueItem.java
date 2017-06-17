package com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues;

import java.util.ArrayList;

/**
 * Created by shijubo on 2017/05/21.
 */

public class IssueItem {
    private long id = 0;
    private String title = null;
    private String overView = null;
    private String detail = null;
    private String info = null;
    private String url = null;
    private ArrayList<String> tableTitles = null;
    private ArrayList<ArrayList<ArrayList<String>>> tables = null;

    public IssueItem(long id, String title, String overView, String detail, ArrayList<String> tableTitles, ArrayList<ArrayList<ArrayList<String>>> table, String info, boolean isContainOverView)
    {
        setId(id);
        setTitle(title);
        if(isContainOverView)
        {
            setOverView(overView);
        }
        setDetail(detail);
        setTableTitles(tableTitles);
        setTables(table);
        setInfo(info);
        setUrl("");
    }
    public IssueItem(long id, String title, String detail, String info, String path)
    {
        setId(id);
        setTitle(title);
        setDetail(detail);
        setInfo(info);
        setUrl(path);
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getOverView() { return overView; }
    public String getDetail() { return detail; }
    public ArrayList<String> getTableTitles() { return tableTitles; }
    public ArrayList<ArrayList<ArrayList<String>>> getTables() { return tables; }
    public String getInfo() { return info; }
    public String getUrl() { return url; }

    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setOverView(String overView) { this.overView = overView; }
    public void setDetail(String detail) { this.detail = detail; }
    public void setTableTitles(ArrayList<String> tableTitles) { this.tableTitles = tableTitles; }
    public void setTables(ArrayList<ArrayList<ArrayList<String>>> tables) { this.tables = tables; }
    public void setInfo(String info) { this.info = info; }
    public void setUrl(String url) { this.url = url; }
}
