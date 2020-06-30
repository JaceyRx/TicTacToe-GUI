package com.jacey.game.gui.manager;

import com.jacey.game.gui.network.ServerNode;
import com.jacey.game.gui.network.netty.NettySocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

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
        // HTTP 请求GM服务器获取 gateway连接地址
        String url = "http://%s:%d/gateway";
        url = String.format(url, ConfigManager.SERVER_HOST, ConfigManager.SERVER_PORT);
        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        try {
            String resultString = rest.getForObject(new URI(url), String.class);
            if (StringUtils.isEmpty(resultString)) {
                log.error("【服务器连接获取异常】无可用连接服务器.....请求地址：{}", url);
                System.exit(1);
            } else {
                String[] hostPort = resultString.split(":");
                NettySocketServer.getInstance().setServerHost(hostPort[0]);
                NettySocketServer.getInstance().setServerPort(Integer.valueOf(hostPort[1]));
            }
        } catch (Exception e) {
            log.error("【服务器连接获取异常】无法连接服务器.....请求地址：{}", url);
            System.exit(1);
        }

        // 设置host port
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
