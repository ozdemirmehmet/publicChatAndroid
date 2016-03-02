package com.likapalab.mehmet.publicchat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mehmet on 26.09.2015.
 */
public class Message {
    private static List<String> reportUsers = new ArrayList<String>();
    private static List<Message> messages = new ArrayList<Message>();
    private String id;
    private String message;
    private String username;
    private String time;
    private int userColor;
    private boolean isMe; //if isMe true else false

    public Message(String id, String username, String message,String time,int userColor,boolean isMe){
        this.id = id;
        this.message = message;
        this.username = username;
        this.time = time;
        this.userColor = userColor;
        this.isMe = isMe;
    }

    public static void addUser(String id){ reportUsers.add(id); }

    public static boolean isReport(String id){
        if(reportUsers.size() > 0){
            for(int i=0;i<reportUsers.size();i++){
                if(id.equals(reportUsers.get(i))){//Kullanıcı daha önce şikayet edilmiş
                    return true;
                }
            }
            return false;
        }
        else
            return false;
    }

    public static void clearUserList(){ reportUsers.clear(); }

    public static List<Message> getMessages() {
        return messages;
    }

    public static void clearMessageList(){ messages.clear(); }

    public static void addList(Message message){messages.add(message);}

    public String getId(){ return id; }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getTime(){
        return time;
    }

    public int getUserColor() {return userColor;}

    public boolean getIsMe() {return  isMe;}
}
