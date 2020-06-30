package com.jacey.game.gui.actor;

import com.jacey.game.gui.annotation.MessageMethodMapping;
import com.jacey.game.gui.manager.ViewManager;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.CommonMsg;
import com.jacey.game.gui.proto3.Rpc;
import lombok.extern.slf4j.Slf4j;

import java.sql.Struct;

/**
 * @Description: 聊天室消息处理
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class ChatRoomActor extends BaseMessageActor {

    /**
     * 聊天服务器推送对战聊天内容
     */
    @MessageMethodMapping(value = Rpc.RpcNameEnum.BattleChatTextPush_VALUE, isNet = true)
    public void battleChatTextPush(NetMessage netMessage) {
        CommonMsg.BattleChatTextPush battleChatTextPush = netMessage.getLite(CommonMsg.BattleChatTextPush.class);
        log.info("【聊天消息接收】 {}", netMessage.getProtobufText(CommonMsg.BattleChatTextPush.class));
        // 拼接发送信息
        int senderUserId = battleChatTextPush.getSenderUserId();
        String sendUserName = ViewManager.getInstance().getBattleFrame().getOpponentUserInfo().getNickname();
        String str = sendUserName + ": " + battleChatTextPush.getText() + "\n";
        // 更新聊天框
        ViewManager.getInstance().getBattleFrame().getLeft_textArea().append(str);
    }

    /**
     * 加入聊天室响应
     * @param netMessage
     */
    @MessageMethodMapping(value = Rpc.RpcNameEnum.JoinChatRoom_VALUE, isNet = true)
    public void joinChatRoom(NetMessage netMessage) {
        // 判断是否加入成功
        // 更新聊天框消息 系统提示：
        switch (netMessage.getErrorCode()) {
            case Rpc.RpcErrorCodeEnum.Ok_VALUE: {
                log.info("【加入聊天室响应】 已加入聊天室");
                String str = "系统: 已加入聊天室 \n";
                ViewManager.getInstance().getBattleFrame().getLeft_textArea().append(str);
                ViewManager.getInstance().getBattleFrame().setJoinChatRoom(true);
                break;
            } case Rpc.RpcErrorCodeEnum.BattleChatTextErrorNotJoinBattle_VALUE: {
                log.error("【加入聊天室响应】 对战聊天室加入失败，未加入该对战");
                String str = "系统: 加入聊天室失败 \n";
                ViewManager.getInstance().getBattleFrame().getLeft_textArea().append(str);
                break;
            } default: {
                String str = "系统: 加入聊天室失败 \n";
                ViewManager.getInstance().getBattleFrame().getLeft_textArea().append(str);
                log.error("【加入聊天室响应】 未知错误类型 errorCode = {}", netMessage.getErrorCode());
                break;
            }
        }

    }


    @MessageMethodMapping(value = Rpc.RpcNameEnum.BattleChatText_VALUE, isNet = true)
    public void onReceiveBattleChatText(NetMessage netMessage) {
        log.info("文本发送成功.....");
    }
}
