// 作者：李宁（蒙娜丽宁)
// Copyright © 2020 Lining. All rights reserved.
// 更多关于鸿蒙的精彩内容，请关注微信公众号【极客起源】
// 项目完整教学视频：https://space.bilibili.com/477001733
// 更多关于鸿蒙的精彩视频请关注我的B站：https://space.bilibili.com/477001733

package com.harmonyos.onlineedict.slice;

import com.harmonyos.onlineedict.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Text;

import java.util.ArrayList;

public class WearableSearchResultAbilitySlice extends AbilitySlice {
    private Text textSearchResult;
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_wearable_search_result);
        textSearchResult = (Text)findComponentById(ResourceTable.Id_text_search_result);
        if(textSearchResult != null) {
            textSearchResult.setText("");
            ArrayList<String> typeList = intent.getStringArrayListParam("typeList");
            ArrayList<String> meaningList = intent.getStringArrayListParam("meaningList");
            for(int i = 0; i < typeList.size();i++) {
                textSearchResult.append(typeList.get(i) + " " + meaningList.get(i) + "\r\n") ;
            }
            if(typeList.size() == 0) {
                textSearchResult.setText("单词没有查到，请确认单词是否输入错误！");
            }
        }
    }
}
