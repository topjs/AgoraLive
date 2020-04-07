package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.agora.vlive.R;
import io.agora.vlive.proxy.struts.response.AudienceListResponse.AudienceInfo;


public class InviteUserActionSheet extends AbstractActionSheet {
    public interface InviteUserActionSheetListener extends AbsActionSheetListener {
        void onActionSheetAudienceInvited(AudienceInfo user);
    }

    private InviteUserActionSheetListener mListener;
    private RoomUserAdapter mAdapter;

    public InviteUserActionSheet(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(
                R.layout.action_room_host_in_invite_user_list, this, true);
        RecyclerView recyclerView = findViewById(R.id.live_room_action_sheet_host_in_invite_user_list_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        mAdapter = new RoomUserAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof InviteUserActionSheetListener) {
            mListener = (InviteUserActionSheetListener) listener;
        }
    }

    public void append(List<AudienceInfo> userList) {
        mAdapter.append(userList);
    }

    private class RoomUserAdapter extends RecyclerView.Adapter {
        private List<AudienceInfo> mUserList = new ArrayList<>();

        public void append(List<AudienceInfo> userList) {
            mUserList.addAll(userList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RoomUserViewHolder(LayoutInflater.
                    from(getContext()).inflate(R.layout.action_invite_audience_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            RoomUserViewHolder viewHolder = (RoomUserViewHolder) holder;
            viewHolder.button.setOnClickListener(view -> {
                if (mListener != null) mListener.onActionSheetAudienceInvited(mUserList.get(position));
            });
        }

        @Override
        public int getItemCount() {
            return mUserList == null || mUserList.isEmpty() ? 0 : mUserList.size();
        }
    }

    private class RoomUserViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView name;
        AppCompatTextView button;

        RoomUserViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.live_room_action_sheet_user_list_item_icon);
            name = itemView.findViewById(R.id.live_room_action_sheet_user_list_item_name);
            button = itemView.findViewById(R.id.live_room_action_sheet_user_list_item_invite_btn);
        }
    }
}