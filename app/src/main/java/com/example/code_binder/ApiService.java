package com.example.code_binder;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.ArrayList;

public interface ApiService {
    @POST("api/addData")
    Call<Void> addDataToServer(@Body ArrayList<String> data);
}
