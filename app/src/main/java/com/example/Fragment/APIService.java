package com.example.Fragment;

import com.example.Notification.MyResponse;
import com.example.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA1oxgX1M:APA91bFeSevkO3eNwIEG7xCPlBaMwz5f7X6iVmT3dj08j-TxW20V38fiuYwmmze4nyfvVpXcAQqyTjkfgXxkw3ZYdETE57FF_rRK-bt_7DwQd5U7ze7O0p4V23FuDS0AMFxdYrdoV_c5"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
