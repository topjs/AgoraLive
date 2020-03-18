package io.agora.vlive.agora.rtm.model;

public class GiftMessageData {
     int cmd;
     GiftMessage data;

     public GiftMessageData(int cmd, String fromUid, String toUid,
                            String giftId, int operate) {
         this.cmd = cmd;
         data = new GiftMessage(fromUid, toUid, giftId, operate);
     }

    class GiftMessage {
         String fromUid;
         String toUid;
         String giftId;
         int operate;

        GiftMessage(String fromUid, String toUid, String giftId, int operate) {
            this.fromUid = fromUid;
            this.toUid = toUid;
            this.giftId = giftId;
            this.operate = operate;
        }
    }
}
