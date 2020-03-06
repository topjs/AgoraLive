package io.agora.vlive.ui.main.fragments;

import androidx.fragment.app.Fragment;

import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.proxy.ClientProxyListener;
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
import io.agora.vlive.ui.main.MainActivity;

public abstract class AbstractFragment extends Fragment implements ClientProxyListener {
    protected AgoraLiveApplication application() {
        return (AgoraLiveApplication) getContext().getApplicationContext();
    }

    MainActivity getContainer() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onAppVersionResponse(AppVersionResponse response) {

    }

    @Override
    public void onRefreshTokenResponse(RefreshTokenResponse refreshTokenResponse) {

    }

    @Override
    public void onOssPolicyResponse(OssPolicyResponse response) {

    }

    @Override
    public void onMusicLisResponse(MusicListResponse response) {

    }

    @Override
    public void onGiftListResponse(GiftListResponse response) {

    }

    @Override
    public void onRoomListResponse(RoomListResponse response) {

    }

    @Override
    public void onCreateUserResponse(CreateUserResponse response) {

    }

    @Override
    public void onEditUserResponse(EditUserResponse response) {

    }

    @Override
    public void onCreateRoomResponse(CreateRoomResponse response) {

    }

    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {

    }

    @Override
    public void onLeaveRoomResponse(LeaveRoomResponse response) {

    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {

    }

    @Override
    public void onRequestSeatState(SeatStateResponse response) {

    }

    @Override
    public void onModifySeatState(ModifySeatStateResponse response) {

    }

    @Override
    public void onSendGift(SendGiftResponse response) {

    }

    @Override
    public void onGiftRank(GiftRankResponse response) {

    }

    @Override
    public void onStartStopPk(StartStopPkResponse response) {

    }
}