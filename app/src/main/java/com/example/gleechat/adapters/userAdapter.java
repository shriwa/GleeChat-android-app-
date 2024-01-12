/**
 * This is the adapter class for the user RecyclerView.
 * It is responsible for binding user data to the views in the RecyclerView.
 */

package com.example.gleechat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gleechat.databinding.ItemContainerUserBinding;
import com.example.gleechat.listeners.UserListener;
import com.example.gleechat.models.user;

import java.util.List;

public class userAdapter extends RecyclerView.Adapter<userAdapter.userViewHolder> {

    private final List<user> users;
    private final UserListener userListener;

    // Constructor for the adapter class
    public userAdapter(List<user> users, UserListener userListener){
        this.users = users;
        this.userListener = userListener;
    }

    // Inflates the item view layout and returns a new instance of the ViewHolder
    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new userViewHolder(itemContainerUserBinding);
    }

    // Binds the user data to the views in the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull userAdapter.userViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    // Returns the total number of users in the data set
    @Override
    public int getItemCount() {
        return users.size();
    }

    // ViewHolder class that holds references to the views in the item view layout
    class userViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;

        // Constructor for the ViewHolder class
        userViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        // Sets the user data to the views in the item view layout
        void setUserData(user user) {
            if (user != null) {
                binding.textName.setText(user.name);
                binding.textEmail.setText(user.email);
                binding.imageProfile.setImageBitmap(getUserImage(user.image));

                // Sets a click listener for the item view
                binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
            }
        }
    }


    // Decodes the Base64 encoded image and returns a Bitmap
    private Bitmap getUserImage(String encodedImage){
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null; // Return null or a default Bitmap if encodedImage is null
    }

}

