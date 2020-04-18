package io.agora.vlive.proxy.interfaces;

import io.agora.vlive.proxy.struts.response.RoomListResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface RoomListService {
    @GET("ent/v1/room/page")
    Call<RoomListResponse> requestRoomList(@Header("reqId") long reqId, @Header("token") String token,
                                           @Header("reqType") int reqType, @Query("nextId") String nextId,
                                           @Query("count") int count, @Query("type") int type,
                                           @Query("pkState") Integer pkState);
}
