package com.jacey.game.gui.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;

/**
 * @Description: 配置文件加载管理
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class ConfigManager implements IManager {

    private static ConfigManager configManager = new ConfigManager();

    private static ConfigurationFactory factory;
    private static Configuration config;

    public static String SERVER_HOST;
    public static int SERVER_PORT;
    /** 心跳配置 */
    public static int SOCKET_READER_IDLE_TIME;      // 空闲读
    public static int SOCKET_WRITER_IDLE_TIME;      // 空闲写
    public static int SOCKET_ALL_IDLE_TIME;         // 空闲（超过300.客户端无发包在，则为空闲）


    private ConfigManager() {}

    public static ConfigManager getInstance() {
        return configManager;
    }


    @Override
    public void init() {
        log.info("------------ start load config ------------");
        loadConfig();
        log.info("------------ finish load config ------------");
    }

    @Override
    public void shutdown() {

    }

    private void loadConfig() {
        factory = new ConfigurationFactory("propertyConfig.xml");
        try {
            config = factory.getConfiguration();
        } catch (ConfigurationException e) {
            log.error("【config初始化失败】, exception = ", e);
            System.exit(-1);
        }
        SERVER_HOST = config.getString("server.host");
        SERVER_PORT = config.getInt("server.port");

        SOCKET_READER_IDLE_TIME = config.getInt("socket.reader.idle.time");
        SOCKET_WRITER_IDLE_TIME = config.getInt("socket.writer.idle.time");
        SOCKET_ALL_IDLE_TIME = config.getInt("socket.all.idle.time");

    }

}
