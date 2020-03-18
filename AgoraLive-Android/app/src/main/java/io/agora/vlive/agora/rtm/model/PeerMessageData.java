package io.agora.vlive.agora.rtm.model;

public class PeerMessageData {
    public int cmd;
    public PeerMessage data;

    public PeerMessageData(int cmd, String account, int operate) {
        this.cmd = cmd;
        data = new PeerMessage(account, operate);
    }

    public class PeerMessage {
        public String account;
        public int operate;

        public PeerMessage(String account, int operate) {
            this.account = account;
            this.operate = operate;
        }
    }
}
