/**
 * This is a model class that represents a user in the chat system.
 * It stores the information related to a user, such as the user's name, image, email, token, and ID.
 * The class implements the Serializable interface to allow objects of this class to be serialized and passed between components.
 */

package com.example.gleechat.models;

import java.io.Serializable;

public class user implements Serializable{
    public String name, image, email, token, id;
}
