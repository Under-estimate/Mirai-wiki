package org.zrnq

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

object Wiki : KotlinPlugin(
    JvmPluginDescriptionBuilder("org.zrnq.wiki", R.version)
        .author("ZRnQ")
        .name(R.name)
        .info("QQ群内问答系统")
        .build()
) {
    lateinit var adminPerm : Permission
    override fun PluginComponentStorage.onLoad() {
        R.logger = logger
        R.logger.info(R.name + R.version + "正在预加载")
    }

    override fun onEnable() {
        R.logger.info(R.name + R.version + "正在加载")
        PluginData.reload()
        PluginConfig.reload()
        CommandManager.registerCommand(PluginCommand, true)
        adminPerm = PermissionService.INSTANCE.register(permissionId("admin"), "问答系统管理员权限", parentPermission)
        R.systemCheck()
        R.initResources()
        Util.initImageStub()
    }

    override fun onDisable() {
        R.logger.info(R.name + R.version + "正在停止")
        CommandManager.unregisterCommand(PluginCommand)
    }
}