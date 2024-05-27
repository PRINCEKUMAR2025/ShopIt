package com.example.princeecommerceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.princeecommerceapp.R;
import com.example.princeecommerceapp.models.ProfileAddressModel;

import java.util.List;

public class ProfileAddressAdapter extends RecyclerView.Adapter<ProfileAddressAdapter.ViewHolder> {

    private List<ProfileAddressModel> addressModelList;

    public ProfileAddressAdapter(Context context, List<ProfileAddressModel> addressModelList) {
        this.addressModelList = addressModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_address_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProfileAddressModel address = addressModelList.get(position);
        holder.addressTextView.setText(address.getUserAddress());
    }

    @Override
    public int getItemCount() {
        return addressModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView addressTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.address_add);
        }
    }
}
