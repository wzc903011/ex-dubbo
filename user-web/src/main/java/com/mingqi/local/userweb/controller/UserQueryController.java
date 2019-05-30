package com.mingqi.local.userweb.controller;

import com.mingqi.local.userservice.UserQueryRemoteService;
import com.mingqi.local.userservice.dto.UidListQueryRequest;
import com.mingqi.local.userservice.dto.UidQueryExtendRequest;
import com.mingqi.local.userservice.dto.UidQueryRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
public class UserQueryController {

    private static int invoke_count = 0;
    @Resource
    private UserQueryRemoteService userQueryRemoteService;

    @RequestMapping("/promitivetypeuid1")
    public String index() {
        invoke_count++;
        Random random = new Random();
        userQueryRemoteService.queryWithPrimitiveType(invoke_count%10, random.nextInt(100));
        return "hello";
    }

    @RequestMapping("/promitivetypeuid2")
    public String queryByRequest() {
        invoke_count++;
        Random random = new Random();
        userQueryRemoteService.queryWithPrimitiveType2(random.nextInt(100), invoke_count%10);
        return "hello";
    }

    @RequestMapping("/uidrequest")
    public String queryByUidRequest() {
        invoke_count++;
        Random random = new Random();
        UidQueryRequest request = new UidQueryRequest();
        request.setUid(invoke_count%10);
        request.setMallid(random.nextInt(100));
        userQueryRemoteService.queryByUidRequest(request);
        return "hello";
    }

    @RequestMapping("/sonuidrequest")
    public String queryBySonUidRequest() {
        invoke_count++;
        Random random = new Random();
        UidQueryExtendRequest request = new UidQueryExtendRequest();
        request.setUid(invoke_count%10);
        request.setMallid(random.nextInt(100));
        userQueryRemoteService.queryBySonUidRequest(request);
        return "hello";
    }

    @RequestMapping("/otherrequest")
    public String queryByOtherRequest() {
        invoke_count++;
        UidListQueryRequest request = new UidListQueryRequest();
        Random random = new Random();
        List<Long> uidList = new ArrayList<Long>();
        uidList.add(random.nextLong());
        uidList.add(random.nextLong());
        request.setUidList(uidList);
        userQueryRemoteService.queryByOtherRequest(request);
        return "hello";
    }
}
