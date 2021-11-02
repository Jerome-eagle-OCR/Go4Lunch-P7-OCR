package com.jr_eagle_ocr.go4lunch.ui.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jr_eagle_ocr.go4lunch.databinding.ItemUserBinding;
import com.jr_eagle_ocr.go4lunch.ui.adaptersviewstates.UserViewState;

import java.util.List;

/**
 * @author jrigault
 * {@link RecyclerView.Adapter} that can display a {@link UserViewState}.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<UserViewState> items;
    @Nullable
    private final DisplayChosenRestaurantListener listener;


    class UserViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final ImageView picture;
        private final TextView lunchTextView;

        public UserViewHolder(
                @NonNull ItemUserBinding binding
        ) {
            super(binding.getRoot());
            picture = binding.rvUserPhoto;
            lunchTextView = binding.rvUserJoining;
            context = binding.getRoot().getContext();
        }

        public void bind(UserViewState currentUser) {
            String userName = currentUser.getName();
            int appendString = currentUser.getAppendingString();
            String append = context.getString(appendString);
            String restaurantName = currentUser.getChosenRestaurantName();
            if (restaurantName != null) {
                append = append.concat(restaurantName);
            }
            lunchTextView.setText(userName.concat(append));
            setUserPhoto(currentUser);

            lunchTextView.setAlpha(currentUser.getTextAlpha());
            picture.setAlpha(currentUser.getImageAlpha());

            String chosenRestaurantId = currentUser.getChosenRestaurantId();
            if (chosenRestaurantId != null) {
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDisplayChosenRestaurant(chosenRestaurantId);
                    }
                });
            }
        }

        private void setUserPhoto(UserViewState currentUser) {
            Glide.with(context)
                    .load(currentUser.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(picture);
        }
    }


    public interface DisplayChosenRestaurantListener {

        void onDisplayChosenRestaurant(String restaurantId);
    }


    public UserAdapter(
            List<UserViewState> items,
            @Nullable DisplayChosenRestaurantListener listener
    ) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(ItemUserBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserViewState currentUser = items.get(position);
        holder.bind(currentUser);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @SuppressLint("NotifyDataSetChanged") //Simple refresh is fine
    public void updateItems(List<UserViewState> userViewStates) {
        items.clear();
        items.addAll(userViewStates);
        notifyDataSetChanged();
    }
}
