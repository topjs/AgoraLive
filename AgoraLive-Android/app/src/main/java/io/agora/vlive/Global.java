package io.agora.vlive;

import android.graphics.Color;

public class Global {
    public static class Constants {
        public static final String TAB_KEY = "live-tab";
        public static final int TAB_ID_MULTI = 0;
        public static final int TAB_ID_SINGLE = 1;
        public static final int TAB_ID_PK = 2;

        public static final String KEY_ROOM_NAME = "room-name";
        public static final String KEY_IS_HOST = "is-host";

        public static final int DIVIDER_COLOR = Color.rgb(239, 239, 239);

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
    }

    public static class States {
        public static int lastTabPosition = Constants.TAB_ID_MULTI;
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
    }
}
