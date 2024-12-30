package com.life.lifelink.api;

import com.life.lifelink.model.JwtResponse;
import com.life.lifelink.model.LoginRequest;
import com.life.lifelink.model.MessageResponse;
import com.life.lifelink.model.SignupRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/signup")
    Call<MessageResponse> signup(@Body SignupRequest request);

    @POST("api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest request);
}