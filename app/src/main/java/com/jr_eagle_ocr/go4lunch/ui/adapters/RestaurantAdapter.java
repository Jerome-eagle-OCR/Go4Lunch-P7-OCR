package com.jr_eagle_ocr.go4lunch.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.ItemRestaurantBinding;
import com.jr_eagle_ocr.go4lunch.ui.viewstates.RestaurantViewSate;

import java.util.List;

/**
 * @author jrigault
 * {@link RecyclerView.Adapter} that can display a {@link RestaurantViewSate}.
 */
public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private final List<RestaurantViewSate> items;
    private final RestaurantAdapter.DisplayRestaurantListener listener;


    class RestaurantViewHolder extends RecyclerView.ViewHolder {
        public final Context context;
        public final ImageView photoImageView;
        public final TextView nameTextView;
        public final TextView distanceTextView;
        public final TextView addressTextView;
        public final TextView joinersTextView;
        public final TextView openingTextView;
        public final RatingBar ratingBar;

        public RestaurantViewHolder(
                @NonNull ItemRestaurantBinding binding
        ) {
            super(binding.getRoot());
            context = binding.getRoot().getContext();
            photoImageView = binding.restaurantItemPicture;
            nameTextView = binding.restaurantItemName;
            distanceTextView = binding.restaurantItemDistance;
            addressTextView = binding.restaurantItemAddress;
            joinersTextView = binding.restaurantItemJoiners;
            openingTextView = binding.restaurantItemOpening;
            ratingBar = binding.restaurantItemRatingBar;
        }

        public void bind(RestaurantViewSate currentRestaurant) {
            Bitmap photo = currentRestaurant.getPhoto();
            Bitmap noPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_photo);
            photo = photo != null ? photo : noPhoto;
            photoImageView.setImageBitmap(photo);

            nameTextView.setText(currentRestaurant.getName());
            distanceTextView.setText(currentRestaurant.getDistance());
            addressTextView.setText(currentRestaurant.getAddress());

            boolean isJoinersVisible = currentRestaurant.isJoinersVisible();
            int joinersVisibility = isJoinersVisible ? View.VISIBLE : View.INVISIBLE;
            joinersTextView.setVisibility(joinersVisibility);
            joinersTextView.setText(currentRestaurant.getJoiners());

            boolean isWarningStyle = currentRestaurant.isWarningStyle();
            int warningColor = context.getResources().getColor(R.color.design_default_color_error);
            String openingPrefix = context.getString(currentRestaurant.getOpeningPrefix());
            String openingText = openingPrefix + currentRestaurant.getClosingTime();
            openingText = isWarningStyle ? ("<b>" + openingText + "</b>") : ("<i>" + openingText + "</i>");
            if (isWarningStyle) openingTextView.setTextColor(warningColor);
            openingTextView.setText(openingText);

            openingTextView.setText(Html.fromHtml(openingText));

            ratingBar.setRating(currentRestaurant.getRating());

            itemView.setOnClickListener(v -> listener.onDisplayRestaurant(currentRestaurant.getId()));
        }

    }

    public interface DisplayRestaurantListener {
        void onDisplayRestaurant(String restaurantId);
    }

    public RestaurantAdapter(List<RestaurantViewSate> items, DisplayRestaurantListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RestaurantViewHolder(ItemRestaurantBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final RestaurantViewHolder holder, int position) {
        RestaurantViewSate currentRestaurant = items.get(position);
        holder.bind(currentRestaurant);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @SuppressLint("NotifyDataSetChanged") //Simple refresh is fine
    public void updateItems(List<RestaurantViewSate> restaurantViewSates) {
        items.clear();
        items.addAll(restaurantViewSates);
        notifyDataSetChanged();
    }
}