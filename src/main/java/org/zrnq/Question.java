package org.zrnq;

import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable {
    public Questioner questioner;
    public final long time=System.currentTimeMillis();
    public String title=null;
    public String text=null;
    public long groupId;
    public final ArrayList<SerializableImage> images=new ArrayList<>();
    public final ArrayList<Answer> answererList=new ArrayList<>();
    public int questionId;
    public boolean requireFurtherInfo=false;
    public boolean haveAccepted(){
        if(answererList.size()<=0)return false;
        for (Answer answerer : answererList) {
            if (answerer.accepted) return true;
        }
        return false;
    }
    public String getStatusBarText(){
        return "提问者: "+questioner.name
                +"  时间: "+ Util.parseTime(time)
                +"  回答数: "+answererList.size()
                +"  状态: "+(haveAccepted()?"解决":
                answererList.size()<=0?"等待":
                requireFurtherInfo?"追问":"未读");
    }
}
