package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 25/02/2016.
 */
public class FlatCompletedTasksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected ArrayList<Task> flatCompletedTasks;
//    protected ArrayList<String> flatCompletedTasksKeys;
    protected TextView flatCompletedTaskTitle;

    public FlatCompletedTasksViewHolder(View itemView, ArrayList<Task> flatCompletedTasks) { //, ArrayList<String> flatCompletedTasksKeys
        super(itemView);
        flatCompletedTaskTitle = (TextView) itemView.findViewById(R.id.flat_task_title);

        this.flatCompletedTasks = flatCompletedTasks;
//        this.flatCompletedTasksKeys = flatCompletedTasksKeys;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Task pTask = flatCompletedTasks.get(getAdapterPosition());
//        String pFlatTaskKey = flatCompletedTasksKeys.get(getAdapterPosition());
        Intent intent = new Intent(v.getContext(), TaskDetails.class);
        intent.putExtra("parceable_task", pTask);
//        intent.putExtra("parceable_task_key", pFlatTaskKey);
        v.getContext().startActivity(intent);
    }
}
