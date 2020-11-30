// 作者：李宁（蒙娜丽宁)
// Copyright © 2020 Lining. All rights reserved.
// 更多关于鸿蒙的精彩内容，请关注微信公众号【极客起源】
// 项目完整教学视频：https://space.bilibili.com/477001733
// 更多关于鸿蒙的精彩视频请关注我的B站：https://space.bilibili.com/477001733

package com.harmonyos.onlineedict.common;


import ohos.app.AbilityContext;
import ohos.data.DatabaseHelper;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;
import ohos.data.resultset.ResultSet;
import ohos.global.resource.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

class AsyncSearchWord extends Thread {
    private String word;
    private RdbStore store;
    private SearchWordCallback callback;

    public AsyncSearchWord(String word,RdbStore store, SearchWordCallback callback) {
        this.word = word;
        this.store = store;
        this.callback = callback;
    }

    @Override
    public void run() {
        try{
            // 获取搜索结果（HTML形式）
            Document doc = Jsoup.connect("https://www.iciba.com/word?w=" + word).get();
            Elements ulElements = doc.getElementsByClass("Mean_part__1RA2V");
            // 将网络单词信息保存到本地的SQL语句
            String insertSQL = "insert into t_words(word, type, meanings) values(?,?,?);";
            List<WordData> wordDataList = new ArrayList<>();
            for(Element ulElement: ulElements) {
                // 获取单词的每一个词性和中文解释
                Elements liElements = ulElement.getElementsByTag("li");
                // 对每一个词性进行迭代
                for(Element liElement:liElements) {
                    WordData wordData = new WordData();
                    Elements iElements = liElement.getElementsByTag("i");
                    for(Element iElement:iElements) {
                        // 获取当前词性
                        wordData.type = iElement.text();
                        break;
                    }
                    Elements divElements = liElement.getElementsByTag("div"); // 获取中文解释
                    for(Element divElement:divElements) {
                        wordData.meanings = divElement.text();   // 提取词性对应的中文解释
                        break;
                    }
                    wordDataList.add(wordData);
                    store.executeSql(insertSQL,new String[]{word,wordData.type,wordData.meanings});
                }
                break;
            }
            if(callback != null) {
                callback.onResult(wordDataList);
            }


        } catch (Exception e) {

        }
    }
}
public class MyDict {
    private AbilityContext context;
    private File dictPath;
    private File dbPath;
    private RdbStore store;
    private StoreConfig config = StoreConfig.newDefaultConfig("dict.sqlite");
    private static final RdbOpenCallback callback = new RdbOpenCallback() {
        @Override
        public void onCreate(RdbStore rdbStore) {

        }

        @Override
        public void onUpgrade(RdbStore rdbStore, int i, int i1) {

        }
    };
    public MyDict(AbilityContext context) {
        this.context = context;
        dictPath = new File(context.getDataDir().toString() + "/MainAbility/databases/db");
        if(!dictPath.exists()){
            dictPath.mkdirs();
        }
        dbPath = new File(Paths.get(dictPath.toString(), "dict.sqlite").toString());

    }
    private void extractDB() throws IOException {
        // 读取dict.sqlite文件的字节流
        Resource resource = context.getResourceManager().getRawFileEntry("resources/rawfile/dict.sqlite").openRawFile();
        if(dbPath.exists()) {
            dbPath.delete();
        }

        FileOutputStream fos = new FileOutputStream(dbPath);
        byte[] buffer = new byte[4096];
        int count = 0;
        while((count = resource.read(buffer)) >= 0) {
            fos.write(buffer, 0 ,count);
        }
        resource.close();
        fos.close();

    }
    public void init() throws IOException {
        extractDB();
        // 打开数据库
        DatabaseHelper helper = new DatabaseHelper(context);
        store = helper.getRdbStore(config,1,callback,null);
    }
    // 搜索本地词库
    public ArrayList<WordData> searchLocalDict(String word) {
        word = word.toLowerCase();
        String[] args = new String[]{word};
        ResultSet resultSet = store.querySql("select * from t_words where word=?",args);
        ArrayList<WordData> result = new ArrayList<>();
        while(resultSet.goToNextRow()) {
            WordData wordData = new WordData();
            wordData.type = resultSet.getString(2);  // 获取type字典的值
            wordData.meanings = resultSet.getString(3);// 获取中文解释
            result.add(wordData);
        }
        resultSet.close();
        return result;
    }

    // 异步搜索网络词典
    public void searchWebDict(String word, SearchWordCallback callback) {
        word = word.toLowerCase();
        // 异步搜索
        new AsyncSearchWord(word,store,callback).start();
    }
}
