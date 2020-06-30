package com.jacey.game.gui.action.battle;

import com.jacey.game.gui.action.BaseMessageAction;
import com.jacey.game.gui.annotation.MessageClassMapping;
import com.jacey.game.gui.msg.IMessage;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.BaseBattle;
import com.jacey.game.gui.proto3.Rpc;
import com.jacey.game.gui.service.BaseBattleEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.List;

/**
 * @Description: 认输响应
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
@Component
@MessageClassMapping(value = Rpc.RpcNameEnum.Concede_VALUE)
public class ConcedeActor extends BaseMessageAction {

    @Autowired
    private BaseBattleEventService baseBattleEventService;

    @Override
    protected void LogRequest(IMessage requestMessage) throws Exception {
        NetMessage req = (NetMessage) requestMessage;
        log.info("【认输响应】 >>{}", req.getProtobufText(BaseBattle.ConcedeResponse.class));
    }

    @Override
    protected void LogResponse(IMessage responseMessage) throws Exception {

    }

    @Override
    protected IMessage doAction(IMessage requestMessage) throws Exception {
        NetMessage netMessage = (NetMessage) requestMessage;
        switch (netMessage.getErrorCode()) {
            case Rpc.RpcErrorCodeEnum.Ok_VALUE: {
                log.error("【认输响应】 认输成功...");
                BaseBattle.ConcedeResponse concedeResponse = netMessage.getLite(BaseBattle.ConcedeResponse.class);
                BaseBattle.EventMsgList eventMsgLists = concedeResponse.getEventList();
                List<BaseBattle.EventMsg> eventMsgList = eventMsgLists.getMsgListList();
                baseBattleEventService.doEvent(eventMsgList);
                break;
            } case Rpc.RpcErrorCodeEnum.UserNotInBattle_VALUE: { //玩家不在对战中
                log.error("【认输响应】 玩家不在对战中....");
                String str = "Player is not playing";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.BattleNotStart_VALUE: { //游戏尚未开始
                log.error("【认输响应】 游戏尚未开始....");
                String str = "The game has not started";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.ServerError_VALUE: { //服务器内部错误
                log.error("【认输响应】 服务器内部错误....");
                String str = "Internal server error";
                JOptionPane.showMessageDialog(null, str);
                break;
            } default: {
                log.error("【认输响应】未知错误代码类型，errorCode = {}", netMessage.getErrorCode());
                String str = "Unknown mistake";
                JOptionPane.showMessageDialog(null, str);
                break;
            }
        }
        return null;
    }
}
