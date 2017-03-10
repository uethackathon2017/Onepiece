package chat.hdd.smartbird.whoareyou.Model;

import java.util.Date;

/**
 * Created by hiepdd on 05/03/2017.
 */

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private String messageImage;
    private long messageTime;

    public ChatMessage(String messageText, String messageUser, String messageImage) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageImage = messageImage;
        messageTime = new Date().getTime();
    }


    public ChatMessage() {
        messageTime = new Date().getTime();
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageImage() {
        return messageImage;
    }

    public void setMessageImage(String messageImage) {
        this.messageImage = messageImage;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}

