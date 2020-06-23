package com.zjs;

import java.util.ArrayList;

/**
 * 运行提醒服务的线程.
 * */
public class NotificationServiceProvider implements Runnable{
    ArrayList<NotificationService> notificationList=new ArrayList<>();
    private final Object lock=new Object();
    public boolean stop=false;
    @Override
    public void run() {
        while(!stop) {
            if (notificationList.isEmpty()) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                } catch (Exception e) {
                    Util.logger.error(e);
                }
            }
            if(stop)break;
            if(notificationList.isEmpty())continue;
            long sleepPeriod = Long.MAX_VALUE;
            long current = System.currentTimeMillis();
            ArrayList<NotificationService> toBeRemoved = new ArrayList<>();
            for (NotificationService service : notificationList) {
                if (service.assignedTime < current) {
                    toBeRemoved.add(service);
                    continue;
                }
                sleepPeriod = Math.min(service.assignedTime - current, sleepPeriod);
            }
            for (NotificationService service : toBeRemoved) {
                if (service.cancelled) continue;
                execute(service);
                notificationList.remove(service);
                service.executed = true;
                service.notifyWaitingThread();
            }
            if(sleepPeriod!=Long.MAX_VALUE){
                try{
                    Thread.sleep(sleepPeriod);
                }catch (Exception e){
                    Util.logger.error(e);
                }
            }
        }
    }
    private void execute(NotificationService notify){
        notify.src.getGroup().sendMessage("有问题但是没人回答?试试群内问答系统吧~发送\"Wiki:Help\"获取使用帮助。");
    }
    public void post(NotificationService notify){
        notificationList.add(notify);
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
    public void cancelAllMatching(long groupId){
        ArrayList<NotificationService> toBeRemoved = new ArrayList<>();
        for(NotificationService service:notificationList){
            if(service.src.getGroup().getId()==groupId)toBeRemoved.add(service);
        }
        for (NotificationService service : toBeRemoved) {
            notificationList.remove(service);
        }
    }
}
