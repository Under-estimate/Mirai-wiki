package com.zjs;

import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;

public class Util {
    public static final File questionsLocation=new File("plugins\\Wiki\\questions.bin");
    public static final File configLocation=new File("plugins\\Wiki\\config.json");
    public static String version="Wiki-0.1.2-build20062616.jar";
    public static URL updateInquireUrl;
    public static URL updateDownloadUrl;
    public static MiraiLogger logger;
    public static HashMap<Long, ArrayList<Question>> questions;
    public static BufferedImage bgImage;
    public static BufferedImage helpImage;
    public static int questionIdPointer;
    public static Config config;
    public static final String help_1=
            "任何情况下都能够使用的指令\n\n" +
            "Wiki:Search + <关键词> 搜索有关问题\n" +
            "Wiki:Question 提出问题\n" +
            "Wiki:MyQuestion 查看自己提出的问题\n" +
            "Wiki:MyAnswer 查看自己回答过的问题\n" +
            "Wiki:Unsolved 查看本群中未解决的问题";
    public static final String help_2=
            "在一定上下文中能够使用的指令\n\n" +
                    "Wiki:Page + <页码> 跳转到指定页\n" +
                    "Wiki:View + <序号> 查看指定的问题及回答\n" +
                    "Wiki:Back 退出当前查看的问题(回到问题列表)\n" +
                    "Wiki:Answer + <序号> 为指定的问题写回答\n" +
                    "Wiki:Title + <标题> 为问题设置标题\n" +
                    "Wiki:Text + <文本> 为问题/回答追加文本\n" +
                    "Wiki:Image + [图片] 为问题/回答追加图片(不能是表情)\n" +
                    "Wiki:Submit 提交问题/回答\n" +
                    "Wiki:Abort 终止提出问题/写回答\n" +
                    "Wiki:Delete 删除问题/回答\n" +
                    "Wiki:Accept 接受回答并标记问题为\"解决\"\n" +
                    "Wiki:Further 表示需要更多信息，将问题标记为\"追问\"";
    /**
     * 保存问题数据，以便在下次启动时加载.
     * */
    public static void saveData(){
        try{
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(questionsLocation));
            oos.writeObject(questions);
            oos.writeObject(questionIdPointer);
            oos.flush();
            oos.close();
        }catch (Exception e){
            logger.error("Failed to save data.",e);
        }
    }
    /**
     * 在当前群的问题数据中搜索指定的关键词.
     * @return 搜索到的问题列表，按相关度降序排列。如果没有搜索到，返回空列表.
     * */
    public static @NotNull ArrayList<Question> search(String keyword, long groupId){
        if(!questions.containsKey(groupId))return new ArrayList<>();
        ArrayList<Question> groupQuestions=questions.get(groupId);
        HashMap<Long,Integer> searchCache=new HashMap<>();
        Iterator<Question> questionIterator=groupQuestions.iterator();
        Question tempQuestion;
        int match;
        while(questionIterator.hasNext()){
            tempQuestion=questionIterator.next();
            match=0;
            for(int i=0;i<keyword.length();i++){
                if(tempQuestion.title.contains(Character.toString(keyword.charAt(i))))match+=10;
                if(tempQuestion.text.contains(Character.toString(keyword.charAt(i))))match++;
            }
            if(match>0)searchCache.put(tempQuestion.questionId,match);
        }
        Long[] result= searchCache.keySet().toArray(new Long[0]);
        Arrays.sort(result, (Comparator<Long>) (o1, o2) -> {
            return searchCache.get(o2)-searchCache.get(o1);
        });
        ArrayList<Question> resultList=new ArrayList<>();
        for(Long questionId:result)
            resultList.add(get(groupId,questionId));
        return resultList;
    }
    /**
     * 在当前群的问题数据中搜索指定成员提出的问题.
     * */
    public static @NotNull ArrayList<Question> myQuestions(long groupId, long userId){
        ArrayList<Question> groupQuestions=questions.get(groupId);
        ArrayList<Question> result=new ArrayList<>();
        for(Question q:groupQuestions)
            if(q.questioner.id==userId)result.add(q);
        return result;
    }
    /**
     * 在当前群的问题数据中搜索指定成员回答过的问题.
     * */
    public static @NotNull ArrayList<Question> myAnswers(long groupId, long userId){
        ArrayList<Question> groupQuestions=questions.get(groupId);
        ArrayList<Question> result=new ArrayList<>();
        for(Question q:groupQuestions){
            for (Answerer answerer : q.answererList)
                if (answerer.id == userId) result.add(q);
        }
        return result;
    }
    /**
     * 在当前群的问题数据中搜索标记为"等待"或"追问"的问题.
     * */
    public static @NotNull ArrayList<Question> unsolvedQuestions(long groupId){
        ArrayList<Question> groupQuestions=questions.get(groupId);
        ArrayList<Question> result=new ArrayList<>();
        for(Question q:groupQuestions)
            if(q.requireFurtherInfo||q.answererList.size()<=0)result.add(q);
        return result;
    }
    /**
     * 生成问题搜索结果图像.
     * 每页10个问题.
     * */
    public static @NotNull BufferedImage generateResultImage(@NotNull ArrayList<Question> searchResult, int page, String title, String subtitle1, String subtitle2){
        if(page*10>=searchResult.size())throw new IllegalArgumentException("Page out of bound. Page:"+page+". Total size:"+searchResult.size());
        int startPoint=page*10;
        int endPoint=Math.min(searchResult.size(),page*10+10);
        int height=600+(endPoint-startPoint)*50;
        BufferedImage result=new BufferedImage(700,height,BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g=(Graphics2D)result.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Dimension d=(Dimension)config.getAs(Config.RESULT_BACKGROUND_OFFSET);
        g.drawImage(bgImage,null,d.width,d.height);
        g.setColor(new Color(0,0,0,100));
        g.fillRect(0,0,700,height);
        g.setColor(Color.white);
        g.setFont(new Font("Microsoft YaHei",Font.PLAIN,30));
        g.drawString(title,10,30);
        g.setColor(Color.lightGray);
        g.setFont(new Font("Microsoft YaHei",Font.PLAIN,20));
        g.drawString(subtitle1,10,60);
        g.drawString(subtitle2,10,80);
        g.setColor(Color.white);
        g.drawString("序号",10,110);
        g.drawString("标题",60,110);
        g.drawString("回答",600,110);
        g.drawString("状态",650,110);
        g.drawLine(10,130,690,130);
        for(int i=startPoint;i<endPoint;i++){
            int y=160+50*(i-startPoint);
            Question temp=searchResult.get(i);
            g.setColor(Color.cyan);
            g.drawString(Integer.toString(i),10,y);
            g.setColor(Color.white);
            g.drawString(limitString(temp.title,30),60,y);
            g.setColor(temp.answererList.size()>0?Color.green:Color.red);
            g.drawString(Integer.toString(temp.answererList.size()),600,y);
            g.setColor(temp.haveAccepted()?Color.green:
                    temp.answererList.size()<=0?Color.cyan:
                    temp.requireFurtherInfo?Color.magenta:
                    Color.yellow);
            g.drawString(temp.haveAccepted()?"解决":
                    temp.answererList.size()<=0?"等待":
                            temp.requireFurtherInfo?"追问":"未读"
                    ,650,y);
        }
        g.setColor(Color.cyan);
        g.drawString("共有"+searchResult.size()+"个条目，当前为第"+page+"页。",10,height-300);
        g.drawString("回复\"Wiki:View <序号>\"来查看详细信息。",10,height-250);
        g.drawString("回复\"Wiki:Help\"获取使用帮助",10,height-200);
        g.setColor(Color.lightGray);
        g.setFont(new Font("Microsoft YaHei",Font.PLAIN,15));
        g.drawString("Wiki v0.1.1 UI",10,height-100);
        g.drawString("Developer e-mail: 1260717118@qq.com",10,height-70);
        g.drawString("~ Resolution for FAQs ~",10,height-50);
        return result;
    }
    public static @NotNull BufferedImage generateSearchResultImage(ArrayList<Question> searchResult, int page, String keyword, String user){
        return generateResultImage(searchResult,page,"Search Result","Keyword:["+keyword+"]","User:["+user+"]");
    }
    public static @NotNull BufferedImage generateMyQuestionsImage(ArrayList<Question> searchResult, int page, String user){
        return generateResultImage(searchResult,page,"My Questions",user,"");
    }
    public static @NotNull BufferedImage generateMyAnswersImage(ArrayList<Question> searchResult, int page, String user){
        return generateResultImage(searchResult,page,"My Answers",user,"");
    }
    public static @NotNull BufferedImage generateUnsolvedImage(ArrayList<Question> searchResult, int page, String user){
        return generateResultImage(searchResult,page,"Unsolved Questions",user,"");
    }
    /**
     * 生成帮助图像.
     * 此方法在插件初始化时调用.
     * */
    public static void generateHelpImage(){
        BufferedImage temp=new BufferedImage(1020,425,BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g=(Graphics2D) temp.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Dimension d=(Dimension) config.getAs(Config.HELP_BACKGROUND_OFFSET);
        g.drawImage(helpImage,null,d.width,d.height);
        g.setColor(new Color(0,0,0,100));
        g.fillRect(0,0,1020,425);
        g.setColor(Color.cyan);
        g.setFont(new Font("Microsoft YaHei",Font.PLAIN,30));
        g.drawString("Wiki Help Page",10,30);
        g.setFont(new Font("Microsoft YaHei",Font.PLAIN,20));
        StringTokenizer st=new StringTokenizer(help_1,"\n",false);
        g.setColor(Color.white);
        int y=60;
        while(st.hasMoreTokens()) {
            g.drawString(st.nextToken(), 10, y);
            y+=30;
        }
        st=new StringTokenizer(help_2,"\n",false);
        y=60;
        while(st.hasMoreTokens()) {
            g.drawString(st.nextToken(), 522, y);
            y+=30;
        }
        helpImage=temp;
    }
    /**
     * 获取指定群中指定ID的问题.
     * */
    public static @NotNull Question get(long group, long questionId){
        ArrayList<Question> groupQuestions=questions.get(group);
        Iterator<Question> it=groupQuestions.iterator();
        Question tempQuestion;
        while(it.hasNext()){
            tempQuestion=it.next();
            if(tempQuestion.questionId==questionId)return tempQuestion;
        }
        throw new IllegalArgumentException("Question ID:"+questionId+" not found.");
    }
    /**
     * 发送消息的简化方法.
     * */
    public static void sendMes(@NotNull GroupMessageEvent event, Message message){
        event.getGroup().sendMessage(new At(event.getSender()).plus(message));
    }
    /**
     * 发送消息的简化方法.
     * */
    public static void sendMes(@NotNull GroupMessageEvent event, String message){
        event.getGroup().sendMessage(new At(event.getSender()).plus(message));
    }
    /**
     * 限制字符串的长度,过长的部分截去并用"..."代替.
     * */
    public static String limitString(@NotNull String original, int maxLength){
        return original.length()<=maxLength?original:original.substring(0,maxLength)+"...";
    }
    /**
     * 由millisecond表示的时间生成正常的时间表示字符串.
     * */
    public static @NotNull String parseTime(long time){
        GregorianCalendar gc=new GregorianCalendar();
        gc.setTimeInMillis(time);
        return gc.get(Calendar.YEAR)+"/"+(gc.get(Calendar.MONTH)+1)+"/"+gc.get(Calendar.DAY_OF_MONTH);
    }
    /**
     * 判断字符串中是否含有给定字符串组中的至少一个.
     * */
    public static boolean containsAny(String target,String... matchers){
        for(String s:matchers){
            if(target.contains(s))return true;
        }
        return false;
    }
    /**
     * 判断字符串中是否equalsIgnoreCase给定字符串组中的至少一个.
     * */
    public static boolean matchesAny(String target,String... matchers){
        for(String s:matchers){
            if(target.equalsIgnoreCase(s))return true;
        }
        return false;
    }
    /**
     * 将消息中的纯文本部分提取出来(不包含At).
     * */
    public static String extractPlainText(MessageEvent e){
        Iterator<SingleMessage> it=e.getMessage().iterator();
        SingleMessage temp;
        StringBuilder sb=new StringBuilder();
        while(it.hasNext()){
            temp=it.next();
            if(temp instanceof PlainText){
                sb.append(temp.contentToString());
            }
        }
        return sb.toString();
    }
    public static byte[] streamToByteArray(InputStream is)throws Exception{
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        byte[] buf=new byte[1024];
        int len;
        while((len=is.read(buf))!=-1)
            bos.write(buf,0,len);
        return bos.toByteArray();
    }
}
