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
    private OnItemLongClickListener longClickListener;

    public OrdersAdapter(Context context, List<OrdersModel> ordersModelList) {
        this.ordersModelList = ordersModelList;
    }

    @NonNull
    @Override
    public OrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_item, parent, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(@NonNull OrdersAdapter.ViewHolder holder, int position) {
        OrdersModel order = ordersModelList.get(position);
        holder.userNormalOrder.setText(order.getUserOrder());

        // Set the order status
        holder.orderStatus.setText("Status: " + order.getOrderStatus());

        // Set the long click listener for cancellation
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(order);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return ordersModelList.size();
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(OrdersModel orderModel);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNormalOrder;
        TextView orderStatus; // New field

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNormalOrder = itemView.findViewById(R.id.orders_add);
            orderStatus = itemView.findViewById(R.id.order_status); // Initialize status TextView
        }
    }
}