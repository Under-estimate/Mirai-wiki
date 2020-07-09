# Mirai-wiki
基于[mirai](https://github.com/mamoe/mirai)的QQ群内问答系统插件  
Mirai Core 版本 `1.0-RC2-1`~`1.1.0`  
Mirai Console 版本 `0.5.2`  
**注意: 暂不支持 Mirai Console `1.0-dev-1` 版本，因为它还不是一个稳定的版本。你可以尝试更改旧版本console的文件名中的版本号来避免版本更新。**

## 使用方法
将[Wiki-wrapper](http://20bf488.nat123.cc:25547/download?app=wikiwrapper) 放入plugins文件夹中即可使用。

> 关于Linux运行环境  
> 如果你正在使用Linux而不是Windows来运行Mirai,请确保Microsoft YaHei字体(msyh.ttc)已安装到你的系统中，否则汉字可能不会被正常显示。  
  
> 关于自动更新  
> 当版本更新时，Wiki-wrapper会自动下载安装最新版本。更新方式为热更新，无需重启mirai。

## 命令列表
### 任何情况下都能够使用的指令
Wiki Search <关键词> 搜索有关问题  
Wiki Question (标题) 提出问题  
Wiki MyQuestion 查看自己提出的问题  
Wiki MyAnswer 查看自己回答过的问题  
Wiki Unsolved 查看本群中未解决的问题  
Wiki All 查看本群所有问题  
Wiki About 查看本插件的相关信息  
Wiki Help (主题) 获取帮助
### 在一定上下文中能够使用的指令
Wiki Page <页码> 跳转到指定页  
Wiki View <序号> 查看指定的问题及回答  
Wiki Back 退出当前查看的问题(回到问题列表)  
Wiki Answer <序号> 为指定的问题写回答  
Wiki Title <标题> 为问题设置标题  
Wiki Text <文本> 为问题/回答添加文本  
Wiki Image [图片] 为问题/回答添加图片(不能是表情)  
Wiki Submit 提交问题/回答  
Wiki Abort 终止提出问题/写回答  
Wiki Delete 删除问题/回答  
Wiki Accept 接受回答并标记问题为"解决"  
Wiki Further 表示需要更多信息，将问题标记为"追问"  
### 管理员(manager)指令
/wiki reloadConfig 在无需重启mirai的情况下重载配置文件。

## 获取插件jar文件
请下载[Wiki-wrapper](http://20bf488.nat123.cc:25547/download?app=wikiwrapper) ，将其放入plugins文件夹中，然后启动mirai，Wiki将会自动被下载安装。
