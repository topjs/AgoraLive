package io.agora.vlive.agora.rtm.model;

public class RoomMessageData {
    int cmd;
    RoomMessage data;

    public RoomMessageData(int cmd, int operate) {
        this.cmd = cmd;
        data = new RoomMessage(operate);
    }

    public class RoomMessage {
        int operate;

        public RoomMessage(int operate) {
            this.operate = operate;
        }
    }
}
