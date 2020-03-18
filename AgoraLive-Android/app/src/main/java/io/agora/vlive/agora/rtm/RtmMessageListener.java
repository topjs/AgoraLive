package io.agora.vlive.agora.rtm;

import java.util.List;
import java.util.Map;

import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelMember;

public interface RtmMessageListener {
    void onRtmConnectionStateChanged(int state, int reason);

    void onRtmTokenExpired();

    void onRtmPeersOnlineStatusChanged(Map<String, Integer> map);

    void onRtmMemberCountUpdated(int memberCount);

    void onRtmAttributesUpdated(List<RtmChannelAttribute> attributeList);

    void onRtmMemberJoined(RtmChannelMember rtmChannelMember);

    void onRtmMemberLeft(RtmChannelMember rtmChannelMember);

    void onRtmInvitedByOwner(String ownerId, String nickname);

    void onRtmAppliedForSeat(String ownerId, String nickname);

    void onRtmInvitationRejected(String peerId, String nickname);

    void onRtmApplicationRejected(String peerId, String nickname);

    void onRtmPkReceivedFromAnotherHost(String peerId, String nickname);

    void onRtmPkAcceptedByTargetHost(String peerId, String nickname);

    void onRtmPkRejectedByTargetHost(String peerId, String nickname);

    void onRtmChannelMessageReceived(String peerId, String nickname, String content);

    void onRtmHostStateChanged(String uid, int index, int operate);

    void onRtmPkStartStateReceived();

    void onRtmPkEndStateReceived();

    void onRtmGiftMessageReceived(String fromUid, String toUid, String giftId);
}
