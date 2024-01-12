/**
 * This is a model class that represents a chat message in the chat system.
 * It stores the information related to a chat message, such as the sender ID, receiver ID, message content, date and time,
 * and information about the conversation, such as the conversation ID, name, and image.
 */

package com.example.gleechat.models;

import java.util.Date;

public class ChatMessage {
    public String senderId, receiverId, message, dataTime;
    public Date dateObject;
    public String conversionId, ConversionName, ConversionImage;
}
