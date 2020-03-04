package io.agora.vlive.proxy.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface GeneralService {
    @GET("ent/v1/app/version")
    Call<ResponseBody> requestAppVersion(@Header("reqId") long reqId, @Header("reqType") int reqType,
                                         @Query("appCode") String appCode, @Query("osType") int osType,
                                         @Query("terminalType") int terminalType, @Query("appVersion") String appVersion);

    @GET("ent/v1/gifts")
    Call<ResponseBody> requestGiftList(@Header("reqId") long reqId, @Header("reqType") int reqType);

    @GET("ent/v1/music")
    Call<ResponseBody> requestMusicList(@Header("reqId") long reqId, @Header("reqType") int reqType);

    @GET("/ent/v1/room/{roomId}/token/refresh")
    Call<ResponseBody> refreshToken(@Header("reqId") long reqId, @Header("reqType") int reqType,
                                    @Query("roomId") String roomId);

    @GET("ent/v1/file/policy")
    Call<ResponseBody> requestOssPolicy(@Header("reqId") long reqId, @Header("reqType") int reqType,
                                        @Header("token") String token, @Query("type") int type);
}
