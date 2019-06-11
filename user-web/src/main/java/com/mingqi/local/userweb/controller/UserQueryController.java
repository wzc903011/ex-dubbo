package com.mingqi.local.userweb.controller;

import com.mingqi.local.userservice.UserQueryRemoteService;
import com.mingqi.local.userservice.dto.UidQueryRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@RestController
public class UserQueryController {

    private static int invoke_count = 0;
    @Resource
    private UserQueryRemoteService userQueryRemoteService;

    private static ExecutorService exec = Executors.newFixedThreadPool(100);

    @RequestMapping("/uidrequest")
    public String queryByUidRequest() {
        Random random = new Random();
        UidQueryRequest request = new UidQueryRequest();
        for (int i = 0; i < 100; i++) {
            invoke_count++;
            request.setUid(invoke_count%10);
            exec.submit(new FutureTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    userQueryRemoteService.queryByUidRequest(request);
                    return null;
                }
            }));
        }
        return "hello";
    }
}
