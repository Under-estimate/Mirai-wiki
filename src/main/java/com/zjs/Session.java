package com.zjs;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
/**
 * 一个Session代表与某个群成员的会话，不同群、不同群成员的会话是互相隔离的.
 * 会话是本插件的核心调度部分，它根据命令和自身的状态执行相关动作，并修改自身的状态。
 * */
public class Session {
    Group group;
    Member member;
    State state;
    ArrayList<Question> queryData;
    Question currentQuestion;
    String text;
    Answerer currentAnswer;
    boolean viewDetail=false;
    private Session(@NotNull GroupMessageEvent event){
        this.group=event.getGroup();
        this.member=event.getSender();
    }
    /**
     * 在给定的群消息中解析动作.
     * 一个动作需要上下文的信息,所以设置为非静态方法.
     * 在动作的执行过程中可能会修改当前会话的状态和信息,甚至删除当前会话.
     * 合理的动作包含:Page,View,Back,Answer,Title,Text,Image,Submit,Abort,Delete,Accept,Further.即需要上下文才能执行的指令.
     *
     * @return 给定的群消息是否包含一个合理的动作.
     * */
    public boolean parseAction(@NotNull GroupMessageEvent event){
        String content=event.getMessage().contentToString();
        if(content.toLowerCase().startsWith("wiki:page")){
            if(state==State.Write_Answer||state==State.Write_Question){
                Util.sendMes(event,"不可以在当前上下文中使用该指令！");
                return true;
            }
            int page;
            try{
                page=Integer.parseInt(content.split("((?i)wiki:page)",2)[1].trim());
            }catch (Exception e){
                Util.sendMes(event,"参数错误。\r\n命令用法 Wiki:Page + <页码>");
                return true;
            }
            if (viewDetail) {
                if(page<0||page>currentQuestion.answererList.size()){
                    Util.sendMes(event, "给定的页码超出范围！页码范围为{x|x∈N,x≤" + currentQuestion.answererList.size() + "}");
                    return true;
                }
                MessageChain message;
                if(page==0){
                    message = new At(event.getSender()).plus(currentQuestion.toString());
                    for (SerializableImage image : currentQuestion.images)
                        message=message.plus(event.getGroup().uploadImage(image.bi));
                    Util.logger.debug("currentAnswer Image size:"+currentAnswer.images.size());
                }else{
                    message = new At(event.getSender()).plus(currentQuestion.answererList.get(page - 1).toString());
                    for (SerializableImage image : currentQuestion.answererList.get(page-1).images)
                        message=message.plus(event.getGroup().uploadImage(image.bi));
                    Util.logger.debug("currentQuestion Image size:"+currentQuestion.images.size());
                }
                Util.sendMes(event,message);
            }else {
                if (page < 0 || page * 10 >= queryData.size()) {
                    Util.sendMes(event, "给定的页码超出范围！页码范围为{x|x∈N,x≤" + ((queryData.size() - 1) / 10) + "}");
                    return true;
                }
                Image i = event.getGroup().uploadImage(
                        state == State.Search_Question ? Util.generateSearchResultImage(queryData, page, text, member.getNameCard()) :
                        state == State.My_Questions ? Util.generateMyQuestionsImage(queryData, page, member.getNameCard()) :
                        state == State.My_Answers ? Util.generateMyAnswersImage(queryData, page, member.getNameCard()) :
                        Util.generateUnsolvedImage(queryData, page, member.getNameCard()));
                Util.sendMes(event, i);
            }
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:view")){
            if(state==State.Write_Answer||state==State.Write_Question){
                Util.sendMes(event,"不可以在当前上下文中使用该指令！");
                return true;
            }
            int number;
            try{
                number=Integer.parseInt(content.split("((?i)wiki:view)",2)[1].trim());
            }catch (Exception e){
                Util.sendMes(event,"参数错误。\r\n命令用法 Wiki:View + <序号>");
                return true;
            }
            if(number<0||number>=queryData.size()){
                Util.sendMes(event,"给定的序号超出范围！序号范围为{x|x∈N,x<"+(queryData.size()+"}"));
                return true;
            }
            currentQuestion= queryData.get(number);
            viewDetail=true;
            MessageChain message=new At(event.getSender()).plus(currentQuestion.toString());
            for (SerializableImage image : currentQuestion.images)
                message=message.plus(event.getGroup().uploadImage(image.bi));
            event.getGroup().sendMessage(message);
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:back")){
            if(!viewDetail){
                Util.sendMes(event,"不可以在当前上下文中使用该指令！");
                return true;
            }
            viewDetail=false;
            Util.sendMes(event,"回退成功!");
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:answer")){
            if(state==State.Write_Answer||state==State.Write_Question){
                Util.sendMes(event,"不可以在写问题/回答时使用该指令！\r\n请回复Wiki:Abort退出编辑模式之后再尝试。");
                return true;
            }
            int number;
            try{
                number=Integer.parseInt(content.split("((?i)wiki:answer)",2)[1].trim());
            }catch (Exception e){
                Util.sendMes(event,"参数错误。\r\n命令用法 Wiki:Answer + <序号>");
                return true;
            }
            if(number<0||number>=queryData.size()){
                Util.sendMes(event,"给定的序号超出范围！序号范围为{x|x∈N,x<"+(queryData.size()+"}"));
                return true;
            }
            currentQuestion=queryData.get(number);
            currentAnswer=new Answerer();
            currentAnswer.id=event.getSender().getId();
            currentAnswer.name=event.getSenderName();
            Util.sendMes(event,"你正在为一个问题写回答。\r\n" +
                    "输入\"Wiki:Text <文本>\"给你的回答追加文本。\r\n" +
                    "输入\"Wiki:Image [图片]\"给你的回答追加图片。\r\n" +
                    "输入\"Wiki:Abort\"取消写回答\r\n" +
                    "输入\"Wiki:Submit\"提交回答");
            state=State.Write_Answer;
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:title")){
            if(state!=State.Write_Question){
                Util.sendMes(event,"不可以在当前上下文中使用该指令！");
                return true;
            }
            String[] temp=content.split("((?i)wiki:title)",2);
            if(temp.length<2){
                Util.sendMes(event,"参数错误！\r\n命令用法:Wiki:Title + <标题>");
                return true;
            }
            String title=temp[1].trim();
            if(title.equalsIgnoreCase("")){
                Util.sendMes(event,"参数错误！\r\n命令用法:Wiki:Title + <标题>");
                return true;
            }
            currentQuestion.title=title;
            Util.sendMes(event,"标题设置成功!");
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:text")){
            if(!(state==State.Write_Answer||state==State.Write_Question)){
                Util.sendMes(event,"不可以在当前上下文中使用该指令！");
                return true;
            }
            String[] temp=content.split("((?i)wiki:text)",2);
            if(temp.length<2){
                Util.sendMes(event,"参数错误！\r\n命令用法:Wiki:Text + <文本>");
                return true;
            }
            String text=temp[1].trim();
            if(text.equalsIgnoreCase("")){
                Util.sendMes(event,"参数错误！\r\n命令用法:Wiki:Text + <文本>");
                return true;
            }
            if(state==State.Write_Answer){
                if(currentAnswer.text==null)currentAnswer.text=text;
                else currentAnswer.text+="\r\n"+text;
            }else{
                if(currentQuestion.text==null)currentQuestion.text=text;
                else currentQuestion.text+=text;
            }
            Util.sendMes(event,"文本追加成功!");
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:image")){
            if(!(state==State.Write_Answer||state==State.Write_Question)){
                Util.sendMes(event,"不可以在当前上下文中使用该指令!");
                return true;
            }
            Iterator<SingleMessage> messages=event.getMessage().iterator();
            SingleMessage temp;
            BufferedImage im=null;
            while(messages.hasNext()){
                temp=messages.next();
                if(!(temp instanceof Image))
                    continue;
                try {
                    URL url = new URL(event.getBot().queryImageUrl((Image) temp));
                    im= ImageIO.read(url);
                }catch (Exception e){
                    Util.logger.error(e);
                    return true;
                }
            }
            if(im==null){
                Util.sendMes(event,"参数错误，没有检测到有效图片。\r\n命令用法 Wiki:Image + [图片]");
                return true;
            }
            if(state==State.Write_Answer){
                currentAnswer.images.add(new SerializableImage(im));
            }else{
                currentQuestion.images.add(new SerializableImage(im));
            }
            Util.sendMes(event,"图片追加成功!");
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:submit")){
            if(!(state==State.Write_Answer||state==State.Write_Question)){
                Util.sendMes(event,"不可以在当前上下文中使用该指令!");
                return true;
            }
            if(state==State.Write_Answer){
                if(currentAnswer.text==null){
                    Util.sendMes(event,"回答没有文本!请回复\"Wiki:Text + <文本>\"为回答追加文本。");
                    return true;
                }
                currentQuestion.answererList.add(currentAnswer);
                currentQuestion.requireFurtherInfo=false;
                Util.sendMes(event,"回答提交成功!感谢群大佬的解答~");
                if(event.getGroup().contains(currentQuestion.questioner.id)){
                    event.getGroup().sendMessage(new At(event.getGroup().get(currentQuestion.questioner.id)).plus("你的问题有群大佬回答了，快去看看吧~"));
                }else{
                    event.getGroup().sendMessage("提问者已退群");
                }
            }else{
                if(currentQuestion.title==null){
                    Util.sendMes(event,"问题没有题目!请回复\"Wiki:Title + <题目>\"为问题设置题目。");
                    return true;
                }
                if(currentQuestion.text==null){
                    Util.sendMes(event,"问题没有文本!请回复\"Wiki:Text + <文本>\"为问题追加文本。");
                    return true;
                }
                currentQuestion.questionId=Util.questionIdPointer++;
                if(!Util.questions.containsKey(event.getGroup().getId()))
                    Util.questions.put(event.getGroup().getId(),new ArrayList<>());
                Util.questions.get(event.getGroup().getId()).add(currentQuestion);
                Util.sendMes(event,"问题提交成功!请耐心等待群大佬的解答。");
            }
            state= State.Null;
            Util.saveData();
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:abort")){
            if(!(state==State.Write_Answer||state==State.Write_Question)){
                Util.sendMes(event,"不可以在当前上下文中使用该指令!");
                return true;
            }
            state=State.Null;
            Util.sendMes(event,"已中止。");
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:delete")){
            if(!(state==State.My_Questions||state==State.My_Answers)){
                Util.sendMes(event,"不可以在当前上下文中使用该指令!");
                return true;
            }
            int number;
            try{
                number=Integer.parseInt(content.split("((?i)wiki:delete)",2)[1].trim());
            }catch (Exception e){
                Util.sendMes(event,"参数错误。\r\n命令用法 Wiki:Delete + <序号>");
                return true;
            }
            if(number<0||number>=queryData.size()){
                Util.sendMes(event,"给定的序号超出范围！序号范围为{x|x∈N,x<"+(queryData.size()+"}"));
                return true;
            }
            Question target=queryData.get(number);
            if(state==State.My_Questions){
                Util.questions.get(event.getGroup().getId()).remove(target);
                Util.sendMes(event,"问题删除成功!");
            }else{
                Iterator<Answerer> it=target.answererList.iterator();
                Answerer temp;
                Answerer delete=null;
                while(it.hasNext()){
                    temp=it.next();
                    if(temp.id==event.getSender().getId())delete=temp;
                }
                target.answererList.remove(delete);
                Util.sendMes(event,"已删除该问题下你的最新回答!");
            }
            Util.saveData();
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:accept")){
            if(!(state==State.My_Questions&&viewDetail)){
                Util.sendMes(event,"不可以在当前上下文中使用该指令!");
                return true;
            }
            int number;
            try{
                number=Integer.parseInt(content.split("((?i)wiki:accept)",2)[1].trim());
            }catch (Exception e){
                Util.sendMes(event,"参数错误。\r\n命令用法 Wiki:Accept + <序号>");
                return true;
            }
            if(number<=0||number>currentQuestion.answererList.size()){
                Util.sendMes(event,"给定的序号超出范围！序号范围为{x|x∈N+,x≤"+(currentQuestion.answererList.size()+"}"));
                return true;
            }
            currentQuestion.answererList.get(number-1).accepted=true;
            currentQuestion.requireFurtherInfo=false;
            Util.sendMes(event,"已标记问题为\"解决\"。");
            Util.saveData();
            return true;
        }
        else if(content.toLowerCase().startsWith("wiki:further")){
            if(state!=State.My_Questions){
                Util.sendMes(event,"不可以在当前上下文中使用该指令!");
                return true;
            }
            currentQuestion.requireFurtherInfo=true;
            Util.sendMes(event,"已标记问题为\"追问\"。");
            Util.saveData();
            return true;
        }
        else return false;
    }
    /**
     * 在给定的群消息中解析会话.
     * 能够启动会话的命令包含:Search,Question,MyQuestion,MyAnswer,Unsolved,Help.即不需要上下文就能执行的指令.
     *
     * @return 如果群消息中含有能启动会话的指令,则返回创建的会话,否则返回null.
     * */
    public static @Nullable Session parseSession(@NotNull GroupMessageEvent event){
        String content=event.getMessage().contentToString();
        Session s=new Session(event);
        s.member=event.getSender();
        if(content.toLowerCase().startsWith("wiki:search")){
            String[] temp=content.split("((?i)wiki:search)",2);
            String query;
            if(temp.length<2){
                Util.sendMes(event,"参数错误。命令用法 Wiki:Search + <关键词>");
                return null;
            }
            query=temp[1].trim();
            s.text=query;
            s.queryData=Util.search(query,event.getGroup().getId());
            if(s.queryData.size()<=0){
                Util.sendMes(event,"没有搜索到相关结果。请尝试换一个关键词或者发起提问。\r\n本群总计问题数:"+
                        (Util.questions.containsKey(event.getGroup().getId())?Util.questions.get(event.getGroup().getId()).size():0));
                return null;
            }
            Image i=event.getGroup().uploadImage(Util.generateSearchResultImage(s.queryData,0,query,event.getSenderName()));
            Util.sendMes(event,i);
            s.state=State.Search_Question;
            return s;
        }
        else if(content.toLowerCase().startsWith("wiki:question")){
            s.state=State.Write_Question;
            s.currentQuestion=new Question();
            s.currentQuestion.questioner=new Questioner();
            s.currentQuestion.questioner.id=s.member.getId();
            s.currentQuestion.questioner.name=s.member.getNameCard();
            Util.sendMes(event,"你正在创建一个问题\r\n" +
                    "输入\"Wiki:Title <标题>\"给问题设定一个标题。\r\n" +
                    "输入\"Wiki:Text <文本>\"给问题追加文本\r\n" +
                    "输入\"Wiki:Image [图片]\"给问题追加图片\r\n" +
                    "输入\"Wiki:Abort\"取消创建问题。\r\n" +
                    "输入\"Wiki:Submit\"提交问题");
            return s;
        }
        else if(content.toLowerCase().startsWith("wiki:myquestion")){
            s.state=State.My_Questions;
            s.queryData=Util.myQuestions(event.getGroup().getId(),event.getSender().getId());
            if(s.queryData.size()<=0){
                Util.sendMes(event,"你还没有提出问题。\r\n回复\"Wiki:Question\"来提出一个问题，群大佬可能会帮助你哦。");
                return null;
            }
            Image i=event.getGroup().uploadImage(Util.generateMyQuestionsImage(s.queryData,0,event.getSenderName()));
            Util.sendMes(event,i);
            return s;
        }
        else if(content.toLowerCase().startsWith("wiki:myanswer")){
            s.state=State.My_Answers;
            s.queryData=Util.myAnswers(event.getGroup().getId(),event.getSender().getId());
            if(s.queryData.size()<=0){
                Util.sendMes(event,"你还没有回答过问题。\r\n回复\"Wiki:Unsolved\"获取未解决的问题列表。");
                return null;
            }
            Image i=event.getGroup().uploadImage(Util.generateMyAnswersImage(s.queryData,0,event.getSenderName()));
            Util.sendMes(event,i);
            return s;
        }
        else if(content.toLowerCase().startsWith("wiki:unsolved")){
            s.state=State.View_Unsolved;
            s.queryData=Util.unsolvedQuestions(event.getGroup().getId());
            if(s.queryData.size()<=0){
                Util.sendMes(event,"本群暂无未解决的问题。群大佬可以休息一下了~");
                return null;
            }
            Image i=event.getGroup().uploadImage(Util.generateUnsolvedImage(s.queryData,0,event.getSenderName()));
            Util.sendMes(event,i);
            return s;
        }
        else if(content.toLowerCase().startsWith("wiki:help")){
            Image i=event.getGroup().uploadImage(Util.helpImage);
            Util.sendMes(event,i);
            return null;
        }
        else if(content.toLowerCase().startsWith("wiki:")){
            Util.sendMes(event,"未知指令或该指令不能在当前上下文中执行，回复Wiki:Help获取帮助。");
            return null;
        }
        return null;
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
        Write_Answer,
        Null
    }
}
