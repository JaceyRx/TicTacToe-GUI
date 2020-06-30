package com.jacey.game.gui.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
/**
 * @Description: 处理Spring相关内容
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class SpringManager implements IManager {

    private static SpringManager instance = new SpringManager();

    public static SpringManager getInstance() {
        return instance;
    }

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    public Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public <T> T getBean(Class<T> object) {
        return context.getBean(object);
    }

    public void setContext(ConfigurableApplicationContext context) {
        this.context = context;
    }
}
