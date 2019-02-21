package com.cyber.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cyber.fastnotes.R;
import com.cyber.model.RowItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RowItemAdapter extends RecyclerView.Adapter<RowItemAdapter.RowItemViewHolder>{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm dd/MM/yyyy");

    private final List<RowItem> rowItemList = new ArrayList<>();

    OnItemPositionClickListener onItemClickListener = null;
    OnItemPositionClickListener onItemLongClickListener = null;


    public RowItemAdapter(){}

    public RowItemAdapter(Collection<? extends RowItem> items) {
        this.rowItemList.addAll(items);
    }

    public void setRowItemList(Collection<? extends RowItem> items){
        rowItemList.clear();
        rowItemList.addAll(items);
    }

    public void add(RowItem rowItem){
        rowItemList.add(rowItem);
    }

    public RowItem get(int index){
        return rowItemList.get(index);
    }

    public int getIndexById(Long id){
        for(int i=0; i<getItemCount(); i++){
            RowItem item = get(i);
            if (id.equals(item.getId())) return i;
        }
        return -1;
    }

    public void clear(){
        rowItemList.clear();
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

        View bindItem(RowItem item){
            txtTitle.setText( item.getTitle() );
            txtDateTime.setText( dateFormat.format(item.getDate()) );

            return cardView;
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
        View view = holder.bindItem( get(position) );

        if (onItemClickListener !=null) view.setOnClickListener( v -> onItemClickListener.onItemClick(v, position));
        if (onItemLongClickListener !=null) view.setOnClickListener( v  -> onItemLongClickListener.onItemClick(v, position));
    }

    @Override
    public int getItemCount() {
        return rowItemList.size();
    }

    public void setOnItemPositionClickListener(OnItemPositionClickListener clickListener){
        this.onItemClickListener = clickListener;
    }

    public void setOnItemPositionLongClickListener(OnItemPositionClickListener longClickListener){
        this.onItemLongClickListener = longClickListener;
    }

}
