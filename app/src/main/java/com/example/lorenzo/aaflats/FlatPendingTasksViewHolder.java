package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 25/02/2016.
 */
public class FlatPendingTasksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    protected ArrayList<Task> flatPendingTasks;
    protected ArrayList<String> flatPendingTasksKeys;
    protected TextView flatPendingTaskTitle;

    public FlatPendingTasksViewHolder(View itemView, ArrayList<Task> flatPendingTasks, ArrayList<String> flatPendingTasksKeys) {
        super(itemView);
        flatPendingTaskTitle = (TextView) itemView.findViewById(R.id.flat_task_title);

        this.flatPendingTasks = flatPendingTasks;
        this.flatPendingTasksKeys = flatPendingTasksKeys;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Task pTask = flatPendingTasks.get(getAdapterPosition());
        String pFlatTaskKey = flatPendingTasksKeys.get(getAdapterPosition());
        Intent intent = new Intent(v.getContext(), TaskDetails.class);
        intent.putExtra("parceable_task", pTask);
        intent.putExtra("parceable_task_key", pFlatTaskKey);
        v.getContext().startActivity(intent);
    }
}
