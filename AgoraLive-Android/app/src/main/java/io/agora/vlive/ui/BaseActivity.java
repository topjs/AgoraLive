package io.agora.vlive.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Stack;

import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.ui.actionsheets.AbstractActionSheet;
import io.agora.vlive.ui.actionsheets.GiftActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomToolActionSheet;
import io.agora.vlive.ui.actionsheets.VoiceActionSheet;
import io.agora.vlive.utils.Global;
import io.agora.vlive.R;
import io.agora.vlive.ui.actionsheets.BackgroundMusicActionSheet;
import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomSettingActionSheet;

/**
 * Capabilities that are shared by all activity, such as
 * messaging, action sheets, dialogs.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected static final int ACTION_SHEET_VIDEO = 0;
    protected static final int ACTION_SHEET_BEAUTY = 1;
    protected static final int ACTION_SHEET_BG_MUSIC = 2;
    protected static final int ACTION_SHEET_GIFT = 3;
    protected static final int ACTION_SHEET_TOOL = 4;
    protected static final int ACTION_SHEET_VOICE = 5;

    private static final int ACTION_SHEET_DIALOG_STYLE_RES = R.style.live_room_dialog;
    private static final int TOAST_SHORT_INTERVAL = 2000;

    protected int systemBarHeight;
    protected int displayHeight;

    private Stack<AbstractActionSheet> mActionSheetStack;
    private BottomSheetDialog mSheetDialog;
    private long mLastToastTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setGlobalLayoutListener();
        mActionSheetStack = new Stack<>();
        systemBarHeight = getStatusBarHeight();
        displayHeight = getDisplayHeight();
    }

    private void setGlobalLayoutListener() {
        final View layout = findViewById(Window.ID_ANDROID_CONTENT);
        ViewTreeObserver observer = layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                onGlobalLayoutCompleted();
            }
        });
    }

    /**
     * Give a chance to obtain view layout attributes when the
     * content view layout process is completed.
     * Some layout attributes will be available here but not
     * in onCreate(), like measured width/height.
     * This callback will be called ONLY ONCE before the whole
     * window content is ready to be displayed for first time.
     */
    protected void onGlobalLayoutCompleted() {

    }

    protected void hideStatusBar(Window window, boolean darkText) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && darkText) {
            flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        window.getDecorView().setSystemUiVisibility(flag |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    protected void hideStatusBar(boolean darkText) {
        hideStatusBar(getWindow(), darkText);
    }

    private int getStatusBarHeight() {
        int id = getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : id;
    }

    private int getDisplayHeight() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    protected void showActionSheetDialog(final AbstractActionSheet sheet) {
        dismissActionSheetDialog();

        mSheetDialog = new BottomSheetDialog(this, ACTION_SHEET_DIALOG_STYLE_RES);
        mSheetDialog.setCanceledOnTouchOutside(true);
        mSheetDialog.setContentView(sheet);
        hideStatusBar(mSheetDialog.getWindow(), false);

        mSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mActionSheetStack.isEmpty()) {
                    // Happens only in case of errors.
                    return;
                }

                if (sheet != mActionSheetStack.peek()) {
                    // When this action sheet is not at the top of
                    // stack, it means that a new action sheet
                    // is about to be shown and it needs a fallback
                    // history, and this sheet needs to be retained.
                    return;
                }

                // At this moment, we want to fallback to
                // the previous action sheet if exists.
                mActionSheetStack.pop();
                if (!mActionSheetStack.isEmpty()) {
                    AbstractActionSheet sheet = mActionSheetStack.peek();
                    ((ViewGroup) sheet.getParent()).removeAllViews();
                    showActionSheetDialog(mActionSheetStack.peek());
                }
            }
        });

        mSheetDialog.show();
    }

    protected void showActionSheetDialog(int type, boolean isHost, boolean newStack,
                                         AbstractActionSheet.AbsActionSheetListener listener) {
        AbstractActionSheet actionSheet;
        switch (type) {
            case ACTION_SHEET_BEAUTY:
                actionSheet = new BeautySettingActionSheet(this);
                break;
            case ACTION_SHEET_BG_MUSIC:
                actionSheet = new BackgroundMusicActionSheet(this);
                break;
            case ACTION_SHEET_GIFT:
                actionSheet = new GiftActionSheet(this);
                break;
            case ACTION_SHEET_TOOL:
                actionSheet = new LiveRoomToolActionSheet(this);
                ((LiveRoomToolActionSheet) actionSheet).setHost(isHost);
                break;
            case ACTION_SHEET_VOICE:
                actionSheet = new VoiceActionSheet(this);
                break;
            default:
                actionSheet = new LiveRoomSettingActionSheet(this);
                ((LiveRoomSettingActionSheet) actionSheet).setFallback(!newStack);
        }

        actionSheet.setActionSheetListener(listener);
        if (newStack) mActionSheetStack.clear();
        mActionSheetStack.push(actionSheet);
        Log.i(TAG, "action sheet stack size:" + mActionSheetStack.size());
        showActionSheetDialog(actionSheet);
    }

    protected void dismissActionSheetDialog() {
        if (mSheetDialog != null && mSheetDialog.isShowing()) {
            mSheetDialog.dismiss();
        }
    }

    protected Dialog showDialog(int title, int message,
                              final View.OnClickListener positiveClickListener) {
        final Dialog dialog = new Dialog(this,
                R.style.live_room_dialog_center_in_window);
        dialog.setContentView(R.layout.live_room_dialog);
        AppCompatTextView titleTextView = dialog.findViewById(R.id.dialog_title);
        titleTextView.setText(title);
        AppCompatTextView msgTextView = dialog.findViewById(R.id.dialog_message);
        msgTextView.setText(message);
        dialog.findViewById(R.id.dialog_negative_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
        dialog.findViewById(R.id.dialog_positive_button)
                .setOnClickListener(positiveClickListener);
        hideStatusBar(dialog.getWindow(), false);
        dialog.show();
        return dialog;
    }

    protected AgoraLiveApplication application() {
        return (AgoraLiveApplication)  getApplication();
    }

    protected Global global() {
        return application().global();
    }

    protected SharedPreferences preferences() {
        return application().preferences();
    }

    protected void showShortToast(String message) {
        long current = System.currentTimeMillis();
        if (current - mLastToastTime > TOAST_SHORT_INTERVAL) {
            // avoid showing the toast too frequently
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            mLastToastTime = current;
        }
    }
}
