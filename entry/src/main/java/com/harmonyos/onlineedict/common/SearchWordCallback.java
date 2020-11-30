/*
作者：李宁（蒙娜丽宁)
Copyright © 2020 Lining. All rights reserved.
更多关于鸿蒙的精彩内容，请关注微信公众号【极客起源】
更多关于鸿蒙的精彩视频请关注我的B站：https://space.bilibili.com/477001733
 */
package com.harmonyos.onlineedict.common;

import java.util.List;

public interface SearchWordCallback {
    void onResult(List<WordData> result);
}
