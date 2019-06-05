package com.mingqi.local.userservice.gc;

import com.mingqi.local.userservice.dto.GroupUserInfo;
import org.assertj.core.util.Lists;

import java.util.List;

public class GCDemoTest {

    public static void main(String[] args) {
        for (int i = 0; i < 200000; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            instanceDemo();
        }
    }

    private static void instanceDemo() {
        GroupUserInfo groupUserInfo = new GroupUserInfo(1L, "name", "avatar", 1);
        groupUserInfo.setUid(1L);
    }
}
