package com.zjs;

import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

class Wiki extends PluginBase {
    HashMap<Long, ArrayList<Session>> sessions;
    Listener<GroupMessageEvent> listener;
    Listener<MemberJoinEvent> join;
    Looper looper;
    NotificationServiceProvider provider;
    @Override
    public void onLoad(){
        getLogger().info("Wiki is being loaded.");
    }
    @Override
    public void onEnable(){
        getLogger().info("Wiki is being enabled.");
        Util.logger=getLogger();
        sessions=new HashMap<>();
        looper=new Looper();
        provider=new NotificationServiceProvider();
        new Thread(provider).start();
        new Thread(looper).start();
        loadData();
        Util.generateHelpImage();
        listener=getEventListener().subscribeAlways(GroupMessageEvent.class, groupMessageEvent -> {
            if(groupMessageEvent.getMessage().contentToString().startsWith("Wiki:")) {
                if(!looper.queue.isEmpty())
                    Util.sendMes(groupMessageEvent,"你的请求正在排队。前方有"+looper.queue.size()+"个请求。\r\n" +
                            "为什么要排队?因为作者没(鸽)有(了)做多线程优化，同时处理多个请求可能会出错。");
                looper.post(groupMessageEvent);
            }
            String content=Util.extractPlainText(groupMessageEvent);
            if(!Util.whiteList.contains(groupMessageEvent.getSender().getId())){
                provider.cancelAllMatching(groupMessageEvent.getGroup().getId());
            }
            if(!(Util.containsAny(content,"没什么")&&Util.matchesAny(content,"?","？"))&&
                    Util.containsAny(content,"?","？","吗","呢","怎么","如何","什么")){
                provider.post(new NotificationService(120*1000,groupMessageEvent));
            }
        });
        join=getEventListener().subscribeAlways(MemberJoinEvent.class,event->
                event.getGroup().sendMessage(new At(event.getMember()).plus("欢迎进群~\r\n\r\n" +
                "本群问答系统已上线，回复Wiki:Help获取使用帮助。")));
    }

    /**
     * 加载包内资源文件和外部数据.
     * */
    @SuppressWarnings("unchecked")
    private void loadData(){
        if(!Util.questionsLocation.exists()){
            Util.questions=new HashMap<>();
            Util.questionIdPointer=0;
        }else {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Util.questionsLocation));
                Util.questions = (HashMap<Long, ArrayList<Question>>) ois.readObject();
                Util.questionIdPointer = (int) ois.readObject();
            } catch (Exception e) {
                getLogger().error("Data corrupted.", e);
                Util.questions = new HashMap<>();
            }
        }
        Util.whiteList=new ArrayList<>();
        if(Util.whiteListLocation.exists()){
            try {
                BufferedReader br=new BufferedReader(new FileReader(Util.whiteListLocation));
                while(true){
                    String content=br.readLine();
                    if(content==null)break;
                    if(content.equalsIgnoreCase(""))break;
                    Util.whiteList.add(Long.parseLong(content));
                }
            } catch (Exception e) {
                getLogger().error("Invalid whitelist file content.", e);
            }
        }
        try {
            Util.bgImage = ImageIO.read(this.getClass().getResourceAsStream("/bg.jpeg"));
            Util.helpImage = ImageIO.read(this.getClass().getResourceAsStream("/help.jpg"));
        } catch (Exception e) {
            getLogger().error("Failed to load default background image. This problem may be caused by reloading plugins. Restarting Mirai may solve this problem.");
        }
        try {
            Process process = Runtime.getRuntime().exec("dir");
            process.waitFor();
            if(process.exitValue()==-1)getLogger().warning("Detected non-Windows runtime. Please install font \"Microsoft YaHei\" so that Chinese characters are rendered properly.");
        } catch (Exception e) {
            getLogger().warning("Detected non-Windows runtime. Please install font \"Microsoft YaHei\" so that Chinese characters are rendered properly.");
        }
        if(!System.getProperty("os.name").toLowerCase().contains("win"))
            getLogger().warning("Detected non-Windows runtime. Please install font \"Microsoft YaHei\" so that Chinese characters are rendered properly.");
    }
    @Override
    public void onDisable(){
        looper.stop();
        provider.stop();
    }
    /**
     * 循环处理正在排队的请求.
     * */
    class Looper implements Runnable{
        LinkedList<GroupMessageEvent> queue=new LinkedList<>();
        private final Object lock=new Object();
        public boolean stop=false;
        @Override
        public void run() {
            while(!stop){
                synchronized (lock){
                    if(queue.isEmpty()){
                        try {
                            lock.wait();
                        }catch (Exception e){
                            Util.logger.error(e);
                        }
                    }
                }
                if(stop)break;
                GroupMessageEvent event=queue.poll();
                if(event==null){
                    Util.logger.error("Internal Error: Poll returns a null value.");
                    continue;
                }
                process(event);
            }
        }
        /**
         * 处理请求.
         *
         * @param event 含有命令的群消息事件.
         * */
        private void process(GroupMessageEvent event){
            if(sessions.containsKey(event.getGroup().getId())){
                ArrayList<Session> list=sessions.get(event.getGroup().getId());
                Iterator<Session> it=list.iterator();
                Session temp;
                while(it.hasNext()){
                    temp=it.next();
                    if(temp.member.getId()==event.getSender().getId()){
                        if(!temp.parseAction(event)){
                            Session s=Session.parseSession(event);
                            if(s!=null){
                                list.remove(temp);
                                list.add(s);
                                return;
                            }
                            return;
                        }
                        if(temp.state== Session.State.Null)
                            list.remove(temp);
                        return;
                    }
                }
                Session s=Session.parseSession(event);
                if(s!=null)
                    list.add(s);
            }else{
                sessions.put(event.getGroup().getId(),new ArrayList<>());
                Session s=Session.parseSession(event);
                if(s!=null)
                    sessions.get(event.getGroup().getId()).add(s);
            }
        }
        /**
         * 将一个请求添加到队列末尾.
         *
         * @param event 含有命令的群消息事件.
         * */
        public void post(GroupMessageEvent event){
            queue.add(event);
            synchronized (lock){
                lock.notify();
            }
        }
        public void stop(){
            stop=true;
            synchronized (lock){
                lock.notify();
            }
        }
    }
}
