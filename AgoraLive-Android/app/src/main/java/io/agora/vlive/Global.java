package io.agora.vlive;

public class Global {
    public static class Constants {
        public static final String TAB_KEY = "live-tab";
        public static final int TAB_ID_MULTI = 0;
        public static final int TAB_ID_SINGLE = 1;
        public static final int TAB_ID_PK = 2;

        public static final String KEY_ROOM_NAME = "room-name";
        public static final String KEY_IS_HOST = "is-host";

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
}
