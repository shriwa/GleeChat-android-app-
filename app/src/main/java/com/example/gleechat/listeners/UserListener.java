/**
 * This is an interface for handling user click events.
 * It defines the onUserClicked method that will be implemented by classes
 * that need to listen to user click events.
 */

package com.example.gleechat.listeners;

import com.example.gleechat.models.user;

public interface UserListener {

    // Called when a user is clicked
    void onUserClicked(user users);
}
