package io.agora.vlive.utils;

import android.graphics.Color;

import io.agora.vlive.R;
import io.agora.vlive.proxy.model.RoomInfo;

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

        public static final String KEY_ROOM_NAME = "room-name";
        public static final String KEY_IS_ROOM_OWNER = "is-room-owner";
        public static final String KEY_ROOM_OWNER_ID = "room-owner-id";

        public static final int DIVIDER_COLOR = Color.rgb(239, 239, 239);

        public static final int VIDEO_MAX_BITRATE = 4000;
        public static final int VIDEO_MIN_BITRATE = 150;
        public static final int VIDEO_DEFAULT_BITRATE = 800;
        public static final int VIDEO_DEFAULT_RESOLUTION_INDEX = 0;
        public static final int VIDEO_DEFAULT_FRAME_RATE_INDEX = 0;

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
    }

    public static class FakeData {
        public static final String[][] BG_MUSIC = {
            {"像梦一样自由", "汪峰"},
            {"我就知道那是爱", "梁静茹"},
            {"I Believe I Can Fly", "R.Kelly"},
            {"What a wonderful world", "Louis Armstrong"},
            {"长安长安", "郑钧"},
            {"同桌的你", "老狼"},
            {"天下没有不散的宴席", "郑钧"},
            {"作曲家", "李荣浩"},
        };

        public static final int[] GIFT_VALUES = {
                20, 30, 40, 50, 60, 70, 80, 90
        };

        public static final RoomInfo[] ROOM_LIST = {
                new RoomInfo("1000", "test room0", null, 10, 1000),
                new RoomInfo("1001", "test room1", null, 10, 1001),
                new RoomInfo("1002", "test room2", null, 10, 1002),
                new RoomInfo("1003", "test room3", null, 10, 1003),
                new RoomInfo("1004", "test room4", null, 10, 1004),
                new RoomInfo("1005", "test room5", null, 10, 1005),
                new RoomInfo("1006", "test room6", null, 10, 1006),
                new RoomInfo("1007", "test room7", null, 10, 1007),
                new RoomInfo("1008", "test room8", null, 10, 1008),
                new RoomInfo("1009", "test room9", null, 10, 1009),
        };
    }
}
