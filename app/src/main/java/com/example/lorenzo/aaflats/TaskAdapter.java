package com.example.lorenzo.aaflats;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Lorenzo on 10/02/2016.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    public static Task mTask = new Task();
    private ArrayList<Task> mTaskList;

    public TaskAdapter(ArrayList<Task> mTaskList) {
        this.mTaskList = mTaskList;
    }

//    @Override
//    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//        View taskView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.task_item, viewGroup, false);
//        //TaskViewHolder taskViewHolder = new TaskViewHolder(view);
//        return new TaskViewHolder(taskView, mTaskList, taskKeys);
//    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView, mTaskList);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder taskViewHolder, int i) {
        //taskViewHolder.mTask = getItem(i);
        mTask = mTaskList.get(i);

        String titleLengthCheck = mTask.getTitle();
        if (titleLengthCheck.length() > 16) {
            titleLengthCheck = titleLengthCheck.substring(0, 16);
            taskViewHolder.taskTitle.setText(titleLengthCheck + "..");
        } else {
            taskViewHolder.taskTitle.setText(mTask.getTitle());
        }

        taskViewHolder.taskProperty.setText(mTask.getProperty());

        if(mTask.getPriority().matches("High")){
            //high priority
            taskViewHolder.taskPhoto.setImageResource(R.drawable.ic_flag_high_48dp);//mTask.getPriority()
        } else if(mTask.getPriority().matches("Medium")){
            //medium priority
            taskViewHolder.taskPhoto.setImageResource(R.drawable.ic_flag_medium_48dp);
        } else {
            //low priority
            taskViewHolder.taskPhoto.setImageResource(R.drawable.ic_flag_low_48dp);
        }

        boolean taskIsDone = mTask.getStatus();
        if (!taskIsDone) {
            taskViewHolder.taskButton.setBackgroundResource(R.drawable.ic_assignment_late_grey_48dp);
        } else if (taskIsDone) {
            taskViewHolder.taskButton.setBackgroundResource(R.drawable.ic_assignment_turned_in_grey_48dp);
        }
    }

    @Override
    public int getItemCount() {
        return mTaskList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
