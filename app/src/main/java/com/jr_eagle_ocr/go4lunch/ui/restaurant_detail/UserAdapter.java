package com.jr_eagle_ocr.go4lunch.ui.restaurant_detail;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.ItemUserBinding;
import com.jr_eagle_ocr.go4lunch.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> items;
    private boolean isJoiningUsers;

    public UserAdapter(List<User> items, boolean isJoiningUsers) {
        this.items = items;
        this.isJoiningUsers = isJoiningUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = items.get(position);
        if (isJoiningUsers) {
            holder.bindForJoiningUsers(currentUser);
        } else {
            holder.bindForUsers(currentUser);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<User> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        private final Context mContext;
        private final ImageView mImageView;
        private final TextView mTextView;


        public UserViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            mImageView = binding.rvUserPhoto;
            mTextView = binding.rvUserJoining;
            mContext = binding.getRoot().getContext();
        }

        public void bindForJoiningUsers(User currentUser) {
            String userIsJoining = currentUser.getUserName() + mContext.getString(R.string.is_joining);
            mTextView.setText(userIsJoining);

            setUserPhoto(currentUser);
        }

        public void bindForUsers(User currentUser) {
            String userIsEatingAt = currentUser.getUserName()
                    + mContext.getString(R.string.is_eating_at)
                    + currentUser.getChosenRestaurantId();
            mTextView.setText(userIsEatingAt);

            setUserPhoto(currentUser);
        }

        private void setUserPhoto(User currentUser) {
            Glide.with(mContext)
                    .load(currentUser.getUserUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(mImageView);
        }
    }
}
