package io.agora.vlive.agora.rtm;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmChannelMember;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.SendMessageOptions;
import io.agora.vlive.agora.rtm.model.ChatMessageData;
import io.agora.vlive.agora.rtm.model.GiftMessageData;
import io.agora.vlive.agora.rtm.model.NotificationMessage;
import io.agora.vlive.agora.rtm.model.PeerMessageData;
import io.agora.vlive.agora.rtm.model.RoomMessageData;

public class RtmMessageManager implements RtmClientListener, RtmChannelListener {
    private static final int PEER_MSG_TYPE_CALL = 1;
    private static final int PEER_MSG_TYPE_PK = 2;

    private static final int CHANNEL_MSG_TYPE_CHAT = 1;
    private static final int CHANNEL_MSG_TYPE_NOTIFY = 2;
    private static final int CHANNEL_MSG_TYPE_ROOM = 3;
    private static final int CHANNEL_MSG_TYPE_GIFT = 4;

    private static final int PEER_MSG_CMD_APPLY = 101;
    private static final int PEER_MSG_CMD_INVITE = 102;
    private static final int PEER_MSG_CMD_APPLY_REJECT = 103;
    private static final int PEER_MSG_CMD_INVITE_REJECT = 104;
    private static final int PEER_MSG_CMD_APPLY_ACCEPTED = 105;
    private static final int PEER_MSG_CMD_INVITE_ACCEPTED = 106;

    private static final int PEER_MSG_CMD_PK = 201;
    private static final int PEER_MSG_CMD_PK_REJECT = 202;
    private static final int PEER_MSG_CMD_PK_ACCEPT = 203;

    private static final int CHANNEL_MSG_CANCEL = 101;
    private static final int CHANNEL_MSG_ACCEPT = 102;
    private static final int CHANNEL_MSG_CLOSE = 103;
    private static final int CHANNEL_MSG_OPEN = 104;
    private static final int CHANNEL_MSG_MUTE = 105;
    private static final int CHANNEL_MSG_UNMUTE = 106;

    private static final int CHANNEL_MSG_MUTE_AUDIO = 201;
    private static final int CHANNEL_MSG_UNMUTE_AUDIO = 202;
    private static final int CHANNEL_MSG_MUTE_VIDEO = 203;
    private static final int CHANNEL_MSG_UNMUTE_VIDEO = 204;

    private static final int CHANNEL_START_PK = 401;
    private static final int CHANNEL_END_PK = 402;

    private static final int CHANNEL_SEND_GIFT = 501;

    private volatile static RtmMessageManager sInstance;

    private RtmClient mRtmClient;
    private RtmChannel mRtmChannel;
    private SendMessageOptions mOptions;
    private List<RtmMessageListener> mMessageListeners;
    private Handler mHandler;

    private RtmMessageManager() {
        mOptions = new SendMessageOptions();
        mOptions.enableOfflineMessaging = false;
        mOptions.enableHistoricalMessaging = false;
        mMessageListeners = new ArrayList<>();
    }

    public static RtmMessageManager instance() {
        if (sInstance == null) {
            synchronized (RtmMessageManager.class) {
                if (sInstance == null) {
                    sInstance = new RtmMessageManager();
                }
            }
        }
        return sInstance;
    }

    public void init(RtmClient client) {
        mRtmClient = client;
    }

    public synchronized void joinChannel(String channel, ResultCallback<Void> callback) {
        if (mRtmChannel != null || mRtmClient == null) {
            return;
        }

        mRtmChannel = mRtmClient.createChannel(channel, this);
        mRtmChannel.join(callback);
    }

    public synchronized void leaveChannel(ResultCallback<Void> callback) {
        if (mRtmChannel == null) return;
        mRtmChannel.leave(callback);
        mRtmChannel.release();
        mRtmChannel = null;
    }

