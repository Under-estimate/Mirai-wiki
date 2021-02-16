# Mirai-wiki
基于[mirai](https://github.com/mamoe/mirai)的QQ群内问答系统插件  
Mirai Core 版本 `2.3.2`  
Mirai Console 版本 `2.3.2`  

> 关于Linux运行环境  
> 如果你正在使用Linux而不是Windows来运行Mirai,请确保Microsoft YaHei字体(msyh.ttc)已安装到你的系统中，否则汉字可能不会被正常显示。  

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

