package io.agora.vlive.proxy.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UserService {
    @POST("ent/v1/user")
    Call<ResponseBody> createUser(@Header("reqId") long reqId, @Header("reqType") int reqType,
                                  @Body String userName, @Body String avator);

    @POST("/ent/v1/user/{userId}")
    Call<ResponseBody> editUser(@Header("token") String token,  @Header("reqId") long reqId,
                                @Header("reqType") int reqType, @Body String userName, @Body String avator);
}
