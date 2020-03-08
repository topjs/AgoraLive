package io.agora.vlive.proxy.model;

public class RoomInfo {
    public RoomInfo(String id, String name, String thumb, int userCount, int ownerId) {
        roomId = id;
        roomName = name;
        thumbnail = thumb;
        currentUsers = userCount;
        ownerUid = ownerId;
    }

    public String roomId;
    public String roomName;
    public String thumbnail;
    public int currentUsers;
    public int ownerUid;
}
