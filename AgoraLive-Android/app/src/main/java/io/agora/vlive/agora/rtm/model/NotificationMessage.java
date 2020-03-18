package io.agora.vlive.agora.rtm.model;

public class NotificationMessage {
    int cmd;
    public Notification data;

    public NotificationMessage(int cmd, String uid, int index, int operate) {
        this.cmd = cmd;
        data = new Notification(uid, index, operate);
    }

    class Notification {
        String uid;
        int index;
        int operate;

        Notification(String uid, int index, int operate) {
            this.uid = uid;
            this.index = index;
            this.operate = operate;
        }
    }
}
