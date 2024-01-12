/**
 * This class is responsible for creating an instance of the Retrofit client used for network communication.
 * It provides a static method getClient() that returns the Retrofit instance.
 * The Retrofit client is configured with the base URL and the ScalarsConverterFactory for string conversion.
 */

package com.example.gleechat.network;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;

    // Returns the Retrofit instance
    public static Retrofit getClient(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com/fcm/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

