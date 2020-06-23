package com.zjs;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * 提醒服务:当群内有人提出问题后过一段时间无人回答,则提醒他使用本插件.
 * (因为没人用)
 * */
public class NotificationService implements Future<MessageReceipt<Group>> {
    GroupMessageEvent src;
    long assignedTime;
    boolean cancelled=false;
    boolean executed=false;
    MessageReceipt<Group> receipt;
    private final Object lock=new Object();
    public NotificationService(long timeout, GroupMessageEvent src){
        this.src=src;
        assignedTime=timeout+System.currentTimeMillis();
    }
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if(executed)return false;
        else cancelled=true;
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return executed;
    }

    public void notifyWaitingThread(){
        synchronized (lock){
            lock.notifyAll();
        }
    }

    @Override
    public MessageReceipt<Group> get() throws InterruptedException, ExecutionException {
        if(!executed){
            synchronized (lock){
                lock.wait();
            }
        }
        return receipt;
    }

    @Override
    public MessageReceipt<Group> get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if(!executed){
            Thread.sleep(unit.convert(timeout,TimeUnit.MILLISECONDS));
            if(!executed)throw new TimeoutException("Timeout waiting for execution");
        }
        return receipt;
    }
}
