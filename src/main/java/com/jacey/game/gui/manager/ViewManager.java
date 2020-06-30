package com.jacey.game.gui.manager;

import com.jacey.game.gui.view.BattleFrame;
import com.jacey.game.gui.view.HallFrame;
import com.jacey.game.gui.view.LoginFrame;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description: 界面管理器
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Getter
@Setter
public class ViewManager implements IManager {

    private static ViewManager instance = new ViewManager();

    public static ViewManager getInstance() {
        return instance;
    }

    // 登录界面
    private LoginFrame loginFrame;
    // 用户界面
    private HallFrame hallFrame;
    // 对战界面
    private BattleFrame battleFrame;

    @Override
    public void init() {
        loginFrame = new LoginFrame();
    }

    @Override
    public void shutdown() {

    }

    public static void main(String[] args) {
        new ViewManager().init();
    }
}
