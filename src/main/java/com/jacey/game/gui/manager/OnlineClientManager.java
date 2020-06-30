package com.jacey.game.gui.manager;

import com.jacey.game.gui.proto3.CommonMsg;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 在线客户端管理
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class OnlineClientManager implements IManager {

    public static final String SERVER_CONNECTION_KEY = "KEY";

    private OnlineClientManager(){}

    private static OnlineClientManager instance = new OnlineClientManager();

    public static OnlineClientManager getInstance() {
        return instance;
    }

    public boolean isConnectionServer = false;

    @Getter
    @Setter
    private CommonMsg.UserInfo UserInfo;

    @Getter
    @Setter
    private boolean isLogin = false;

    /** 服务器连接会话Map */
    private final Map<String, Channel> sessionMap = new ConcurrentHashMap<String, Channel>();

    @Override
    public void init() {

    }

    @Override
    public void shutdown() {

    }

    public void addSession(Channel channel) {
        sessionMap.put(SERVER_CONNECTION_KEY, channel);
        isConnectionServer = true;
    }

    public Channel getSession() {
        return sessionMap.get(SERVER_CONNECTION_KEY);
    }

    public void removeSession() {
        if (isConnectionServer) {
            Channel channel = sessionMap.remove(SERVER_CONNECTION_KEY);
            channel.close();
            isConnectionServer = false;
        }
    }
}
