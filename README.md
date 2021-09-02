# Mirai-wiki
[![mirai](https://img.shields.io/badge/mirai-v2.7.0-brightgreen)](https://github.com/mamoe/mirai )  
基于[mirai](https://github.com/mamoe/mirai )的QQ群内问答系统插件  

> 关于Linux运行环境  
> 如果你正在使用Linux而不是Windows来运行Mirai,请确保Microsoft YaHei字体(msyh.ttc)已安装到你的系统中，否则汉字可能不会被正常显示。  

## 如何安装
1. 在[这里](https://github.com/Under-estimate/Mirai-wiki/releases/ )下载最新的插件文件。
2. 将插件文件放入[mirai-console](https://github.com/mamoe/mirai-console )运行生成的`plugins`文件夹中。
3. 如果您还未安装[chat-command](https://github.com/project-mirai/chat-command )插件(添加聊天环境中使用命令的功能)，你可以从下面选择一种方法安装此插件：
> 1. 如果您正在使用[Mirai Console Loader](https://github.com/iTXTech/mirai-console-loader )来启动[mirai-console](https://github.com/mamoe/mirai-console )，您可以运行以下命令来安装[chat-command](https://github.com/project-mirai/chat-command )插件：  
> `./mcl --update-package net.mamoe:chat-command --channel stable --type plugin`
> 2. 如果您没有使用[Mirai Console Loader](https://github.com/iTXTech/mirai-console-loader )，您可以在[这里](https://github.com/project-mirai/chat-command/releases )下载最新的[chat-command](https://github.com/project-mirai/chat-command )插件文件，并将其一同放入[mirai-console](https://github.com/mamoe/mirai-console )运行生成的`plugins`文件夹中。
4. 启动[mirai-console](https://github.com/mamoe/mirai-console )之后，在后台命令行输入以下命令授予相关用户使用此插件命令的权限：
> - 如果您希望所有群的群员都可以使用此插件，请输入：  
> `/perm grant m* org.zrnq.wiki:*`  
> - 如果您希望只授予某一个群的群员使用此插件的权限，请输入：  
> `/perm grant m<QQ群号>.* org.zrnq.wiki:*`
> - 如果您希望只授予某一个群的特定群员使用此插件的权限，请输入：  
> `/perm grant m<QQ群号>.<群员QQ号> org.zrnq.wiki:*`
> - 如果你希望了解更多高级权限设置方法，请参阅[mirai-console的权限文档](https://github.com/mamoe/mirai-console/blob/master/docs/Permissions.md )
5. 安装完成。
## 权限列表
*有关权限部分的说明，参见[mirai-console的权限文档](https://github.com/mamoe/mirai-console/blob/master/docs/Permissions.md )*  
根权限： `org.zrnq.wiki:*`  
基本操作权限： `org.zrnq.wiki:command.wiki`
- 包含所有命令执行的权限。
## 命令列表
*提示: <尖括号>中的参数必填，(圆括号)中的参数可以不填*
### 任何情况下都能够使用的指令
wiki search <关键词> 搜索有关问题  
wiki question <标题> 提出新的问题  
wiki myquestion 查看自己提出的问题列表  
wiki myanswer 查看自己回答过的问题列表  
wiki unsolved 查看本群中未解决的问题列表  
wiki all 查看本群所有问题列表  
wiki about 查看本插件的相关信息  
wiki viewimage <序号> 查看指定序号的图片原图
### 在一定上下文中能够使用的指令
wiki page <页码> 跳转到指定页  
wiki view <序号> 查看列表中指定问题的详细信息  
wiki answer (序号) 为指定的问题写回答。若不加序号参数，则为刚刚查看过详细信息的问题写回答。  
wiki text <文本> 为问题/回答添加文本  
wiki image <图片> 为问题/回答添加图片(不能是表情)  
wiki submit 提交问题/回答  
wiki abort 终止提出问题/写回答  
wiki deleteq (序号) 删除列表中指定的问题。若不加序号参数，则删除刚刚查看过详细信息的问题。删除的问题必须是用户自己提出的。  
wiki deletea (问题序号) <回答序号> 删除列表中指定问题下的指定回答。若不加问题序号参数，则删除刚刚查看过详细信息的问题下指定序号的回答。删除的回答必须是用户自己提供的。  
wiki accept <回答序号> 接受刚刚查看过详细信息的问题中的指定回答并标记问题为"解决"  
wiki further 将刚刚查看过详细信息的问题标记为"追问"  

## FAQ
### Q: 后台命令行或私聊机器人输入指令后提示"参数错误"
A: 目前只支持在QQ群中发送命令，因为每次提问/回答需要记录执行该操作的用户。提示参数错误是因为插件限制了命令执行者只能为QQ群成员。
### Q: 在QQ群中发送命令没反应
A: 请检查是否安装了[chat-command](https://github.com/project-mirai/chat-command )插件，如果没有安装请看[这里](#如何安装 )