package com.example.foolishguy.geo_fire_demo;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.*;
import java.util.Map;

/**
 * Created by Foolish Guy on 1/3/2017.
 */

public class UserListAdapter extends RecyclerView.Adapter <UserListAdapter.Holder> {

    private static String TAG = "UserListAdapter";
    java.util.Map<String, Detail> list;
    List <Detail> list_data = new ArrayList<Detail>();
    public UserListAdapter (Map<String, Detail> list) {
        Log.d(TAG, "LIST DATA" );
        list_data.addAll(list.values());
        for (Map.Entry<String, Detail> entry : list.entrySet())
        {
            Log.d(TAG, entry.getKey() + "/" + entry.getValue());
        }
        this.list = list;
    }
    public class Holder extends RecyclerView.ViewHolder {
    public TextView userName, userPhone;
        public Holder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            userPhone = (TextView) itemView.findViewById(R.id.user_phone);
        }
    }
    @Override
    public UserListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(UserListAdapter.Holder holder, int position) {

        holder.userPhone.setText(list_data.get(position).getPhone());
        holder.userName.setText(list_data.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return list_data.size();
    }
}
