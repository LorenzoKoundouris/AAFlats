package com.example.lorenzo.aaflats;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lorenzo on 10/02/2016.
 */
public class MyAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    public static Task task = new Task();
    private ArrayList<Task> mTaskList;

    public MyAdapter(Query query, ArrayList<Task> mTaskList) {
        this.mTaskList = mTaskList;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        //TaskViewHolder taskViewHolder = new TaskViewHolder(view);
        return new TaskViewHolder(itemView, mTaskList);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder taskViewHolder, int i) {
        //taskViewHolder.task = getItem(i);
        task = mTaskList.get(i);
        String titleLengthCheck = task.getTitle();
        if (titleLengthCheck.length() > 16) {
            titleLengthCheck = titleLengthCheck.substring(0, 16);
            taskViewHolder.taskTitle.setText(titleLengthCheck + "..");
        } else {
            taskViewHolder.taskTitle.setText(task.getTitle());
        }
        taskViewHolder.taskDescription.setText(task.getDescription());

        //taskViewHolder.taskPhoto.setImageResource(task.getPriority()); //Does this work? - nope
        int imgNum = task.getPriority();
        if(imgNum == 3){
            //high priority
            taskViewHolder.taskPhoto.setImageResource(R.drawable.low_priority_circle);//task.getPriority()
        } else if(imgNum == 2){
            //medium priority
            taskViewHolder.taskPhoto.setImageResource(R.drawable.medium_priority_circle);
        } else {
            taskViewHolder.taskPhoto.setImageResource(R.drawable.low_priority_circle);
        }

        boolean taskIsDone = task.getStatus();
        if (!taskIsDone) {
            taskViewHolder.taskButton.setBackgroundResource(R.drawable.todo);
        } else if (taskIsDone) {
            taskViewHolder.taskButton.setBackgroundResource(R.drawable.done);
        }

//        taskViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                v.getContext().startActivity(new Intent(v.getContext(), TaskDetails.class)
//                        .putExtra("parceable_task", task));
//                //Task pTask = mTaskList.get(getAdapterPosition());
//                //        Intent intent = new Intent(v.getContext(), TaskDetails.class);
//                //        intent.putExtra("parceable_task", pTask);
//                //        intent.putExtra("parceable_tasklist", mTaskList);
//                //        v.getContext().startActivity(intent);
//
//                System.out.println("You clicked on: " + task.getTitle());
//            }
//        });

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
