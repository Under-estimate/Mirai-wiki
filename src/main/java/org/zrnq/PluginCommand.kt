package org.zrnq

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException
import kotlin.reflect.KProperty

@Suppress("RedundantSuspendModifier")
object PluginCommand : CompositeCommand(
    Wiki.INSTANCE,"wiki",
    description = "MiraiWiki相关指令"){
    private var MemberCommandSenderOnMessage.session by SessionDelegate()
    private fun MemberCommandSenderOnMessage.disposeSession(){
        R.sessions[group.id]?.remove(user.id)
    }
    private fun Session.State.toStateString() : String{
        return when(this){
            Session.State.Search_Question -> "搜索问题"
            Session.State.Write_Question -> "创建新问题"
            Session.State.My_Questions -> "查看\"我提出的问题\""
            Session.State.My_Answers -> "查看\"我回答过的问题\""
            Session.State.View_Unsolved -> "查看本群未解决的问题"
            Session.State.View_All -> "查看本群所有问题"
            Session.State.Write_Answer -> "为问题写解答"
            Session.State.Null -> "[上下文无效]"
        }
    }
    private fun MemberCommandSenderOnMessage.checkApplicability(acceptTheseStates : Boolean, vararg states : Session.State) : Boolean{
        if(acceptTheseStates xor (session.state in states) || session.state == Session.State.Null){
            Util.sendMes(fromEvent, "在${session.state.toStateString()}时不能执行该操作")
            return false
        }
        return true
    }
    private fun MemberCommandSenderOnMessage.checkApplicability(acceptTheseStates: Boolean, acceptViewDetail : Boolean, vararg states: Session.State) : Boolean{
        if(acceptViewDetail xor session.viewDetail){
            Util.sendMes(fromEvent,"${if(session.viewDetail) "" else "不" }在查看问题详细信息时不能执行该操作，或指令缺少[列表中的序号]参数")
            return false
        }
        return checkApplicability(acceptTheseStates, *states)
    }
    private fun MemberCommandSenderOnMessage.checkPermission(question : Question) : Boolean{
        if(question.questioner.id != user.id){
            Util.sendMes(fromEvent, "你不能在${question.questioner.name}提出的问题中执行该操作")
            return false
        }
        return true
    }
    private fun MemberCommandSenderOnMessage.checkPermission(answer : Answer) : Boolean{
        if(answer.id != user.id){
            Util.sendMes(fromEvent, "你不能对${answer.name}的回答执行该操作")
            return false
        }
        return true
    }
    @SubCommand
    @Description("搜索当前群聊中的有关问题")
    suspend fun MemberCommandSenderOnMessage.search(keyword : String){
        session.queryData=Util.search(keyword, group.id)
        if(session.queryData.size<=0){
            Util.sendMes(fromEvent, "什么都没找到...请尝试换一个关键词或者发起提问")
            disposeSession()
            return
        }
        session.state = Session.State.Search_Question
        session.text = keyword
        val image = Util.generateResultImage(session.queryData,0,
            "搜索结果",
            "关键词: $keyword",
            "用户: ${user.nameCardOrNick}")
        Util.sendMes(fromEvent,image)
    }
    @SubCommand
    @Description("创建新的问题")
    suspend fun MemberCommandSenderOnMessage.question(title : String){
        session.currentQuestion = Question()
        session.currentQuestion.title = title
        session.currentQuestion.questioner = Questioner(user.nameCardOrNick, user.id)
        session.state = Session.State.Write_Question
        Util.sendMes(fromEvent, "开始创建新的问题")
    }
    @SubCommand
    @Description("查看当前用户提出的问题列表")
    suspend fun MemberCommandSenderOnMessage.myquestion(){
        session.queryData = Util.myQuestions(group.id,user.id)
        if(session.queryData.size<=0){
            Util.sendMes(fromEvent, "你还没有提出过问题呢")
            disposeSession()
            return
        }
        session.state = Session.State.My_Questions
        val image = Util.generateResultImage(session.queryData, 0,
            "我提出的问题",
            "用户: ${user.nameCardOrNick}",
            "")
        Util.sendMes(fromEvent,image)
    }
    @SubCommand
    @Description("查看当前用户回答过的问题列表")
    suspend fun MemberCommandSenderOnMessage.myanswer(){
        session.queryData = Util.myAnswers(group.id,user.id)
        if(session.queryData.size<=0){
            Util.sendMes(fromEvent, "你还没有回答过问题呢")
            disposeSession()
            return
        }
        session.state = Session.State.My_Answers
        val image = Util.generateResultImage(session.queryData,0,
            "我回答过的问题",
            "用户: ${user.nameCardOrNick}",
            "")
        Util.sendMes(fromEvent,image)
    }
    @SubCommand
    @Description("查看本群未解决的问题")
    suspend fun MemberCommandSenderOnMessage.unresolved(){
        session.queryData = Util.unsolvedQuestions(group.id)
        if(session.queryData.size<=0){
            Util.sendMes(fromEvent, "现在还没有未解决的问题哦")
            disposeSession()
            return
        }
        session.state = Session.State.View_Unsolved
        val image = Util.generateResultImage(session.queryData,0,
            "本群未解决的问题",
            "用户: ${user.nameCardOrNick}",
            "")
        Util.sendMes(fromEvent,image)
    }
    @SubCommand
    @Description("查看本群的所有问题")
    suspend fun MemberCommandSenderOnMessage.all(){
        if(QuestionListHolder.INSTANCE.getListOf(group.id).size<=0){
            Util.sendMes(fromEvent, "本群还没有人提过问题")
            disposeSession()
            return
        }
        session.queryData = ArrayList()
        session.queryData = QuestionListHolder.INSTANCE.getListOf(group.id)
        session.state = Session.State.View_All
        val image = Util.generateResultImage(session.queryData,0,
            "本群所有问题",
            "用户: ${user.nameCardOrNick}",
            "")
        Util.sendMes(fromEvent,image)
    }
    @SubCommand
    @Description("显示版本信息")
    suspend fun MemberCommandSenderOnMessage.about(){
        Util.sendMes(fromEvent,"${R.name}版本${R.version}\n项目地址https://github.com/Under-estimate/Mirai-wiki")
    }
    @SubCommand
    @Description("在多页结果中翻页")
    suspend fun MemberCommandSenderOnMessage.page(page : Int){
        if(!checkApplicability(false,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        val lim = (session.queryData.size-1)/10
        if(page !in 0..lim){
            Util.sendMes(fromEvent,"给定的页码[$page]超出范围[0,$lim]")
            return
        }
        val image = when(session.state){
            Session.State.Search_Question -> Util.generateResultImage(session.queryData,page,"搜索结果","关键词: ${session.text}","用户: ${user.nameCardOrNick}")
            Session.State.My_Questions -> Util.generateResultImage(session.queryData,page,"我提出的问题","用户: ${user.nameCardOrNick}","")
            Session.State.My_Answers -> Util.generateResultImage(session.queryData,page,"我回答过的问题","用户: ${user.nameCardOrNick}","")
            Session.State.View_Unsolved -> Util.generateResultImage(session.queryData,page,"本群未解决的问题","用户: ${user.nameCardOrNick}","")
            Session.State.View_All ->  Util.generateResultImage(session.queryData,page,"本群所有问题","用户: ${user.nameCardOrNick}","")
            else -> throw IllegalStateException("An unexpected exception has occurred: Theoretically unreachable statement at command [page]")
        }
        Util.sendMes(fromEvent,image)
    }
    @SubCommand
    @Description("查看列表中指定问题的详细信息")
    suspend fun MemberCommandSenderOnMessage.view(item : Int){
        if(!checkApplicability(false,
                Session.State.Write_Answer,
                Session.State.Write_Question))
                    return
        if(item !in 0 until session.queryData.size){
            Util.sendMes(fromEvent,"给定的序号[$item]超出范围[0,${session.queryData.size})")
            return
        }
        session.currentQuestion = session.queryData[item]
        session.viewDetail = true
        val image = Util.generateDetailImage(session.queryData[item])
        Util.sendMes(fromEvent,image)
    }
    @SubCommand
    @Description("查看指定序号的图片")
    suspend fun MemberCommandSenderOnMessage.viewimage(imageId : Int){
        val image = SerializableImage.getImage(imageId)
        if(image == null) {
            Util.sendMes(fromEvent, PluginImageHolder.INSTANCE.getByteArray("missing"))
            return
        }
        Util.sendMes(fromEvent,image)
    }
    @SubCommand
    @Description("为列表中指定的问题写回答")
    suspend fun MemberCommandSenderOnMessage.answer(question : Int){
        if(!checkApplicability(false,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        if(question !in 0 until session.queryData.size){
            Util.sendMes(fromEvent,"给定的序号[$question]超出范围[0,${session.queryData.size})")
            return
        }
        session.currentQuestion = session.queryData[question]
        session.currentAnswer = Answer()
        session.currentAnswer.id = user.id
        session.currentAnswer.name = user.nameCardOrNick
        session.state = Session.State.Write_Answer
        Util.sendMes(fromEvent,"开始为问题[${session.currentQuestion.title}]写回答")
    }
    @SubCommand
    @Description("为刚刚查看过的问题写回答")
    suspend fun MemberCommandSenderOnMessage.answer(){
        if(!checkApplicability(false,true,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        session.currentAnswer = Answer()
        session.currentAnswer.id = user.id
        session.currentAnswer.name = user.nameCardOrNick
        session.state = Session.State.Write_Answer
        Util.sendMes(fromEvent,"开始为问题[${session.currentQuestion.title}]写回答")
    }
    @SubCommand
    @Description("为问题/回答追加文本")
    suspend fun MemberCommandSenderOnMessage.text(text : String){
        if(!checkApplicability(true,
            Session.State.Write_Answer,
            Session.State.Write_Question))
                return
        if(session.state == Session.State.Write_Question)
            session.currentQuestion.text = (session.currentQuestion.text ?: "") + text
        else
            session.currentAnswer.text = (session.currentAnswer.text ?: "") + text
        Util.sendMes(fromEvent,"文本追加成功")
    }
    @SubCommand
    @Description("为问题/回答追加图片")
    suspend fun MemberCommandSenderOnMessage.image(image : Image){
        if(!checkApplicability(true,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        val url = image.queryUrl()
        if(session.state == Session.State.Write_Question)
            session.currentQuestion.images.add(SerializableImage(url))
        else
            session.currentAnswer.images.add(SerializableImage(url))
        Util.sendMes(fromEvent,"图片追加成功")
    }
    @SubCommand
    @Description("提交问题/回答")
    suspend fun MemberCommandSenderOnMessage.submit(){
        if(!checkApplicability(true,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        if(session.state == Session.State.Write_Question){
            if(session.currentQuestion.text.isNullOrBlank()){
                Util.sendMes(fromEvent,"问题未设置文本，使用\"wiki text <文本>\"来追加文本")
                return
            }
            session.currentQuestion.questionId = PluginData.questionIdPointer++
            session.currentQuestion.groupId = group.id
            session.currentQuestion.questioner = Questioner(user.nameCardOrNick, user.id)
            QuestionListHolder.INSTANCE.RWAccessor(group.id)
            { list : ArrayList<Question> ->
                list.add(session.currentQuestion)
            }
            Util.sendMes(fromEvent, "问题提交成功")
        }else{
            if(session.currentAnswer.text.isNullOrBlank()){
                Util.sendMes(fromEvent, "回答未设置文本，使用\"wiki text <文本>\"来追加文本")
                return
            }
            session.currentQuestion.answererList.add(session.currentAnswer)
            if(user.id != session.currentQuestion.questioner.id)
            session.currentQuestion.requireFurtherInfo = false
            QuestionListHolder.INSTANCE.saveQuestions()
            Util.sendMes(fromEvent, "回答提交成功")
        }
        disposeSession()
    }
    @SubCommand
    @Description("中止写问题/回答")
    suspend fun MemberCommandSenderOnMessage.abort(){
        if(!checkApplicability(true,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        Util.sendMes(fromEvent, "已中止${session.state.toStateString()}")
        disposeSession()
    }
    @SubCommand
    @Description("删除列表中指定的问题(只能是你自己的)")
    suspend fun MemberCommandSenderOnMessage.deleteq(question : Int){
        if(!checkApplicability(false,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        if(question !in 0 until session.queryData.size){
            Util.sendMes(fromEvent, "给定的序号[$question]超出范围[0,${session.queryData.size})")
            return
        }
        val questionE = session.queryData[question]
        if(!checkPermission(questionE))
            return
        QuestionListHolder.INSTANCE.RWAccessor(group.id)
        { list : ArrayList<Question> ->
            list.remove(questionE)
        }
        disposeSession()
    }
    @SubCommand
    @Description("刚刚查看过的问题(只能是你自己的)")
    suspend fun MemberCommandSenderOnMessage.deleteq(){
        if(!checkApplicability(false,true,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        if(!checkPermission(session.currentQuestion))
            return
        QuestionListHolder.INSTANCE.RWAccessor(group.id)
        { list : ArrayList<Question> ->
            list.remove(session.currentQuestion)
        }
        disposeSession()
    }
    @SubCommand
    @Description("删除列表中指定问题下指定序号的回答(只能是你的回答)")
    suspend fun MemberCommandSenderOnMessage.deletea(question : Int, answer : Int){
        if(!checkApplicability(false,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        if(question !in 0 until session.queryData.size){
            Util.sendMes(fromEvent, "给定的序号[$question]超出范围[0,${session.queryData.size})")
            return
        }
        if(answer !in 0 until session.queryData[question].answererList.size){
            Util.sendMes(fromEvent, "给定的序号[$answer]超出范围[0,${session.queryData[question].answererList.size})")
            return
        }
        if(!checkPermission(session.queryData[question].answererList[answer]))
            return
        session.queryData[question].answererList.removeAt(answer)
        Util.sendMes(fromEvent, "删除回答成功")
    }
    @SubCommand
    @Description("删除刚刚查看的问题下指定序号的回答(只能是你的回答)")
    suspend fun MemberCommandSenderOnMessage.deletea(answer : Int){
        if(!checkApplicability(false,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        if(answer !in 0 until session.currentQuestion.answererList.size){
            Util.sendMes(fromEvent, "给定的序号[$answer]超出范围[0,${session.currentQuestion.answererList.size})")
            return
        }
        if(!checkPermission(session.currentQuestion.answererList[answer]))
            return
        session.currentQuestion.answererList.removeAt(answer)
        Util.sendMes(fromEvent, "删除回答成功")
    }
    @SubCommand
    @Description("采纳指定的回答(只能在你提出的问题中)")
    suspend fun MemberCommandSenderOnMessage.accept(answer : Int){
        if(!checkApplicability(false, true,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        if(answer !in 0 until session.currentQuestion.answererList.size){
            Util.sendMes(fromEvent, "给定的序号[$answer]超出范围[0,${session.currentQuestion.answererList.size})")
            return
        }
        if(!checkPermission(session.currentQuestion))
            return
        session.currentQuestion.answererList[answer].accepted = true
        session.currentQuestion.requireFurtherInfo = false
        Util.sendMes(fromEvent, "成功采纳了${session.currentQuestion.answererList[answer].name}的回答")
        QuestionListHolder.INSTANCE.saveQuestions()
    }
    @SubCommand
    @Description("标记指定问题需要更多信息(只能在你提出的问题中)")
    suspend fun MemberCommandSenderOnMessage.further(question : Int){
        if(!checkApplicability(false,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        if(question !in 0 until session.queryData.size){
            Util.sendMes(fromEvent, "给定的序号[$question]超出范围[0,${session.queryData.size})")
            return
        }
        val questionE = session.queryData[question]
        if(!checkPermission(questionE))
            return
        questionE.requireFurtherInfo = true
        Util.sendMes(fromEvent, "成功标记该问题为\"追问\"")
        QuestionListHolder.INSTANCE.saveQuestions()
    }
    @SubCommand
    @Description("标记刚刚查看过的问题需要更多信息(只能是你提出的问题)")
    suspend fun MemberCommandSenderOnMessage.further(){
        if(!checkApplicability(false, true,
                Session.State.Write_Answer,
                Session.State.Write_Question))
            return
        if(!checkPermission(session.currentQuestion))
            return
        session.currentQuestion.requireFurtherInfo = true
        Util.sendMes(fromEvent, "成功标记该问题为\"追问\"")
        QuestionListHolder.INSTANCE.saveQuestions()
    }
}

class SessionDelegate{
    operator fun getValue(thisRef: MemberCommandSenderOnMessage, property: KProperty<*>): Session {
        if(!R.sessions.containsKey(thisRef.group.id))
            R.sessions[thisRef.group.id] = HashMap()
        val map = R.sessions[thisRef.group.id]!!
        if(!map.containsKey(thisRef.user.id))
            map[thisRef.user.id] = Session(thisRef.fromEvent)
        return map[thisRef.user.id]!!
    }

    operator fun setValue(thisRef: MemberCommandSenderOnMessage, property: KProperty<*>, value: Session) {
        throw UnsupportedOperationException("Modifying session reference is not allowed.")
    }
}