package com.kumano_ryo.shijubo.kumano_dormitoryapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class IssuesViewHolder extends RecyclerView.ViewHolder {
    View view;
    TextView title;
    TextView detail;
    TextView info;

    public IssuesViewHolder(View itemView)
    {
        super(itemView);
        this.view = itemView;
        this.title = (TextView) view.findViewById(R.id.issue_title);
        this.detail = (TextView) view.findViewById(R.id.issue_detail);
        this.info = (TextView) view.findViewById(R.id.issue_info);

    }
}
