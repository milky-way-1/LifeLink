package com.life.lifelink.api;

import com.life.lifelink.model.BookingRequest;
import com.life.lifelink.model.BookingResponse;
import com.life.lifelink.model.InsuranceRequest;
import com.life.lifelink.model.InsuranceResponse;
import com.life.lifelink.model.JwtResponse;
import com.life.lifelink.model.LoginRequest;
import com.life.lifelink.model.MessageResponse;
import com.life.lifelink.model.PatientRequest;
import com.life.lifelink.model.PatientResponse;
import com.life.lifelink.model.SignupRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/auth/signup")
    Call<MessageResponse> signup(@Body SignupRequest request);

    @POST("api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest request);

    @POST("api/patient/profile")
    Call<PatientResponse> createPatientProfile(@Header("Authorization") String token,
                    @Body PatientRequest request);

    @POST("api/insurance")
    Call<InsuranceResponse> createInsurance(@Header("Authorization") String token, @Body InsuranceRequest request);

    @GET("api/insurance")
    Call<List<InsuranceResponse>> getAllInsurance(@Header("Authorization") String token);
    @POST("api/bookings/request")
    Call<BookingResponse> requestAmbulance(@Body BookingRequest request);

    @GET("api/bookings/{bookingId}/status")
    Call<BookingResponse> getBookingStatus(@Path("bookingId") String bookingId);

    @GET("api/drivers/{driverId}/location")
    Call<DriverLocation> getDriverLocation(
            @Header("Authorization") String token,
            @Path("driverId") String driverId
    );

    @GET("api/drivers/{driverId}/route")
    Call<RouteResponse> getRoute(
            @Header("Authorization") String token,
            @Path("driverId") String driverId
    );
}