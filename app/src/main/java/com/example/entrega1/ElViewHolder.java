package com.example.entrega1;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ElViewHolder extends RecyclerView.ViewHolder {
    public ImageView laimagen;
    public ElViewHolder (@NonNull View itemView){
        super(itemView);
        laimagen=itemView.findViewById(R.id.foto);
        /*itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/
    }
}
