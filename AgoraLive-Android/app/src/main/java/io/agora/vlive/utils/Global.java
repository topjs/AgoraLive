package io.agora.vlive.utils;

import android.graphics.Color;

import io.agora.vlive.R;

public class Global {
    public static class Constants {
        // Shared reference keys
        public static final String SF_NAME = "sf-agora-live";
        public static final String KEY_BEAUTY_ENABLED = "key-beauty-enabled";
        public static final String KEY_BRIGHTNESS = "key-brightness";
        public static final String KEY_SMOOTH = "key-smooth";
        public static final String KEY_TEMPERATURE = "key-color-temperature";
        public static final String KEY_CONTRAST = "key-contrast";

        public static final String KEY_RESOLUTION = "key-resolution";
        public static final String KEY_FRAME_RATE = "key-framerate";
        public static final String KEY_BITRATE = "key-bitrate";

        public static final String TAB_KEY = "live-tab";
        public static final int TAB_ID_MULTI = 0;
        public static final int TAB_ID_SINGLE = 1;
        public static final int TAB_ID_PK = 2;

        public static final String KEY_ROOM_ID = "room-id";
        public static final String KEY_ROOM_NAME = "room-name";
        public static final String KEY_IS_ROOM_OWNER = "is-room-owner";
        public static final String KEY_ROOM_OWNER_ID = "room-owner-id";
        public static final String KEY_CREATE_ROOM = "create-room";

        public static final String KEY_PROFILE_UID = "key-profile-uid";
        public static final String KEY_USER_NAME = "key-user-name";
        public static final String KEY_IMAGE_URL = "key-image-url";
        public static final String KEY_TOKEN = "key-token";

        public static final int DIVIDER_COLOR = Color.rgb(239, 239, 239);

        public static final int VIDEO_MAX_BITRATE = 4000;
        public static final int VIDEO_MIN_BITRATE = 150;
        public static final int VIDEO_DEFAULT_BITRATE = 800;
        public static final int VIDEO_DEFAULT_RESOLUTION_INDEX = 0;
        public static final int VIDEO_DEFAULT_FRAME_RATE_INDEX = 0;

        public static final int CAMERA_CAPTURE_WIDTH = 1920;
        public static final int CAMERA_CAPTURE_HEIGHT = 1080;
        public static final int CAMERA_FRAME_RATE = 30;

        public static final int[] TAB_IDS_RES = {
                R.string.home_category_title_multi,
                R.string.home_category_title_single,
                R.string.home_category_title_pk
        };

        public static final String[] RESOLUTIONS = {
                "1920x1080",
                "1280x720",
                "960x640",
                "640x480"
        };

        public static final String[] FRAME_RATES = {
                "15", "24", "30"
        };

        public static final int[] GIFT_ICON_RES = {
                R.drawable.gift_01_bell,
                R.drawable.gift_02_icecream,
                R.drawable.gift_03_wine,
                R.drawable.gift_04_cake,
                R.drawable.gift_05_ring,
                R.drawable.gift_06_watch,
                R.drawable.gift_07_diamond,
                R.drawable.gift_08_rocket
        };

        public static final int[] PROFILE_BG_RES = {
                R.drawable.profile_image_1,
                R.drawable.profile_image_2,
                R.drawable.profile_image_3,
                R.drawable.profile_image_4,
                R.drawable.profile_image_5,
                R.drawable.profile_image_6,
                R.drawable.profile_image_7,
                R.drawable.profile_image_8,
                R.drawable.profile_image_9,
                R.drawable.profile_image_10,
                R.drawable.profile_image_11,
                R.drawable.profile_image_12
        };

        public static final String[][] FAKE_RANK = {
                { "1324389", "", "" },
                { "3532423", "", "" },
                { "234790238", "", "" },
        };
    }
}