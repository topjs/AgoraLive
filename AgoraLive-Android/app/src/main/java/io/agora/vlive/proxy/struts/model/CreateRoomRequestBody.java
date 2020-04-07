package io.agora.vlive.proxy.struts.model;

public class CreateRoomRequestBody {
    String roomName;
    int type;

    public CreateRoomRequestBody(String roomName, int type) {
        this.roomName = roomName;
        this.type = type;
    }
}
