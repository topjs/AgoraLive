package io.agora.vlive.proxy.struts.request;

public class Request {
    // General purposes
    public static final int APP_VERSION = 1;
    public static final int OSS = 2;
    public static final int GIFT_LIST = 3;
    public static final int MUSIC_LIST = 4;

    // User management
    public static final int CREATE_USER = 5;
    public static final int EDIT_USER = 6;
    public static final int USER_LOGIN = 7;

    // Live Room
    public static final int CREATE_ROOM = 8;
    public static final int ROOM_LIST = 9;
    public static final int ENTER_ROOM = 10;
    public static final int LEAVE_ROOM = 11;
    public static final int AUDIENCE_LIST = 12;
    public static final int SEND_GIFT = 13;
    public static final int GIFT_RANK = 14;
    public static final int REFRESH_TOKEN = 15;
    public static final int MODIFY_SEAT_STATE = 16;
    public static final int SEAT_STATE = 17;
    public static final int PK_START_STOP = 18;
}