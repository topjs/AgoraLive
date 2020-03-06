package io.agora.vlive.proxy;

import io.agora.vlive.proxy.struts.response.AppVersionResponse;
import io.agora.vlive.proxy.struts.response.AudienceListResponse;
import io.agora.vlive.proxy.struts.response.CreateRoomResponse;
import io.agora.vlive.proxy.struts.response.CreateUserResponse;
import io.agora.vlive.proxy.struts.response.EditUserResponse;
import io.agora.vlive.proxy.struts.response.EnterRoomResponse;
import io.agora.vlive.proxy.struts.response.GiftListResponse;
import io.agora.vlive.proxy.struts.response.GiftRankResponse;
import io.agora.vlive.proxy.struts.response.LeaveRoomResponse;
import io.agora.vlive.proxy.struts.response.ModifySeatStateResponse;
import io.agora.vlive.proxy.struts.response.MusicListResponse;
import io.agora.vlive.proxy.struts.response.OssPolicyResponse;
import io.agora.vlive.proxy.struts.response.RefreshTokenResponse;
import io.agora.vlive.proxy.struts.response.RoomListResponse;
import io.agora.vlive.proxy.struts.response.SeatStateResponse;
import io.agora.vlive.proxy.struts.response.SendGiftResponse;
import io.agora.vlive.proxy.struts.response.StartStopPkResponse;

public interface ClientProxyListener {
    void onAppVersionResponse(AppVersionResponse response);

    void onRefreshTokenResponse(RefreshTokenResponse refreshTokenResponse);

    void onOssPolicyResponse(OssPolicyResponse response);

    void onMusicLisResponse(MusicListResponse response);

    void onGiftListResponse(GiftListResponse response);

    void onRoomListResponse(RoomListResponse response);

    void onCreateUserResponse(CreateUserResponse response);

    void onEditUserResponse(EditUserResponse response);

    void onCreateRoomResponse(CreateRoomResponse response);

    void onEnterRoomResponse(EnterRoomResponse response);

    void onLeaveRoomResponse(LeaveRoomResponse response);

    void onAudienceListResponse(AudienceListResponse response);

    void onRequestSeatState(SeatStateResponse response);

    void onModifySeatState(ModifySeatStateResponse response);

    void onSendGift(SendGiftResponse response);

    void onGiftRank(GiftRankResponse response);

    void onStartStopPk(StartStopPkResponse response);
}
