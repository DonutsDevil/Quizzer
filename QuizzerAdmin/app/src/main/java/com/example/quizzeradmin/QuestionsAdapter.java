package com.example.quizzeradmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {
    private  List<QuestionModel> list;
    private String categoryName;
    private DeleteListener listener;

    public QuestionsAdapter(List<QuestionModel> list, String categoryName, DeleteListener listener) {
        this.list = list;
        this.categoryName = categoryName;
        this.listener = listener;
    }

    public QuestionsAdapter() {

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String question = list.get(position).getQuestion();
            String answer = list.get(position).getCorrectAns();
            holder.setData(question,answer,position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView questionTv,answerTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTv = itemView.findViewById(R.id.questionTv);
            answerTv = itemView.findViewById(R.id.answerTv);
        }

        private void setData(String question, String ans, final int position){
            questionTv.setText(position+1+". "+question);
            answerTv.setText("Ans. "+ans);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editIntent = new Intent(itemView.getContext(), AddQuestionActivity.class);
                    editIntent.putExtra("categoryName", categoryName);
                    editIntent.putExtra("setId",list.get(position).getSet());
                    editIntent.putExtra("position",position);
                    itemView.getContext().startActivity(editIntent);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClick(position,list.get(position).getId());
                    return true;
                }
            });
        }
    }

    public interface DeleteListener{
        void onLongClick(int position, String id);
    }
}
