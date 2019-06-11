package com.mingqi.local.userservice.remote;

import com.mingqi.local.userservice.UserQueryRemoteService;
import com.mingqi.local.userservice.dto.UidQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service("userQueryRemoteService")
public class UserQueryRemoteServiceImpl implements UserQueryRemoteService {

    @Autowired
    Environment environment;

    @Override
    public String queryByUidRequest(UidQueryRequest request) {
        System.out.println(String.format("invoke queryByUidRequest with request=%s, serverport=%s", request, getPort()));
        return "success";
    }

    public String getPort(){
        return environment.getProperty("local.server.port");
    }
}
