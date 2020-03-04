package io.agora.vlive.proxy.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LiveRoomService {
    @POST("ent/v1/room")
    Call<ResponseBody> createLiveRoom(@Header("token") String token, @Header("reqId") long reqId,
                                      @Header("reqType") int reqType, @Query("roomName") String roomName,
                                      @Query("type") int type);

    @POST("ent/v1/room/{roomId}/entry")
    Call<ResponseBody> enterLiveRoom(@Header("token") String token, @Header("reqId") long reqId,
                                     @Header("reqType") int reqType, @Path("roomId") String roomId);

    @POST("ent/v1/room/{roomId}/exit")
    Call<ResponseBody> leaveLiveRoom(@Header("token") String token, @Header("reqId") long reqId,
                                 @Header("reqType") int reqType, @Path("roomId") String roomId);

    @GET("/ent/v1/room/{roomId}/users")
    Call<ResponseBody> requestAudienceList(@Header("token") String token, @Header("reqId") long reqId,
                                           @Header("reqType") int reqType, @Path("roomId") String roomId);

    @GET("/ent/v1/room/{roomId}/seats")
    Call<ResponseBody> requestSeatState(@Header("token") String token, @Header("reqId") long reqId,
                                        @Header("reqType") int reqType, @Path("roomId") String roomId);

    @POST("/ent/v1/room/{roomId}/seat")
    Call<ResponseBody> modifySeatState(@Header("token") String token, @Header("reqId") long reqId,
                                       @Header("reqType") int reqType, @Path("roomId") String roomId,
                                       @Body int no, @Body String userId, @Body int state);

    @POST("v1/room/{roomId}/gift")
    Call<ResponseBody> sendGift(@Header("reqId") long reqId, @Header("reqType") int reqType,
                                @Path("roomId") String roomId, @Body String giftId, @Body int count);

    @GET("/v1/room/{roomId}/ranks")
    Call<ResponseBody> giftRank(@Header("reqId") long reqId, @Header("reqType") int reqType,
                                @Path("roomId") String roomId);

    @POST("/v1/room/{roomId}/pk")
    Call<ResponseBody> startStopPk(@Header("reqId") long reqId, @Header("reqType") int reqType,
                                   @Path("roomId") String myRoomId, @Body String roomId);
}
