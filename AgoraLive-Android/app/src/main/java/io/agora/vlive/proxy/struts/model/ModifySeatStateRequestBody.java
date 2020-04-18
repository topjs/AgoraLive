package io.agora.vlive.proxy.struts.model;

public class ModifySeatStateRequestBody {
    private String userId;
    private int state;
    private int no;

    public ModifySeatStateRequestBody(int no, String userId, int state) {
        this.no = no;
        this.userId = userId;
        this.state = state;
    }
}