    private void sendPeerMessage(String userId, String message, ResultCallback<Void> callback) {
        if (mRtmClient == null) return;
        RtmMessage msg = mRtmClient.createMessage(message);
        mRtmClient.sendMessageToPeer(userId, msg, mOptions, callback);
    }

    private void sendChannelMessage(String message, ResultCallback<Void> callback) {
        if (mRtmChannel == null) return;
        RtmMessage msg = mRtmClient.createMessage(message);
        mRtmChannel.sendMessage(msg, mOptions, callback);
    }

    public void apply(String userId, String nickname, int coindex, ResultCallback<Void> callback) {
        String json = getPeerMessageDataJson(PEER_MSG_TYPE_CALL, nickname, PEER_MSG_CMD_APPLY, coindex);
        sendPeerMessage(userId, json, callback);
    }

    public void invite(String userId, String nickname, int coindex, ResultCallback<Void> callback) {
        String json = getPeerMessageDataJson(PEER_MSG_TYPE_CALL, nickname, PEER_MSG_CMD_INVITE, coindex);
        sendPeerMessage(userId, json, callback);
    }

    public void acceptApplication(String userId, String nickname, int coindex, ResultCallback<Void> callback) {
        String json = getPeerMessageDataJson(PEER_MSG_TYPE_CALL, nickname, PEER_MSG_CMD_APPLY_ACCEPTED);
        sendPeerMessage(userId, json, callback);
    }

    public void acceptInvitation(String userId, String nickname, int coindex, ResultCallback<Void> callback) {
        String json = getPeerMessageDataJson(PEER_MSG_TYPE_CALL, nickname, PEER_MSG_CMD_INVITE_ACCEPTED);
        sendPeerMessage(userId, json, callback);
    }

    public void rejectApplication(String userId, String nickname, ResultCallback<Void> callback) {
        String json = getPeerMessageDataJson(PEER_MSG_TYPE_CALL, nickname, PEER_MSG_CMD_APPLY_REJECT);
        sendPeerMessage(userId, json, callback);
    }

    public void rejectInvitation(String userId, String nickname, ResultCallback<Void> callback) {
        String json = getPeerMessageDataJson(PEER_MSG_TYPE_CALL, nickname, PEER_MSG_CMD_INVITE_REJECT);
        sendPeerMessage(userId, json, callback);
    }

    public void applyPk(String userId, String nickname, ResultCallback<Void> callback) {
        String json = getPeerMessageDataJson(PEER_MSG_TYPE_PK, nickname, PEER_MSG_CMD_PK);
        sendPeerMessage(userId, json, callback);
    }

    public void acceptPk(String userId, String nickname, ResultCallback<Void> callback) {
        String json = getPeerMessageDataJson(PEER_MSG_TYPE_PK, nickname, PEER_MSG_CMD_PK_ACCEPT);
        sendPeerMessage(userId, json, callback);
    }

    public void rejectPk(String userId, String nickname, ResultCallback<Void> callback) {
        String json = getPeerMessageDataJson(PEER_MSG_TYPE_PK, nickname, PEER_MSG_CMD_PK_REJECT);
        sendPeerMessage(userId, json, callback);
    }

    private String getPeerMessageDataJson(int cmd, String nickname, int operate, int coindex) {
        PeerMessageData data = new PeerMessageData(cmd, nickname, operate, coindex);
        return new GsonBuilder().create().toJson(data);
    }

    private String getPeerMessageDataJson(int cmd, String nickname, int operate) {
        PeerMessageData data = new PeerMessageData(cmd, nickname, operate, 0);
        return new GsonBuilder().create().toJson(data);
    }

    public void sendChatMessage(String nickname, String content, ResultCallback<Void> callback) {
        String json = getChatMessageJsonString(nickname, content);
        sendChannelMessage(json, callback);
    }

    private String getChatMessageJsonString(String nickname, String content) {
        ChatMessageData data = new ChatMessageData(CHANNEL_MSG_TYPE_CHAT, nickname, content);
        return new GsonBuilder().create().toJson(data);
    }

