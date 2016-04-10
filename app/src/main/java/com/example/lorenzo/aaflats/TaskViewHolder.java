package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 13/02/2016.
 */
public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected CardView cardView;
    protected TextView taskTitle;
    protected TextView taskProperty;
    protected ImageView taskPhoto;
    protected ImageView taskButton;
    protected ArrayList<Task> mTaskList;
    protected ArrayList<String> taskKeys;

    public TaskViewHolder(View itemView, ArrayList<Task> taskList) {
        super(itemView);
        cardView = (CardView) itemView.findViewById(R.id.task_card_view);
        taskTitle = (TextView) itemView.findViewById(R.id.task_title);
        taskProperty = (TextView) itemView.findViewById(R.id.task_property);
        taskPhoto = (ImageView) itemView.findViewById(R.id.task_photo);
        taskButton = (ImageView) itemView.findViewById(R.id.task_check);

        this.mTaskList = taskList;
        itemView.setOnClickListener(this);
//            taskButton.setOnClickListener(this);
//            cardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
////        //v.getContext().startActivity(new Intent(v.getContext(), TaskDetails.class).putExtra(TaskDetails.taskTitle, mTask.getTitle()));
//        Task pTask = mTaskList.get(getAdapterPosition());
////        String pTaskKey = mTaskList.get(getAdapterPosition()).getTaskKey();
////        String pTaskKey = taskKeys.get(getAdapterPosition());
//        Intent intent = new Intent(v.getContext(), TaskDetails.class);
//        intent.putExtra("parceable_task", pTask);
//        //intent.putExtra("parceable_tasklist", mTaskList);
////        intent.putExtra("parceable_task_key", pTaskKey);
//        v.getContext().startActivity(intent);
//
//        //System.out.println("You clicked on: " + pTask.getTitle());

        v.getContext().startActivity(new Intent(v.getContext(), TaskDetails.class)
                .putExtra("parceable_task", mTaskList.get(getAdapterPosition())));

    }
}
