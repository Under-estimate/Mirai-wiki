package com.zjs;

import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable {
    public Questioner questioner;
    public long time=System.currentTimeMillis();
    public String title=null;
    public String text=null;
    public ArrayList<SerializableImage> images=new ArrayList<>();
    public ArrayList<Answerer> answererList=new ArrayList<>();
    public long questionId;
    public boolean requireFurtherInfo=false;
    public boolean haveAccepted(){
        if(answererList.size()<=0)return false;
        for (Answerer answerer : answererList) {
            if (answerer.accepted) return true;
        }
        return false;
    }
    @Override
    public String toString(){
        return "[问题]"+title+"\r\n" +
                "提问者:"+questioner.toString()+"\r\n" +
                "提问时间:"+Util.parseTime(time)+"\r\n" +
                "回答数:"+answererList.size()+"\r\n"+
                "状态:"+(haveAccepted()?"解决":
                answererList.size()<=0?"等待":
                requireFurtherInfo?"追问":"未读(未读状态不会解除直至提问者标记问题为\"解决\"或\"追问\")")+"\r\n\r\n" +
                text+"\r\n\r\n回复Wiki:Page + <序号>翻页查看回答";
    }
}
