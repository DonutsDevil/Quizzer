package com.example.quizzer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.List;

public class GridAdapter extends BaseAdapter {
    private List<String> sets;
    private String category;
    private InterstitialAd mInterstitialAd;
    public GridAdapter(List<String> sets, String category, InterstitialAd mInterstitialAd) {
        this.sets = sets;
        this.category = category;
        this.mInterstitialAd = mInterstitialAd;

    }

    @Override
    public int getCount() {
        return sets.size();
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
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterstitialAd.setAdListener(new AdListener(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                        Intent questionIntent = new Intent(parent.getContext(), QuestionsActivity.class);
                        questionIntent.putExtra("category",category);
                        questionIntent.putExtra("setId",sets.get(position));
                        parent.getContext().startActivity(questionIntent);
                    }
                });
                if (mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                    return;
                }
                Intent questionIntent = new Intent(parent.getContext(), QuestionsActivity.class);
                questionIntent.putExtra("category",category);
                questionIntent.putExtra("setId",sets.get(position));
                parent.getContext().startActivity(questionIntent);
            }
        });
        ((TextView)view.findViewById(R.id.sets_textView)).setText(String.valueOf(position+1));
        return view;
    }
}
