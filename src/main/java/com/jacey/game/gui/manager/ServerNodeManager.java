package com.jacey.game.gui.manager;

import com.jacey.game.gui.network.ServerNode;
import com.jacey.game.gui.network.netty.NettySocketServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 服务器启动管理
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class ServerNodeManager implements IManager {

    private ServerNodeManager() {}

    private static ServerNodeManager instance = new ServerNodeManager();

    public static ServerNodeManager getInstance() {
        return instance;
    }

    private ServerNode serverNode;

    @Override
    public void init() {
        serverNode = NettySocketServer.getInstance();
        try {
            serverNode.start();
        } catch (Exception e) {
            log.error("【客户端启动失败】 error = ", e);
            System.exit(0);
        }
    }

    @Override
    public void shutdown() {

    }
}
