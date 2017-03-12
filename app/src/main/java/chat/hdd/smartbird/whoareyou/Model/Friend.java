package chat.hdd.smartbird.whoareyou.Model;

/**
 * Created by hiepdd on 11/03/2017.
 */

public class Friend {
    private String idFriend;
    private String emailFriend;
    private String nameFriend;
    private boolean isChat;
    private boolean isRequest;
    private boolean isFriend;

    public Friend() {
        this.isChat =  true;
    }

    public Friend(String idFriend, String emailFriend, String nameFriend, boolean isRequest, boolean isFriend) {
        this.idFriend = idFriend;
        this.emailFriend = emailFriend;
        this.nameFriend = nameFriend;
        this.isRequest = isRequest;
        this.isFriend = isFriend;
        this.isChat = true;
    }



    public boolean isRequest() {
        return isRequest;
    }

    public void setRequest(boolean request) {
        isRequest = request;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getIdFriend() {
        return idFriend;
    }

    public void setIdFriend(String idFriend) {
        this.idFriend = idFriend;
    }

    public String getEmailFriend() {
        return emailFriend;
    }

    public void setEmailFriend(String emailFriend) {
        this.emailFriend = emailFriend;
    }

    public String getNameFriend() {
        return nameFriend;
    }

    public void setNameFriend(String nameFriend) {
        this.nameFriend = nameFriend;
    }

    public boolean isChat() {
        return isChat;
    }

    public void setChat(boolean chat) {
        isChat = chat;
    }
}