    public void sendNotificationMessage(String userId, int index, int operate, ResultCallback<Void> callback) {
        String json = getNotificationString(userId, index, operate);
        sendChannelMessage(json, callback);
    }

    private String getNotificationString(String userId, int index, int operate) {
        NotificationMessage message = new NotificationMessage(
                CHANNEL_MSG_TYPE_NOTIFY, userId, index, operate);
        return new GsonBuilder().create().toJson(message);
    }

    public void sendRoomMessage(int operate, ResultCallback<Void> callback) {
        String json = getRoomMessageString(operate);
        sendChannelMessage(json, callback);
    }

    private String getRoomMessageString(int operate) {
        RoomMessageData data = new RoomMessageData(CHANNEL_MSG_TYPE_ROOM, operate);
        return new GsonBuilder().create().toJson(data);
    }

    public void sendGiftMessage(String fromUid, String toUid, String giftId, ResultCallback<Void> callback) {
        String json = getGiftMessageString(fromUid, toUid, giftId);
        sendChannelMessage(json, callback);
    }

    private String getGiftMessageString(String fromUid, String toUid, String giftId) {
        GiftMessageData data = new GiftMessageData(CHANNEL_MSG_TYPE_GIFT,
                fromUid, toUid, giftId, CHANNEL_SEND_GIFT);
        return new GsonBuilder().create().toJson(data);
    }

    public void registerMessageHandler(RtmMessageListener handler) {
        mMessageListeners.add(handler);
    }

    public void removeMessageHandler(RtmMessageListener handler) {
        mMessageListeners.remove(handler);
    }

    public void setCallbackThread(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {
        for (RtmMessageListener handler : mMessageListeners) {
            handler.onRtmConnectionStateChanged(state, reason);
        }
    }

    @Override
    public void onMessageReceived(RtmMessage rtmMessage, final String peerId) {
        // Where peer to peer messages are received.
        PeerMessageData message = new GsonBuilder().create().
                fromJson(rtmMessage.getText(), PeerMessageData.class);

        if (mHandler != null) {
            mHandler.post(() -> handlePeerMessages(message, peerId, message.data.account, message.data.coindex));
        } else {
            handlePeerMessages(message, peerId, message.data.account, message.data.coindex);
        }
    }

    private void handlePeerMessages(PeerMessageData message, String peerId, String nickname, int index) {
        for (RtmMessageListener listener : mMessageListeners) {
            switch (message.data.operate) {
                case PEER_MSG_CMD_APPLY:
                    listener.onRtmAppliedForSeat(peerId, nickname, index);
                    break;
                case PEER_MSG_CMD_INVITE:
                    listener.onRtmInvitedByOwner(peerId, nickname, index);
                    break;
                case PEER_MSG_CMD_APPLY_REJECT:
                    listener.onRtmApplicationRejected(peerId, nickname);
                    break;
                case PEER_MSG_CMD_INVITE_REJECT:
                    listener.onRtmInvitationRejected(peerId, nickname);
                    break;
                case PEER_MSG_CMD_PK:
                    listener.onRtmPkReceivedFromAnotherHost(peerId, nickname);
                    break;
                case PEER_MSG_CMD_PK_ACCEPT:
                    listener.onRtmPkAcceptedByTargetHost(peerId, nickname);
                    break;
                case PEER_MSG_CMD_PK_REJECT:
                    listener.onRtmPkRejectedByTargetHost(peerId, nickname);
                    break;
            }
        }
    }

    @Override
    public void onTokenExpired() {
        for (RtmMessageListener listener : mMessageListeners) {
            listener.onRtmTokenExpired();
        }
    }

    @Override
    public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

    }

    @Override
    public void onMemberCountUpdated(int memberCount) {

    }

    @Override
    public void onAttributesUpdated(List<RtmChannelAttribute> attributeList) {
        for (RtmMessageListener listener : mMessageListeners) {
            listener.onRtmAttributesUpdated(attributeList);
        }
    }

