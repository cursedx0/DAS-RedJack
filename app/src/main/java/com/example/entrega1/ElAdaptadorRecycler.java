package com.example.entrega1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ElAdaptadorRecycler extends RecyclerView.Adapter<ElViewHolder> {
    private List<String> listaCartas;
    //private int[] lasimagenes;
    public ElAdaptadorRecycler (List<String> lc)
    {
        this.listaCartas = lc;
    }

    @NonNull
    @Override
    public ElViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elLayoutItem= LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout,null);
        return new ElViewHolder(elLayoutItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ElViewHolder holder, int position) {
        //holder.eltexto.setText(losnombres[position]);
        int resourceId = holder.itemView.getContext().getResources().getIdentifier(
                "card_b_"+listaCartas.get(position)+"_large", "drawable", holder.itemView.getContext().getPackageName()
        );

        if (resourceId != 0) {
            holder.laimagen.setImageResource(resourceId);
        } else {
            holder.laimagen.setImageResource(R.drawable.icono_rombo);
        }

    }

    @Override
    public int getItemCount() {
        return listaCartas.toArray().length;
    }
}
