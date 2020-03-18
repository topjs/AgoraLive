package io.agora.vlive.proxy.model;

public class SeatInfo {
    public static final int OPEN = 0;
    public static final int SPEAK = 1;
    public static final int MUTED = 2;
    public static final int CLOSE = 3;

    public int no;
    public String userId;
    public String userName;
    public int uid;
    public int state;
}
