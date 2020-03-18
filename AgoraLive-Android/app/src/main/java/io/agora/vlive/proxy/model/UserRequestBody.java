package io.agora.vlive.proxy.model;

public class UserRequestBody {
    String userName;
    String avatar;

    public UserRequestBody(String name, String avatar) {
        this.userName = name;
        this.avatar = avatar;
    }
}
