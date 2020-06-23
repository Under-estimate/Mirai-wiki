# Mirai-wiki
基于[mirai](https://github.com/mamoe/mirai)的QQ群内问答系统插件
Mirai Core 版本 `1.0.2`

## 使用方法
将插件放入plugins文件夹内即可使用。

## 命令列表
### 任何情况下都能够使用的指令
Wiki:Search + <关键词> 搜索有关问题
Wiki:Question 提出问题
Wiki:MyQuestion 查看自己提出的问题
Wiki:MyAnswer 查看自己回答过的问题
Wiki:Unsolved 查看本群中未解决的问题
### 在一定上下文中能够使用的指令
Wiki:Page + <页码> 跳转到指定页
Wiki:View + <序号> 查看指定的问题及回答
Wiki:Back 退出当前查看的问题(回到问题列表)
Wiki:Answer + <序号> 为指定的问题写回答
Wiki:Title + <标题> 为问题设置标题
Wiki:Text + <文本> 为问题/回答追加文本
Wiki:Image + [图片] 为问题/回答追加图片(不能是表情)
Wiki:Submit 提交问题/回答
Wiki:Abort 终止提出问题/写回答
Wiki:Delete 删除问题/回答
Wiki:Accept 接受回答并标记问题为"解决"
Wiki:Further 表示需要更多信息，将问题标记为"追问"
