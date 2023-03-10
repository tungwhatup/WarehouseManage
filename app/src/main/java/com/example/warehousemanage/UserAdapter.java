package com.example.warehousemanage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    Context mContext;
    List<User> mListUser;

    public UserAdapter(Context mContext,List<User> userList) {
        this.mContext = mContext;
        this.mListUser = userList;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_single_user,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = mListUser.get(position);
        holder.tv_name.setText(user.getFullname());
        holder.tv_age.setText("Age: " + user.getAge());
        holder.tv_email.setText(user.getEmail());
        holder.tv_wh.setText(user.getIsusersof());
        holder.tv_phone.setText(user.getPhone());
    }

    @Override
    public int getItemCount() {
        if(mListUser != null) return mListUser.size();
        return 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_name, tv_email, tv_phone, tv_wh,tv_age;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_email = itemView.findViewById(R.id.tv_email);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            tv_wh = itemView.findViewById(R.id.tv_wh);
            tv_age = itemView.findViewById(R.id.tv_age);
        }
    }
}
