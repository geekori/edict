// 作者：李宁（蒙娜丽宁)
// Copyright © 2020 Lining. All rights reserved.
// 更多关于鸿蒙的精彩内容，请关注微信公众号【极客起源】
// 项目完整教学视频：https://space.bilibili.com/477001733
// 更多关于鸿蒙的精彩视频请关注我的B站：https://space.bilibili.com/477001733

package com.harmonyos.onlineedict.slice;

import com.harmonyos.onlineedict.ResourceTable;
import com.harmonyos.onlineedict.common.MyDict;
import com.harmonyos.onlineedict.common.SearchWordCallback;
import com.harmonyos.onlineedict.common.WordData;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.system.DeviceInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice {
    public static final int SEARCH_RESULT = 100;
    private MyDict myDict;
    private TextField textfieldWord;
    private Text textSearchResult;
    private Image imageSearch;
    private Image image;
    private class SearchWordCallbackImpl implements SearchWordCallback {
        @Override
        public void onResult(List<WordData> result) {
            if(DeviceInfo.getDeviceType().equals("wearable")) {
                showSearchResult(result);
            } else {
                EventRunner runner = EventRunner.getMainEventRunner();
                MyEventHandler handler = new MyEventHandler(runner, result);
                handler.sendEvent(SEARCH_RESULT);
                runner = null;
            }
        }
    }
    public class MyEventHandler extends EventHandler {
        private List<WordData> wordDataList;

        public MyEventHandler(EventRunner runner, List<WordData> wordDataList)  {
            super(runner);
            this.wordDataList = wordDataList;
        }

        @Override
        protected void processEvent(InnerEvent event) {
            super.processEvent(event);
            if(event == null) {
                return;
            }
            int eventId = event.eventId;
            switch (eventId) {
                case  SEARCH_RESULT: {
                    if(wordDataList.size() == 0) {
                        textSearchResult.setText("单词没有查到，请确认单词是否输入错误！");
                    } else {
                        textSearchResult.setText("");
                        for(WordData wordData:wordDataList) {
                            textSearchResult.append(wordData.type + " " + wordData.meanings + "\r\n");
                        }
                    }
                    break;
                }
            }
        }
    }
    // 用于在智能手表中显示搜索结果的方法
    public void showSearchResult(List<WordData> result) {
        Intent intent = new Intent();
        ArrayList<String> typeList = new ArrayList<>();
        ArrayList<String> meaningList = new ArrayList<>();
        for(WordData wordData:result) {
            typeList.add(wordData.type);
            meaningList.add(wordData.meanings);
        }
        intent.setStringArrayListParam("typeList", typeList);
        intent.setStringArrayListParam("meaningList", meaningList);
        present(new WearableSearchResultAbilitySlice(),intent);
    }
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        if(DeviceInfo.getDeviceType().equals("wearable")) {
            super.setUIContent(ResourceTable.Layout_ability_main_wearable);
        } else {
            super.setUIContent(ResourceTable.Layout_ability_main);
        }

        myDict = new MyDict(this);
        try{
            myDict.init();
        }catch (IOException e) {
            terminateAbility();
        }

        textfieldWord = (TextField)findComponentById(ResourceTable.Id_textfield_word);

        textSearchResult = (Text)findComponentById(ResourceTable.Id_text_search_result);

        image = (Image)findComponentById(ResourceTable.Id_image);
        imageSearch = (Image)findComponentById(ResourceTable.Id_image_search);
        if(imageSearch != null) {
            imageSearch.setClickable(true);
            imageSearch.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component component) {
                    if(DeviceInfo.getDeviceType().equals("tv")) {
                        image.setVisibility(Component.HIDE);
                        textSearchResult.setVisibility(Component.VISIBLE);
                    }
                    ArrayList<WordData> result = myDict.searchLocalDict(textfieldWord.getText());
                    if(result.size() > 0) {
                        if(DeviceInfo.getDeviceType().equals("wearable")) {
                            showSearchResult(result);
                        } else {
                            textSearchResult.setText("");
                            // 输出查询结果
                            for (WordData wordData : result) {
                                textSearchResult.append(wordData.type + " " + wordData.meanings + "\r\n");
                            }
                        }

                    } else {
                        if(DeviceInfo.getDeviceType().equals("tv")) {
                            textSearchResult.setText("本地词库没有查到此单词，正常查找网络词库...");
                        }
                        myDict.searchWebDict(textfieldWord.getText(),new SearchWordCallbackImpl());
                    }
                }
            });
        }

    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
