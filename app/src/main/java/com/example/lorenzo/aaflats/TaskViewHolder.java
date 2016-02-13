package com.example.lorenzo.aaflats;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Lorenzo on 13/02/2016.
 */
public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected CardView cardView;
    protected TextView taskTitle;
    protected TextView taskDescription;
    protected ImageView taskPhoto;
    protected Button taskButton;

    public TaskViewHolder(View itemView) {
        super(itemView);
        cardView = (CardView) itemView.findViewById(R.id.card_view);
        taskTitle = (TextView) itemView.findViewById(R.id.task_title);
        taskDescription = (TextView) itemView.findViewById(R.id.task_description);
        taskPhoto = (ImageView) itemView.findViewById(R.id.task_photo);
        taskButton = (Button) itemView.findViewById(R.id.task_check);

        itemView.setOnClickListener(this);
//            taskButton.setOnClickListener(this);
//            cardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
    }

//        @Override
//        public void onClick(View v) {
////            if (v.getId() == taskTitle.getId()){
////                Toast.makeText(v.getContext(), "ITEM PRESSED = " + String.valueOf(getAdapterPosition() + task.getTitle()), Toast.LENGTH_SHORT).show();
////            } else {
////                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()+ task.getTitle()
////                        + "\n " + v.getId() + "\n " + taskTitle.getId()), Toast.LENGTH_SHORT).show();
////            }
////
////
//
//            Intent intent = new Intent(v.getContext(), TaskDetails.class);
//            intent.putExtra("something", boobs);
//            intent.putExtra("thisShit", task);
//            v.getContext().startActivity(intent);
//        }

}
