package io.agora.vlive.proxy.struts.request;

public class ModifySeatStateRequest extends RoomRequest {
    public int no;
    public String userId;
    public int state;

    public ModifySeatStateRequest(String token, String roomId,
                                  String userId, int coindex, int state) {
        this.token = token;
        this.roomId = roomId;
        this.userId = userId;
        this.state = state;
        no = coindex;
    }
}
