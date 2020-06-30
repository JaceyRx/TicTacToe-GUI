package com.jacey.game.gui.action.battle;

import com.jacey.game.gui.action.BaseMessageAction;
import com.jacey.game.gui.annotation.MessageClassMapping;
import com.jacey.game.gui.manager.ViewManager;
import com.jacey.game.gui.msg.IMessage;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.BaseBattle;
import com.jacey.game.gui.proto3.Rpc;
import com.jacey.game.gui.service.BaseBattleEventService;
import com.jacey.game.gui.view.BattleFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.List;

/**
 * @Description: 落子响应
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
@Component
@MessageClassMapping(value = Rpc.RpcNameEnum.PlacePieces_VALUE)
public class PlacePiecesAction extends BaseMessageAction {

    @Autowired
    private BaseBattleEventService baseBattleEventService;

    @Override
    protected void LogRequest(IMessage requestMessage) throws Exception {
        NetMessage req = (NetMessage) requestMessage;
        log.info("【落子响应】 >>{}", req.getProtobufText(BaseBattle.PlacePiecesResponse.class));
    }

    @Override
    protected void LogResponse(IMessage responseMessage) throws Exception {

    }

    @Override
    protected IMessage doAction(IMessage requestMessage) throws Exception {
        NetMessage netMessage = (NetMessage) requestMessage;
        switch (netMessage.getErrorCode()) {
            case Rpc.RpcErrorCodeEnum.Ok_VALUE: {
                BaseBattle.PlacePiecesResponse placePiecesResponse = netMessage.getLite(BaseBattle.PlacePiecesResponse.class);
                BaseBattle.EventMsgList eventMsgLists = placePiecesResponse.getEventList();
                List<BaseBattle.EventMsg> eventMsgList = eventMsgLists.getMsgListList();
                baseBattleEventService.doEvent(eventMsgList);
                break;
            } case Rpc.RpcErrorCodeEnum.PlacePiecesErrorIndexError_VALUE: { //请求落子错误，要落子的位置非法
                log.error("【落子请求响应】 请求落子错误，要落子的位置非法....");
                String str = "Position failure, the position of the child is illegal";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.PlacePiecesErrorIndexIsNotEmpty_VALUE: { //请求落子错误，要落子的位置已经有棋子
                log.error("【落子请求响应】 请求落子错误，要落子的位置已经有棋子....");
                String str = "Position failure, There are already chess pieces";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.UserNotInBattle_VALUE: { //玩家不在对战中
                log.error("【落子请求响应】 玩家不在对战中....");
                String str = "Player is not playing";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.BattleNotStart_VALUE: { //游戏尚未开始
                log.error("【落子请求响应】 游戏尚未开始....");
                String str = "The game has not started";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.IsNotUserTurn_VALUE: { //不是该玩家的回合
                log.error("【落子请求响应】 不是该玩家的回合....");
                String str = "Not your player's turn";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.InputLastEventNumError_VALUE: { //丢包
                log.error("【落子请求响应】 丢包....");
                break;
            } case Rpc.RpcErrorCodeEnum.ServerError_VALUE: { //服务器内部错误
                log.error("【落子请求响应】 服务器内部错误....");
                String str = "Internal server error";
                JOptionPane.showMessageDialog(null, str);
                break;
            } default: {
                log.error("【落子请求响应】未知错误代码类型，errorCode = {}", netMessage.getErrorCode());
                String str = "Unknown mistake";
                JOptionPane.showMessageDialog(null, str);
                break;
            }
        }
        return null;
    }
}
