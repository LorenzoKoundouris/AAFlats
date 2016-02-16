package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 13/02/2016.
 */
public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected CardView cardView;
    protected TextView taskTitle;
    protected TextView taskProperty;
    protected ImageView taskPhoto;
    protected Button taskButton;
    protected ArrayList<Task> mTaskList;

    public TaskViewHolder(View itemView, ArrayList<Task> taskList) {
        super(itemView);
        cardView = (CardView) itemView.findViewById(R.id.card_view);
        taskTitle = (TextView) itemView.findViewById(R.id.task_title);
        taskProperty = (TextView) itemView.findViewById(R.id.task_property);
        taskPhoto = (ImageView) itemView.findViewById(R.id.task_photo);
        taskButton = (Button) itemView.findViewById(R.id.task_check);

        this.mTaskList = taskList;
        itemView.setOnClickListener(this);
//            taskButton.setOnClickListener(this);
//            cardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
//        //v.getContext().startActivity(new Intent(v.getContext(), TaskDetails.class).putExtra(TaskDetails.taskTitle, task.getTitle()));
        Task pTask = mTaskList.get(getAdapterPosition());
        Intent intent = new Intent(v.getContext(), TaskDetails.class);
        intent.putExtra("parceable_task", pTask);
        intent.putExtra("parceable_tasklist", mTaskList);
        v.getContext().startActivity(intent);

        //System.out.println("You clicked on: " + pTask.getTitle());

    }
}
