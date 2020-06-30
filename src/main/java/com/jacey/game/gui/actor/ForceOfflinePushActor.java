package com.jacey.game.gui.actor;

import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.annotation.MessageMethodMapping;
import com.jacey.game.gui.proto3.Rpc;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * @Description: TODO
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class ForceOfflinePushActor extends BaseMessageActor {

    public ForceOfflinePushActor() {
        super();
    }

    public ForceOfflinePushActor(String actionPackageName) {
        super(actionPackageName);
    }

    /**
     * 强制下线,推送
     */
    @MessageMethodMapping(value = Rpc.RpcNameEnum.ForceOfflinePush_VALUE, isNet = true)
    public void forceOfflinePush(NetMessage netMessage) {
        // 弹框
        log.error("【强制下线推送】....");
        JOptionPane.showMessageDialog(null, "Force Off line !!");
        // 关停
        System.exit(1);
    }

}
