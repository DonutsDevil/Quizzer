package com.example.quizzeradmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class GridAdapter extends BaseAdapter {
    public List<String> sets;
    private String category;
    private GridListener gridListener;
    public GridAdapter(List<String> sets, String category, GridListener gridListener) {
        this.sets = sets;
        this.category = category;
        this.gridListener = gridListener;
    }

    @Override
    public int getCount() {
        return sets.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sets_item,parent,false);
        }
        if (position == 0){
            ((TextView)view.findViewById(R.id.sets_textView)).setText("+");
        }else{
            ((TextView)view.findViewById(R.id.sets_textView)).setText(String.valueOf(position));
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0){
                    gridListener.addSet();
                }else {
                Intent questionIntent = new Intent(parent.getContext(), QuestionsActivity.class);
                questionIntent.putExtra("category",category);
                questionIntent.putExtra("setId",sets.get(position - 1));
                parent.getContext().startActivity(questionIntent);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (position != 0) {
                    gridListener.onLongClick(sets.get(position-1), position);
                }
                return false;

            }
        });
        
        return view;
    }
    public interface  GridListener{
        void addSet();
        void onLongClick(String setId, int position);
    }
}
