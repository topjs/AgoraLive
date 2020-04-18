package io.agora.vlive.agora.rtm.model;

public class PeerMessageData {
    public int cmd;
    public PeerMessage data;

    public PeerMessageData(int cmd, String account, int operate, int coindex) {
        this.cmd = cmd;
        data = new PeerMessage(account, operate, coindex);
    }

    public PeerMessageData(int cmd, String account, int operate) {
        this.cmd = cmd;
        data = new PeerMessage(account, operate, 0);
    }

    public PeerMessageData(int cmd, String account ,int operate, String roomId) {
        this.cmd = cmd;
        data = new PeerMessage(account, operate, roomId);
    }

    public class PeerMessage {
        public String account;
        public int operate;
        public int coindex;
        public String pkRoomId;

        public PeerMessage(String account, int operate, int coindex) {
            this.account = account;
            this.operate = operate;
            this.coindex = coindex;
        }

        public PeerMessage(String account, int operate, String pkRoomId) {
            this.account = account;
            this.operate = operate;
            this.pkRoomId = pkRoomId;
        }
    }
}
