package org.zrnq

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object PluginData : AutoSavePluginData("MiraiWikiData"){
    var questionIdPointer : Int by value(0)
    var imageIdPointer : Int by value(0)
}
object PluginConfig : AutoSavePluginConfig("MiraiWikiConfig"){
    var queryResultBg : String by value("")
}