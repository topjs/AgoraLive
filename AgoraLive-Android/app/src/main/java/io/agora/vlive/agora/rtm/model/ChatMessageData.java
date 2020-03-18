package io.agora.vlive.agora.rtm.model;

public class ChatMessageData {
    public int cmd;
    public ChatMessage data;

    public ChatMessageData(int cmd, String nickname, String content) {
        this.cmd = cmd;
        data = new ChatMessage(nickname, content);
    }

    public static class ChatMessage {
        String account;
        String content;

        ChatMessage(String account, String content) {
            this.account = account;
            this.content = content;
        }
    }
}
