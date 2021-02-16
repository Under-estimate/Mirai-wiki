package org.zrnq;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
/**
 * 一个Session代表与某个群成员的会话，不同群、不同群成员的会话是互相隔离的.
 * */
public class Session {
    Group group;
    Member member;
    State state=State.Null;
    ArrayList<Question> queryData=null;
    Question currentQuestion=null;
    String text=null;
    Answer currentAnswer=null;
    boolean viewDetail = false;
    public Session(@NotNull GroupMessageEvent event){
        this.group=event.getGroup();
        this.member=event.getSender();
    }
    /**
     * 当前会话的状态.
     * */
    public enum State{
        Search_Question,
        Write_Question,
        My_Questions,
        My_Answers,
        View_Unsolved,
        View_All,
        Write_Answer,
        Null
    }
}
