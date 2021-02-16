package org.zrnq;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class QuestionListHolder {
    private HashMap<Long, ArrayList<Question>> questionList;
    public static final QuestionListHolder INSTANCE = getInstance();
    private static File storage;
    private QuestionListHolder(){

    }
    @SuppressWarnings("unchecked")
    private static QuestionListHolder getInstance(){
        QuestionListHolder holder = new QuestionListHolder();
        storage = Wiki.INSTANCE.resolveDataFile("questions.bin");
        if(storage.exists()){
            try{
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storage));
                holder.questionList = (HashMap<Long, ArrayList<Question>>) ois.readObject();
            }catch (Exception e){
                R.logger.error("读取问题列表失败，正在使用空的问题列表。",e);
                holder.questionList = new HashMap<>();
            }
        }else{
            holder.questionList = new HashMap<>();
        }
        return holder;
    }
    public ArrayList<Question> getListOf(long group){
        if(!questionList.containsKey(group))
            questionList.put(group,new ArrayList<>());
        return questionList.get(group);
    }
    public void RWAccessor(long group, Consumer<ArrayList<Question>> rwAction){
        ArrayList<Question> list = getListOf(group);
        rwAction.accept(list);
        saveQuestions();
    }
    public void saveQuestions(){
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storage));
            oos.writeObject(questionList);
        }catch (Exception e){
            R.logger.error("保存问题列表失败。",e);
        }
    }
}
