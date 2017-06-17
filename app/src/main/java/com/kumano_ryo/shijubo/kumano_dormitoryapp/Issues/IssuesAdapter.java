package com.kumano_ryo.shijubo.kumano_dormitoryapp.Issues;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kumano_ryo.shijubo.kumano_dormitoryapp.R;

import java.util.ArrayList;

/**
 * Created by shijubo on 2017/05/21.
 */

public class IssuesAdapter extends RecyclerView.Adapter<IssuesViewHolder> {
    private Context context;
    private ArrayList<IssueItem> data;
    private onItemClickListener listener;
    private boolean isBlockCIssuesData;

    public IssuesAdapter(Context context, ArrayList<IssueItem> data, boolean isBlockCIssuesData)
    {
        this.context = context;
        this.isBlockCIssuesData = isBlockCIssuesData;
        this.data = data;
    }

    public void setOnClickListener(onItemClickListener listener)
    {
        this.listener = listener;
    }

    public void add(IssueItem issueItem)
    {
        this.data.add(issueItem);
    }

    @Override
    public IssuesViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.issues_list_item, parent, false);
        return new IssuesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(IssuesViewHolder holder, final int position)
    {
        if (this.data.size() > position)
        {
            holder.title.setText(this.data.get(position).getTitle());
            if(isBlockCIssuesData)
            {
                holder.detail.setText(this.data.get(position).getOverView());
            }
            else
            {
                holder.detail.setText(this.data.get(position).getDetail());
            }
            holder.info.setText(this.data.get(position).getInfo());

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view, position);
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return this.data.size();
    }


    public interface onItemClickListener {
        void onClick(View view, int position);
    }

}
