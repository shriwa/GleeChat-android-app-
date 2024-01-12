/**
 * This is an interface for handling conversion click events.
 * It defines the onConversionClicked method that will be implemented by classes
 * that need to listen to conversion click events.
 */

package com.example.gleechat.listeners;

import com.example.gleechat.models.user;

public interface ConversionListener {

    // Called when a conversion is clicked
    void onConversionClicked(user user);
}
