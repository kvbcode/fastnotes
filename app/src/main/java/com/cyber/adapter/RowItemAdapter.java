package com.cyber.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyber.fastnotes.R;
import com.cyber.model.RowItem;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

public class RowItemAdapter extends RecyclerView.Adapter<RowItemAdapter.RowItemViewHolder>{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm dd/MM/yyyy");

    private List<RowItem> messageRowItemList = Collections.emptyList();

    public RowItemAdapter(){}

    public RowItemAdapter(List<RowItem> messageRowItemList) {
        this.messageRowItemList = messageRowItemList;
    }

    public void setRowItemList(List<RowItem> messageRowItemList){
        this.messageRowItemList = messageRowItemList;
    }

    static class RowItemViewHolder extends RecyclerView.ViewHolder{
        View container;

        TextView txtTitle;
        TextView txtDateTime;
        CardView cardView;

        RowItemViewHolder(View container) {
            super(container);
            this.container = container;

            txtTitle = container.findViewById(R.id.txtTitle);
            txtDateTime = container.findViewById(R.id.txtDateTime);
            cardView = container.findViewById(R.id.messageRowItemCardView);
        }

        void bindItem(RowItem item, View.OnClickListener clickListener){
            txtTitle.setText( item.getTitle() );
            txtDateTime.setText( dateFormat.format(item.getDate()) );

            if (clickListener!=null) cardView.setOnClickListener( clickListener );
        }
    }

    @NonNull
    @Override
    public RowItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View rowItemView = inflater.inflate(R.layout.row_item, parent, false);
        return new RowItemViewHolder( rowItemView );
    }

    @Override
    public void onBindViewHolder(@NonNull RowItemViewHolder holder, int position) {
        holder.bindItem( messageRowItemList.get(position), null );
    }

    @Override
    public int getItemCount() {
        return messageRowItemList.size();
    }
}