    @Override
    public void onMessageReceived(RtmMessage rtmMessage, RtmChannelMember fromMember) {
        // Where channel messages are received
        boolean error = false;
        String peerId = fromMember.getChannelId();
        String json = rtmMessage.getText();
        JSONObject messageObj;
        int cmd = -1;
        try {
            JSONObject obj = new JSONObject(json);
            cmd = obj.getInt("cmd");
            messageObj = obj.getJSONObject("data");

            for (final RtmMessageListener listener : mMessageListeners) {
                switch (cmd) {
                    case CHANNEL_MSG_TYPE_CHAT:
                        String account = messageObj.getString("account");
                        String content = messageObj.getString("content");
                        handleChatMessages(listener, peerId, account, content);
                        break;
                    case CHANNEL_MSG_TYPE_NOTIFY:
                        String userId = messageObj.getString("uid");
                        int index = messageObj.getInt("coindex");
                        int operate = messageObj.getInt("operate");
                        handleNotifyMessages(listener, userId, index, operate);
                        break;
                    case CHANNEL_MSG_TYPE_ROOM:
                        operate = messageObj.getInt("operate");
                        handleRoomMessage(listener, operate);
                        break;
                    case CHANNEL_MSG_TYPE_GIFT:
                        String fromUid = messageObj.getString("fromUid");
                        String toUid = messageObj.getString("toUid");
                        String giftId = messageObj.getString("giftId");
                        handleGiftMessage(listener, fromUid, toUid, giftId);
                        break;
                }
            }
        } catch (JSONException e) {
            error = true;
            e.printStackTrace();
        }
    }

    private void handleChatMessages(@NonNull RtmMessageListener listener, String userId, String account, String content) {
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmChannelMessageReceived(userId, account, content));
        } else {
            listener.onRtmChannelMessageReceived(userId, account, content);
        }
    }

    private void handleNotifyMessages(@NonNull RtmMessageListener listener, String userId, int index, int operate) {
        switch (operate) {
            case CHANNEL_MSG_CANCEL:
            case CHANNEL_MSG_ACCEPT:
            case CHANNEL_MSG_CLOSE:
            case CHANNEL_MSG_OPEN:
            case CHANNEL_MSG_MUTE:
            case CHANNEL_MSG_UNMUTE:
            case CHANNEL_MSG_MUTE_AUDIO:
            case CHANNEL_MSG_UNMUTE_AUDIO:
            case CHANNEL_MSG_MUTE_VIDEO:
            case CHANNEL_MSG_UNMUTE_VIDEO:
                if (mHandler != null) {
                    mHandler.post(() -> listener.onRtmHostStateChanged(userId, index, operate));
                } else {
                    listener.onRtmHostStateChanged(userId, index, operate);
                }
                break;
        }
    }

    private void handleRoomMessage(@NonNull RtmMessageListener listener, int operate) {
        if (mHandler != null) {
            mHandler.post(() -> HandleRoomPKMessage(listener, operate));
        } else {
            HandleRoomPKMessage(listener, operate);
        }
    }

    private void HandleRoomPKMessage(@NonNull RtmMessageListener listener, int operate) {
        if (operate == CHANNEL_START_PK) {
            listener.onRtmPkStartStateReceived();
        } else if (operate == CHANNEL_END_PK) {
            listener.onRtmPkEndStateReceived();
        }
    }

    private void handleGiftMessage(@NonNull RtmMessageListener listener, String fromUid, String toUid, String giftId) {
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmGiftMessageReceived(fromUid, toUid, giftId));
        } else {
            listener.onRtmGiftMessageReceived(fromUid, toUid, giftId);
        }
    }

    @Override
    public void onMemberJoined(RtmChannelMember rtmChannelMember) {

    }

    @Override
    public void onMemberLeft(RtmChannelMember rtmChannelMember) {

    }
}
