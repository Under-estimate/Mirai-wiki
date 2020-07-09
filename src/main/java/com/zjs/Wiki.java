package com.zjs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.mamoe.mirai.console.MiraiConsole;
import net.mamoe.mirai.console.command.BlockingCommand;
import net.mamoe.mirai.console.command.Command;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.JCommandManager;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.utils.PlatformLogger;
import net.mamoe.mirai.utils.SimpleLogger;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class Wiki extends com.zjs.UpdatablePlugin{
    HashMap<Long, ArrayList<Session>> sessions;
    Listener<GroupMessageEvent> listener=null;
    Listener<MemberJoinEvent> join=null;
    Looper looper;
    Thread looperThread;
    NotificationServiceProvider provider=null;
    @Override
    public void onEnable(){
        Util.logger=new SimpleLogger("Plugin Wiki",(logPriority, s, throwable) -> {
            MiraiConsole.frontEnd.pushLog(logPriority,"[Wiki]",0,s);
            if(throwable!=null) {
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                throwable.printStackTrace(new PrintStream(bos));
                MiraiConsole.frontEnd.pushLog(logPriority, "[Wiki]",0, new String(bos.toByteArray()));
            }
            return null;
        });
        Util.logger.info("Wiki is being enabled.");

        if(!Util.dataFolderLocation.exists()) {
            Util.logger.warning("Data Folder不存在，正在创建...");
            if(Util.dataFolderLocation.mkdirs())
                Util.logger.info("Data Folder创建成功。");
            else
                Util.logger.error("Data Folder创建失败。");
        }

        loadConfig();
        sessions=new HashMap<>();
        looper=new Looper();
        looperThread=new Thread(looper);
        looperThread.start();

        loadData();
        Util.generateHelpImage();
        listener=getEventListener().subscribeAlways(GroupMessageEvent.class, groupMessageEvent -> {
            JsonArray arr=(JsonArray)Util.config.getAs(Config.GROUP_BLACKLIST);
            Iterator<JsonElement> it=arr.iterator();
            JsonElement temp;
            while(it.hasNext()){
                temp=it.next();
                if(groupMessageEvent.getGroup().getId()==temp.getAsLong())
                    return;
            }
            if(groupMessageEvent.getMessage().contentToString().toLowerCase().startsWith("wiki ")) {
                if(!looper.queue.isEmpty())
                    Util.sendMes(groupMessageEvent,"你的请求正在排队。前方有"+looper.queue.size()+"个请求。");
                looper.post(groupMessageEvent);
            }
            if((boolean)Util.config.getAs(Config.QUESTION_NOTIFICATION)) {
                String content = Util.extractPlainText(groupMessageEvent).trim();
                arr = (JsonArray) Util.config.getAs(Config.MEMBER_BLACKLIST);
                it = arr.iterator();
                boolean match = false;
                while (it.hasNext()) {
                    temp = it.next();
                    match = match || groupMessageEvent.getSender().getId() == temp.getAsLong();
                }
                if (!match) {
                    provider.cancelAllMatching(groupMessageEvent.getGroup().getId());
                    if (!(Util.containsAny(content, "没什么") && ! Util.matchesAny(content, "?", "？")) &&
                            Util.containsAny(content, "?", "？", "吗", "呢", "怎么", "如何", "什么")) {
                        provider.post(new NotificationService(120 * 1000, groupMessageEvent));
                    }
                }
            }
        });
        join=getEventListener().subscribeAlways(MemberJoinEvent.class,event->{
            if((boolean)Util.config.getAs(Config.JOIN_NOTIFICATION))
                event.getGroup().sendMessage(new At(event.getMember()).plus("欢迎进群~\r\n\r\n" +
                        "本群问答系统已上线，回复Wiki Help获取使用帮助。"));});
        JCommandManager.getInstance().register(this, new BlockingCommand("wiki", new ArrayList<>(),"All wiki commands.","/wiki reloadConfig - 重载配置文件。") {
            @Override
            public boolean onCommandBlocking(@NotNull CommandSender commandSender, @NotNull List<String> list) {
                if(list.size()<1){
                    commandSender.sendMessageBlocking("缺少参数。");
                    return false;
                }
                return list.get(0).equalsIgnoreCase("reloadConfig");
            }
        });
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
                Util.logger.error("Data corrupted.", e);
                Util.questions = new HashMap<>();
            }
        }

        if(!System.getProperty("os.name").toLowerCase().contains("win"))
            Util.logger.warning("Detected non-Windows runtime. Please install font \"Microsoft YaHei\" so that Chinese characters are rendered properly.");
        GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] f=ge.getAllFonts();
        for(Font temp:f){
            if(Util.matchesAny(temp.getName(),"微软雅黑","msyh","Microsoft YaHei"))return;
        }
        Util.logger.error("Font \"Microsoft YaHei\" not found in your system! Chinese characters will not be displayed normally!");
    }
    @Override
    public void onDisable(){
        looper.stop();
        provider.stop();
        listener.complete();
        if(join!=null)
            join.complete();
        JCommandManager.getInstance().unregister("wiki");
    }
    @Override
    public void onCommand(@NotNull Command command, @NotNull CommandSender sender, @NotNull List<String> args){
        if(args.get(0).equalsIgnoreCase("reloadConfig")){
            sender.sendMessageBlocking("重载配置文件...");
            loadConfig();
            sender.sendMessageBlocking("重载完成!");
        }
    }
    /**
     * 加载配置文件。
     * */
    private void loadConfig(){
        if(!Util.configLocation.exists()){
            Util.logger.warning("Config file does not exist, creating one with default config...");
            Util.config=new Config();
            Util.config.save(Util.configLocation);
        }else{
            Util.config=new Config(Util.configLocation);
        }
        if((boolean)Util.config.getAs(Config.QUESTION_NOTIFICATION)) {
            provider = new NotificationServiceProvider();
            new Thread(provider).start();
        }else{
            if(provider!=null)
                provider.stop();
            provider=null;
        }
        try {
            if(Util.config.getAs(Config.RESULT_BACKGROUND).equals("default"))
                Util.bgImage = ImageIO.read(this.getClass().getResourceAsStream("/bg.jpeg"));
            else
                Util.bgImage=ImageIO.read(new File("plugins\\Wiki\\"+Util.config.getAs(Config.RESULT_BACKGROUND)));
            if(Util.config.getAs(Config.HELP_BACKGROUND).equals("default"))
                Util.helpImage = ImageIO.read(this.getClass().getResourceAsStream("/help.jpg"));
            else
                Util.helpImage=ImageIO.read(new File("plugins\\Wiki\\"+Util.config.getAs(Config.HELP_BACKGROUND)));
        } catch (Exception e) {
            Util.logger.error("Failed to load background image.",e);
        }
        Util.generateHelpImage();
    }
    @Override
    public String getVersion(){
        return Util.VERSION;
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
            Util.logger.info("请求已加入队列。");
            queue.add(event);
            synchronized (lock){
                lock.notify();
            }
            if(looperThread.getState()== Thread.State.TERMINATED) {
                Util.logger.error("Looper线程意外终止。尝试重启looper...");
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
