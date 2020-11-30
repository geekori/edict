/*
作者：李宁（蒙娜丽宁)
Copyright © 2020 Lining. All rights reserved.
更多关于鸿蒙的精彩内容，请关注微信公众号【极客起源】
更多关于鸿蒙的精彩视频请关注我的B站：https://space.bilibili.com/477001733
 */
package com.harmonyos.onlineedict;

import com.harmonyos.onlineedict.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {

        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
    }
}
