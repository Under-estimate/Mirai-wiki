package com.zjs;

import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.Timer;

class Wiki extends PluginBase {
    HashMap<Long, ArrayList<Session>> sessions;
    Listener<GroupMessageEvent> listener;
    Listener<MemberJoinEvent> join;
    Looper looper;
    Thread looperThread;
    Timer autoUpdateTimer;
    NotificationServiceProvider provider;
    boolean optionPaneShown=false;
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
        looperThread=new Thread(looper);
        looperThread.start();
        try{
            Util.updateInquireUrl=new URL("http://20bf488.nat123.cc:25547/update?app=wiki");
            Util.updateDownloadUrl=new URL("http://20bf488.nat123.cc:25547/download?app=wiki");
        }catch (Exception e){
            getLogger().error(e);
        }
        autoUpdateTimer=new Timer();
        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    getLogger().info("检查更新...");
                    HttpURLConnection conn=(HttpURLConnection) Util.updateInquireUrl.openConnection();
                    conn.connect();
                    String result=new String(Util.streamToByteArray(conn.getInputStream()));
                    if(result.equalsIgnoreCase(Util.version)){
                        getLogger().info("Wiki已为最新!");
                        return;
                    }
                    conn=(HttpURLConnection) Util.updateDownloadUrl.openConnection();
                    byte[] jar=Util.streamToByteArray(conn.getInputStream());
                    FileOutputStream fos=new FileOutputStream("plugins\\"+result+".disabled");
                    fos.write(jar);
                    fos.flush();
                    getLogger().warning("检测到有新版本的Wiki，已下载至plugins文件夹下，请删除旧版Wiki并将最新版Wiki的.disabled后缀名删除，并重启Mirai。");
                    if(!optionPaneShown) {
                        JOptionPane.showMessageDialog(null, "检测到有新版本的Wiki\r\n已下载至plugins文件夹下\r\n请删除旧版Wiki并将最新版Wiki的.disabled后缀名删除\r\n并重启Mirai。", "Wiki自动更新", JOptionPane.INFORMATION_MESSAGE);
                        optionPaneShown=true;
                    }
                }catch (Exception e){
                    getLogger().error(e);
                }
            }
        },0,10*60*1000);
        loadData();
        Util.generateHelpImage();
        listener=getEventListener().subscribeAlways(GroupMessageEvent.class, groupMessageEvent -> {
            if(groupMessageEvent.getMessage().contentToString().toLowerCase().startsWith("wiki:")) {
                if(!looper.queue.isEmpty())
                    Util.sendMes(groupMessageEvent,"你的请求正在排队。前方有"+looper.queue.size()+"个请求。");
                looper.post(groupMessageEvent);
            }
            String content=Util.extractPlainText(groupMessageEvent).trim();
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
        if(!System.getProperty("os.name").toLowerCase().contains("win"))
            getLogger().warning("Detected non-Windows runtime. Please install font \"Microsoft YaHei\" so that Chinese characters are rendered properly.");
        GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] f=ge.getAllFonts();
        for(Font temp:f){
            if(Util.matchesAny(temp.getName(),"微软雅黑","msyh","Microsoft YaHei"))return;
        }
        getLogger().error("Font \"Microsoft YaHei\" not found in your system! Chinese characters will not be displayed normally!");
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
            getLogger().info("请求已加入队列。");
            queue.add(event);
            synchronized (lock){
                lock.notify();
            }
            if(looperThread.getState()== Thread.State.TERMINATED) {
                getLogger().error("Looper线程意外终止。尝试重启looper...");
                looperThread=new Thread(this);
                looperThread.start();
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
