package com.example.princeecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.princeecommerceapp.R;
import com.example.princeecommerceapp.models.OrdersModel;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {


    private List<OrdersModel> ordersModelList;

    public OrdersAdapter(Context context, List<OrdersModel> ordersModelList) {
        this.ordersModelList = ordersModelList;
    }

    @NonNull
    @Override
    public OrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersAdapter.ViewHolder holder, int position) {
        OrdersModel order = ordersModelList.get(position);
        holder.userNormalOrder.setText(order.getUserOrder());
    }

    @Override
    public int getItemCount() {
        return ordersModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView userNormalOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNormalOrder = itemView.findViewById(R.id.orders_add);
        }
    }
}
