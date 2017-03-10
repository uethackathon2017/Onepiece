package chat.hdd.smartbird.whoareyou.Model;

/**
 * Created by hiepdd on 06/03/2017.
 */

public class Account {
    private String id;
    private String email;
    private boolean isChat;

    public Account() {
        this.isChat = false;
    }

    public Account(String id, String email) {
        this.id = id;
        this.email = email;
        this.isChat = false;
    }

    public boolean isChat() {
        return isChat;
    }

    public void setChat(boolean chat) {
        isChat = chat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
