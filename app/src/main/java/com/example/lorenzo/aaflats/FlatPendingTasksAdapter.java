package com.example.lorenzo.aaflats;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 25/02/2016.
 */
public class FlatPendingTasksAdapter extends RecyclerView.Adapter<FlatPendingTasksViewHolder>{

    private ArrayList<Task> flatPendingTasks;
//    private ArrayList<String> flatPendingTasksKeys;

    public FlatPendingTasksAdapter(ArrayList<Task> flatPendingTasks) { //, ArrayList<String> flatPendingTasksKeys
        this.flatPendingTasks = flatPendingTasks;
//        this.flatPendingTasksKeys = flatPendingTasksKeys;
    }

    @Override
    public FlatPendingTasksViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View flatTaskView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flat_task_item, viewGroup, false);
        return new FlatPendingTasksViewHolder(flatTaskView,flatPendingTasks); //, flatPendingTasksKeys
    }

    @Override
    public void onBindViewHolder(FlatPendingTasksViewHolder flatPendingTasksViewHolder, int position) {
        Task tsk = flatPendingTasks.get(position);
        flatPendingTasksViewHolder.flatPendingTaskTitle.setText(tsk.getTitle());
    }

    @Override
    public int getItemCount() {
        return flatPendingTasks.size();
    }

}
