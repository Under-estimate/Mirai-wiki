package org.zrnq;

import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import org.jetbrains.annotations.NotNull;

public final class Wiki extends JavaPlugin {
    public static final Wiki INSTANCE=new Wiki();
    private Wiki(){
        super(new JvmPluginDescriptionBuilder("org.zrnq.wiki","2.0.0")
                .author("ZRnQ")
                .name(R.name)
                .info("QQ群内问答系统")
                .build());
    }
    @Override
    public void onLoad(@NotNull PluginComponentStorage storage) {
        R.logger=getLogger();
        R.logger.info(R.name+R.version+"正在预加载");
    }
    @Override
    public void onEnable(){
        R.logger.info(R.name+R.version+"正在加载");
        reloadPluginData(PluginData.INSTANCE);
        reloadPluginConfig(PluginConfig.INSTANCE);
        CommandManager.INSTANCE.registerCommand(PluginCommand.INSTANCE,true);
        R.systemCheck();
        R.initResources();
        Util.initImageStub();
    }
    @Override
    public void onDisable(){
        R.logger.info(R.name+R.version+"正在停止");
        CommandManager.INSTANCE.unregisterCommand(PluginCommand.INSTANCE);
    }
}
