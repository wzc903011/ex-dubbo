package com.mingqi.local.userservice.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupUserInfo implements Serializable {
    private Long uid;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别：男1，女2，0为未知
     */
    private Integer gender;

    /**
     * 预测性别：男1，女2，-1为未知
     */
    private Integer predictGender;

    /**
     * 购买力：由低到高 1-5，-1为未知
     */
    private Integer buyingPower;

    /**
     * 购买力：0-低，1-高，-1为未知
     */
    private Integer consumeLevel;

    /**
     * 年龄：0:小于18岁，1：18-25，2：25-35，3：大于35，-1为未知
     */
    private Integer age;

    /**
     * icon偏好：0-未分清，1-活动偏好，2-购物偏好，-1为未知
     */
    private Integer iconPreference;

    /**
     * 是否新用户：0-不是，1-是，-1为未知
     */
    private Integer isNewUser;

    /**
     * 普通订单数量（有过非抽奖及部分活动下单），-1为未知
     */
    private Integer hasNormalOrder;

    /**
     * 孕产人群标签：1-孕产人群，-1为未知
     */
    private Integer isPregnant;

    /**
     * 用户人群标签信息
     * 形如: "abg1,sut0,mcg1"
     */
    private String groupList;

    public GroupUserInfo(Long uid, String nicName, String avatar, Integer gender) {
        this.uid = uid;
        this.nickname = nicName;
        this.avatar = avatar;
        this.gender = gender;
        this.predictGender = gender;
        this.buyingPower = 1;
        this.consumeLevel = 1;
        this.age = 20;
        this.iconPreference = 1;
        this.hasNormalOrder = 1;
        this.isNewUser = 1;
        this.isPregnant =1;
        this.groupList = "testtesttesteeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeafbsfnsbfnmsbfnsdmbsmnfbmsnfbmsnbfmnsfnmwbenfbwkjefjkwebfjwbefjkbwejkfbjkwebfjwbekfbwjkebfkwbefbwkejfbkwkjwebfkwjefjwbfkjwbefkw";

    }

    public GroupUserInfo(){}

    public static GroupUserInfo getByUid(Long uid) {
        GroupUserInfo groupUserInfo = new GroupUserInfo();
        groupUserInfo.setUid(uid);
        return groupUserInfo;
    }
}
