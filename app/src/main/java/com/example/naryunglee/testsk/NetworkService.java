package com.example.naryunglee.testsk;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by leejiyoung on 2017-01-10.
 */

public interface NetworkService {

    @GET("/Membership/{mbs}")
    Call<List<Membership>> insert_member(@Path("mbs") String mbs);

    @POST("/Phonenumber/{pnm}")
    Call<List<Membership>> insert_phonenumber(@Path("pnm") String pnm);

    @POST("/Withdraw/{wd}")
    Call<List<Membership>> withdraw(@Path("wd") String wd);

    @POST("/findNumber/{fm}")
    Call<List<Membership>> find_number(@Path("fm") String fm);

    @POST("/modifyNumber/{mn}")
    Call<List<Membership>> modify_number(@Path("mn") String mn);

    @POST("/inquiry/{iq}")
    Call<List<Membership>> inquiry(@Path("iq") String iq);

    // 길 검색 내용으로 cctv 정보 찾기
    @GET("/data/cctv/{start_lat}/{start_lon}/{end_lat}/{end_lon}")
    Call<List<CCTV>> getCCTV(@Path("start_lat") double start_lat, @Path("start_lon") double start_lon,
                             @Path("end_lat") double end_lat, @Path("end_lon") double end_lon);

    @GET("/message/getall/{email}")   // 긴급 메세지 전송 때 폰 번호와 메세지 내용 가져오기
    Call<List<SMS>> getAll(@Path("email") String email);

    @GET("/message/get/{email}")      // 메세지 수정 시에 기존에 저장된 메세지 가져오기
    Call<List<Message2>> getMessage(@Path("email") String email);

    @POST("/message/save/{email}/{content}")    // 수정 메세지 저장
    Call<List<Message>> saveMessage(@Path("email") String email, @Path("content") String content);

    // 길 검색 내용으로 보행자 정보 찾기
    @GET("/data/jaywalking/{start_lat}/{start_lon}/{end_lat}/{end_lon}")
    Call<List<Jaywalk>> getJaywalk(@Path("start_lat") double start_lat, @Path("start_lon") double start_lon, @Path("end_lat") double end_lat, @Path("end_lon") double end_lon);

}