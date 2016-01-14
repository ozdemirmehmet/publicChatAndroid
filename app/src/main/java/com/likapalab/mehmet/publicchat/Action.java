package com.likapalab.mehmet.publicchat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mehmet on 9.12.2015.
 */
public class Action {

    public static List<Action> actions = new ArrayList<Action>();

    private String username;
    private String operation;
    private int duration;

    public Action(String username,String operation,int duration){
        this.username = username;
        this.operation = operation;
        this.duration = duration;
    }

    public static void clearActions(){actions.clear();}

    public static void addAction(Action action){ actions.add(action); }

    public static Action getAction(){
        Action tempAction = actions.get(0);
        actions.remove(0);
        return tempAction; }

    public String getUsername() {
        return username;
    }

    public String getOperation() {
        return operation;
    }

    public int getDuration() {
        return duration;
    }
}
