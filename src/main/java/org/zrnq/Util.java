package org.zrnq;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Util {
    /**
     * 在当前群的问题数据中搜索指定的关键词.
     * @return 搜索到的问题列表，按相关度降序排列。如果没有搜索到，返回空列表.
     * */
    public static @NotNull ArrayList<Question> search(String keyword, long groupId){
        ArrayList<Question> groupQuestions=QuestionListHolder.INSTANCE.getListOf(groupId);
        if(groupQuestions.size()<=0)
            return new ArrayList<>();
        HashMap<Integer,Integer> searchCache=new HashMap<>();
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
        Integer[] result= searchCache.keySet().toArray(new Integer[0]);
        Arrays.sort(result, (o1, o2) -> searchCache.get(o2)-searchCache.get(o1));
        ArrayList<Question> resultList=new ArrayList<>();
        for(Integer questionId:result)
            resultList.add(get(groupId,questionId));
        return resultList;
    }
    /**
     * 在当前群的问题数据中搜索指定成员提出的问题.
     * */
    public static @NotNull ArrayList<Question> myQuestions(long groupId, long userId){
        ArrayList<Question> groupQuestions=QuestionListHolder.INSTANCE.getListOf(groupId);
        ArrayList<Question> result=new ArrayList<>();
        if(groupQuestions==null)return result;
        for(Question q:groupQuestions)
            if(q.questioner.id==userId)result.add(q);
        return result;
    }
    /**
     * 在当前群的问题数据中搜索指定成员回答过的问题.
     * */
    public static @NotNull ArrayList<Question> myAnswers(long groupId, long userId){
        ArrayList<Question> groupQuestions=QuestionListHolder.INSTANCE.getListOf(groupId);
        ArrayList<Question> result=new ArrayList<>();
        if(groupQuestions==null)return result;
        for(Question q:groupQuestions){
            for (Answer answerer : q.answererList)
                if (answerer.id == userId) result.add(q);
        }
        return result;
    }
    /**
     * 在当前群的问题数据中搜索标记为"等待"或"追问"的问题.
     * */
    public static @NotNull ArrayList<Question> unsolvedQuestions(long groupId){
        ArrayList<Question> groupQuestions=QuestionListHolder.INSTANCE.getListOf(groupId);
        ArrayList<Question> result=new ArrayList<>();
        if(groupQuestions==null)return result;
        for(Question q:groupQuestions)
            if(q.requireFurtherInfo||q.answererList.size()<=0)result.add(q);
        return result;
    }
    /**
     * 生成问题搜索结果图像.
     * 每页10个问题.
     * */
    public static byte[] generateResultImage(@NotNull ArrayList<Question> searchResult, int page, String title, String subtitle1, String subtitle2){
        if(page*10>=searchResult.size())throw new IllegalArgumentException("Page out of bound. Page:"+page+". Total size:"+searchResult.size());
        int startPoint=page*10;
        int endPoint=Math.min(searchResult.size(),page*10+10);
        int height=600+(endPoint-startPoint)*50;
        BufferedImage result=new BufferedImage(700,height,BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g=(Graphics2D)result.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(PluginImageHolder.INSTANCE.getBufferedImage("bg"), 0,0,null);
        g.setColor(new Color(0,0,0,100));
        g.fillRect(0,0,700,height);
        g.setColor(Color.white);
        g.setFont(R.F);
        g.drawString(title,10,30);
        g.setColor(Color.lightGray);
        g.setFont(R.F.deriveFont(20f));
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
        g.drawString("共有"+searchResult.size()+"个条目，当前为第"+page+"页。",10,height-150);
        g.setColor(Color.lightGray);
        g.setFont(R.F.deriveFont(15f));
        g.drawString(R.name+" "+R.version+" UI",10,height-50);
        return PluginImageHolder.toByteArray(result);
    }
    private static Graphics2D stub;
    public static void initImageStub(){
        BufferedImage img = new BufferedImage(64,64,BufferedImage.TYPE_3BYTE_BGR);
        stub = img.createGraphics();
        stub.setFont(R.F.deriveFont(20f));
        stub.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        stub.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    public static byte[] generateDetailImage(Question question){
        int width = 600;
        int imageGridSize = width/3;
        int height = 0;
        //Preload - Calculate the height of the image
        ArrayList<RenderedText> textList = new ArrayList<>();
        //10px padding
        textList.add(preCalculate(stub,"#"+question.questionId,width));
        textList.add(preCalculate(stub,question.title,width));
        //5px interval
        textList.add(preCalculate(stub, question.getStatusBarText(), width));
        //5px interval
        textList.add(preCalculate(stub, question.text, width));
        //5px interval
        //Image grid
        //5px interval
        for (int i = 0; i < question.answererList.size(); i++) {
            Answer answer = question.answererList.get(i);
            //5px line separator
            textList.add(preCalculate(stub,answer.getStatusBarText(),width));
            //5px interval
            textList.add(preCalculate(stub, answer.text, width));
            //5px interval
            //Image grid
            height +=((answer.images.size()+2)/3)*imageGridSize;
            //5px interval
        }
        //10px padding
        textList.add(preCalculate(stub,R.name+" "+R.version+" UI",width));
        //5px interval

        height += 45 + ((question.images.size()+2)/3)*imageGridSize + question.answererList.size()*25;
        for(RenderedText text : textList)
            height += text.height;

        //Post Load - paint texts and images at calculated position
        BufferedImage result = new BufferedImage(width+100,height,BufferedImage.TYPE_3BYTE_BGR);
        int yPos = 10;
        int textMargin = 80;
        int avatarMargin = 10;
        int avatarSize = 60;
        Iterator<RenderedText> it = textList.iterator();
        RenderedText tmp;
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(Color.WHITE);
        g.fillRect(0,0,width+100,height);
        g.setFont(R.F.deriveFont(20f));

        g.setColor(R.orange);
        doDraw(g,tmp=it.next(),textMargin,yPos);
        yPos += tmp.height;

        g.setColor(Color.BLACK);
        doDraw(g,tmp=it.next(),textMargin,yPos);
        doDraw(g,"提问",getAvatarOf(question.groupId, question.questioner.id),avatarMargin,yPos,avatarSize);
        yPos += tmp.height;
        yPos +=5;

        g.setColor(R.skyBlue);
        doDraw(g,tmp=it.next(),textMargin,yPos);
        yPos += tmp.height;
        yPos +=5;

        g.setColor(Color.BLACK);
        doDraw(g,tmp=it.next(),textMargin,yPos);
        yPos += tmp.height;
        yPos +=5;

        doDraw(g, question.images, textMargin,yPos,imageGridSize);
        yPos += ((question.images.size()+2)/3)*imageGridSize;
        yPos +=5;

        for (int i = 0; i < question.answererList.size(); i++) {
            Answer answer = question.answererList.get(i);
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(textMargin,yPos,textMargin+width,yPos);
            yPos+=5;

            g.setColor(R.skyBlue);
            doDraw(g,tmp=it.next(),textMargin,yPos);
            doDraw(g,"A"+i,getAvatarOf(question.groupId,answer.id),avatarMargin,yPos,avatarSize);
            yPos += tmp.height;
            yPos += 5;

            g.setColor(Color.BLACK);
            doDraw(g,tmp=it.next(),textMargin,yPos);
            yPos += tmp.height;
            yPos += 5;

            doDraw(g,answer.images,textMargin,yPos,imageGridSize);
            yPos += ((answer.images.size()+2)/3)*imageGridSize;
            yPos += 5;
        }

        yPos += 10;
        g.setColor(Color.darkGray);
        doDraw(g,it.next(),avatarMargin,yPos);

        return PluginImageHolder.toByteArray(result);
    }
    /**
     * 获取指定群中指定ID的问题.
     * */
    public static @NotNull Question get(long group, long questionId){
        ArrayList<Question> groupQuestions = QuestionListHolder.INSTANCE.getListOf(group);
        Iterator<Question> it=groupQuestions.iterator();
        Question tempQuestion;
        while(it.hasNext()){
            tempQuestion=it.next();
            if(tempQuestion.questionId==questionId)return tempQuestion;
        }
        throw new IllegalArgumentException("Question ID:"+questionId+" not found.");
    }
    public static void sendMes(@NotNull GroupMessageEvent event, Message message){
        QuoteReply quote=new QuoteReply(event.getMessage());
        event.getGroup().sendMessage(quote.plus(message));
    }
    public static void sendMes(@NotNull GroupMessageEvent event, String message){
        sendMes(event,new PlainText(message));
    }
    public static void sendMes(@NotNull GroupMessageEvent event, ExternalResource resource){
        sendMes(event,event.getGroup().uploadImage(resource));
        try {
            resource.close();
        } catch (IOException e) {
            R.logger.error("关闭ExternalResource失败",e);
        }
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(time));
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
    public static byte[] streamToByteArray(InputStream is)throws Exception{
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        byte[] buf=new byte[1024];
        int len;
        while((len=is.read(buf))!=-1)
            bos.write(buf,0,len);
        return bos.toByteArray();
    }
    public static byte[] readPackageResource(String name){
        InputStream is = Wiki.INSTANCE.getResourceAsStream(name);
        if(is == null){
            R.logger.error("找不到包内资源文件:"+name);
        }else{
            try{
                return Util.streamToByteArray(is);
            }catch (Exception e){
                R.logger.error("读取包内资源文件时出错:"+name,e);
            }
        }
        return null;
    }

    public static RenderedText preCalculate(Graphics2D g, String text, int w){
        RenderedText rendered = new RenderedText();
        FontMetrics fm=g.getFontMetrics();
        //Preventing characters overflow
        w -= fm.charWidth('啊');
        int lineCount = 1;
        int lastDraw=0;
        rendered.linebreaks.add(0);
        for (int i = 1; i < text.length(); i++) {
            if(text.charAt(i)=='\n'||fm.stringWidth(text.substring(lastDraw,i))>w){
                rendered.linebreaks.add(i);
                lineCount++;
                lastDraw=i;
            }
        }
        rendered.linebreaks.add(text.length());
        rendered.ascent = fm.getAscent();
        rendered.lineHeight = fm.getHeight();
        rendered.height = lineCount*fm.getHeight();
        rendered.text = text;
        return rendered;
    }

    public static void doDraw(Graphics2D g, RenderedText text, int x, int y){
        for (int i = 1; i < text.linebreaks.size(); i++) {
            g.drawString(text.text.substring(text.linebreaks.get(i-1),text.linebreaks.get(i)),
                    x,y+text.ascent+text.lineHeight*(i-1));
        }
    }

    public static void doDraw(Graphics2D g, ArrayList<SerializableImage> images, int x,int y,int a){
        int xPos,yPos;
        int w,h;
        String ids;
        FontMetrics metric = g.getFontMetrics();
        for(int i=0;i< images.size();i++){
            BufferedImage entity = images.get(i).getImage();
            xPos = x + a * (i % 3);
            yPos = y + a * (i / 3);
            w=entity.getWidth();
            h=entity.getHeight();
            if(w>h){
                h = h * a / w;
                w = a;
                g.drawImage(entity,xPos,yPos+(a-h)/2,w,h,null);
            }else{
                w = w * a / h;
                h = a;
                g.drawImage(entity,xPos+(a-w)/2,yPos,w,h,null);
            }
            ids = "@" + images.get(i).getImageId();
            g.setColor(R.hover);
            g.fillRect(xPos,yPos,metric.stringWidth(ids),metric.getHeight());
            g.setColor(Color.CYAN);
            g.drawString(ids,xPos,yPos+metric.getAscent());
            g.setColor(R.skyBlue);
            g.drawRect(xPos,yPos,a,a);
        }
    }

    public static void doDraw(Graphics2D g, String append, BufferedImage avatar, int x, int y, int a){
        FontMetrics metric = g.getFontMetrics();
        g.drawImage(avatar,x,y,a,a,null);
        g.setColor(R.hover);
        g.fillRect(x,y,metric.stringWidth(append),metric.getHeight());
        g.setColor(Color.WHITE);
        g.drawString(append,x,y+metric.getAscent());
        g.setColor(R.skyBlue);
        g.drawRect(x,y,a,a);
    }

    public static BufferedImage getAvatarOf(long groupId, long userId){
        List<Bot> botList = Bot.getInstances();
        Group targetGroup = null;
        for(Bot bot : botList){
            if((targetGroup=bot.getGroup(groupId))!=null)
                break;
        }
        if(targetGroup == null){
            R.logger.warning("找不到群组:"+groupId+"，使用丢失图片材质。");
            return PluginImageHolder.INSTANCE.getBufferedImage("missing");
        }
        if(targetGroup.contains(userId)){
            try{
                URL url = new URL(targetGroup.getOrFail(userId).getAvatarUrl());
                return ImageIO.read(url);
            }catch (Exception e){
                R.logger.warning("下载用户("+userId+")的头像失败，使用丢失图片材质。");
                return PluginImageHolder.INSTANCE.getBufferedImage("missing");
            }
        }else{
            return PluginImageHolder.INSTANCE.getBufferedImage("missing");
        }
    }

    private static class RenderedText{
        int height;
        int lineHeight;
        int ascent;
        String text;
        final ArrayList<Integer> linebreaks = new ArrayList<>();
    }
}
