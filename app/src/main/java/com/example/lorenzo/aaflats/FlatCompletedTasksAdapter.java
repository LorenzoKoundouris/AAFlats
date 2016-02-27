package com.example.lorenzo.aaflats;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 25/02/2016.
 */
public class FlatCompletedTasksAdapter extends RecyclerView.Adapter<FlatCompletedTasksViewHolder>{

    private ArrayList<Task> flatCompletedTasks;
    private ArrayList<String> flatCompletedTasksKeys;

    public FlatCompletedTasksAdapter(ArrayList<Task> flatCompletedTasks, ArrayList<String> flatCompletedTasksKeys) {
        this.flatCompletedTasks = flatCompletedTasks;
        this.flatCompletedTasksKeys = flatCompletedTasksKeys;
    }


    @Override
    public FlatCompletedTasksViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View flatTaskView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flat_task_item, viewGroup, false);
        return new FlatCompletedTasksViewHolder(flatTaskView,flatCompletedTasks,flatCompletedTasksKeys);
    }

    @Override
    public void onBindViewHolder(FlatCompletedTasksViewHolder flatCompletedTasksViewHolder, int position) {
        Task tsk = flatCompletedTasks.get(position);
        flatCompletedTasksViewHolder.flatCompletedTaskTitle.setText(tsk.getTitle());
    }

    @Override
    public int getItemCount() {
        return flatCompletedTasks.size();
    }
}
