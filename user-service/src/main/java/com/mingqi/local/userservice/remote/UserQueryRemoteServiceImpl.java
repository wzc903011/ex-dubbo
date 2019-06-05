package com.mingqi.local.userservice.remote;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mingqi.local.userservice.UserQueryRemoteService;
import com.mingqi.local.userservice.dto.GroupUserInfo;
import com.mingqi.local.userservice.dto.UidListQueryRequest;
import com.mingqi.local.userservice.dto.UidQueryExtendRequest;
import com.mingqi.local.userservice.dto.UidQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service("userQueryRemoteService")
public class UserQueryRemoteServiceImpl implements UserQueryRemoteService {

    @Autowired
    Environment environment;

    @Override
    public String queryWithPrimitiveType(long uid, int mallId) {
        Transaction t = Cat.newTransaction("experiment-dubbo-lb", getPort());
        t.setStatus(Message.SUCCESS);
        t.complete();
        System.out.println(String.format("queryWithPrimitiveType with uid=%d, mallId=%d, serverport=%s", uid, mallId, getPort()));

        return "success";
    }

    @Override
    public String queryWithPrimitiveType2(long mallId, int uid) {
        Transaction t = Cat.newTransaction("experiment-dubbo-lb", getPort());
        t.setStatus(Message.SUCCESS);
        t.complete();
        System.out.println(String.format("queryWithPrimitiveType2 with uid=%d, mallId=%d, serverport=%s", uid, mallId, getPort()));
        return "success";
    }

    @Override
    public String queryByUidRequest(UidQueryRequest request) {
        Transaction t = Cat.newTransaction("experiment-dubbo-lb", getPort());
        t.setStatus(Message.SUCCESS);
        t.complete();
        System.out.println(String.format("invoke queryByUidRequest with request=%s, serverport=%s", request, getPort()));
        return "success";
    }

    @Override
    public String queryBySonUidRequest(UidQueryExtendRequest request) {
        Transaction t = Cat.newTransaction("experiment-dubbo-lb", getPort());
        t.setStatus(Message.SUCCESS);
        t.complete();
        System.out.println(String.format("invoke queryBySonUidRequest with request=%s, serverport=%s", request, getPort()));
        return "success";
    }

    @Override
    public String queryByOtherRequest(UidListQueryRequest request) {
        Transaction t = Cat.newTransaction("experiment-dubbo-lb", getPort());
        t.setStatus(Message.SUCCESS);
        t.complete();
        System.out.println(String.format("invoke queryByOtherRequest with request=%s", request, getPort()));
        return "success";
    }

    public String getPort(){
        return environment.getProperty("local.server.port");
    }
}
