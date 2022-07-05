package com.example.blindtravel;

import com.example.blindtravel.models.SignupUser;
import com.example.blindtravel.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("/login-conductor")
    Call<String> login(@Query("email") String email,
                       @Query("passwrd") String passwrd);


    @POST("/login-client")
    Call<String> loginClient(@Query("email") String email,
                             @Query("passwrd") String passwrd);

    @POST("/signup-client-new")
    Call<String> signupTest(@Query("name") String name,
                        @Query("email") String email,
                        @Query("passwrd") String passwrd,
                        @Query("mob") String mobile,
                        @Query("lat") String lat,
                        @Query("lon") String lon,
                        @Query("dest") String dest,
                        @Query("role") String role,
                        @Query("pic") String pic,
                        @Query("requested") Boolean requested);

    @POST("/signup-conductor-new")
    Call<String> signupObj(@Query("name") String name,
                           @Query("email") String email,
                           @Query("passwrd") String passwrd,
                           @Query("mob") String mobile,
                           @Query("lat") String lat,
                           @Query("lon") String lon,
                           @Query("dest") String dest,
                           @Query("role") String role,
                           @Query("pic") String pic,
                           @Query("requested") Boolean requested);

    @GET("/get-conductors")
    Call<String> getConductors(@Query("dest") String destination, @Query("email") String email);

    @GET("/get-clients")
    Call<String> getClients(@Query("dest") String destination, @Query("email") String email);

    @GET("/get-conductor")
    Call<String> getConductor(@Query("email") String email);

    @GET("/get-client")
    Call<String> getClient(@Query("email") String email);

    @POST("/update-client")
    Call<User> updateClient(@Query("name") String name,
                            @Query("email") String email,
                            @Query("passwrd") String passwrd,
                            @Query("mob") String mobile,
                            @Query("lat") String lat,
                            @Query("lon") String lon,
                            @Query("dest") String dest,
                            @Query("role") String role,
                            @Query("pic") String pic,
                            @Query("requested") Boolean requested);

    @POST("/update-conductor")
    Call<User> updateConductor(@Query("name") String name,
                               @Query("email") String email,
                               @Query("passwrd") String passwrd,
                               @Query("mob") String mobile,
                               @Query("lat") String lat,
                               @Query("lon") String lon,
                               @Query("dest") String dest,
                               @Query("role") String role,
                               @Query("pic") String pic,
                               @Query("requested") Boolean requested);

    @POST("/update-current-loc-client")
    Call<String> updateClientLoc(@Query("email") String id, @Query("latitude") String latitude, @Query("longitude") String longitude);

    @POST("/update-current-loc-conductor")
    Call<String> updateConductorLoc(@Query("email") String id, @Query("latitude") String latitude, @Query("longitude") String longitude);

    @POST("/remove-dest-client")
    Call<String> removeDestClient(@Query("email") String email);

    @POST("/remove-dest-conductor")
    Call<String> removeDestConductor(@Query("email") String email);

}
