package com.jacey.game.gui.action.battle;

import com.jacey.game.gui.action.BaseMessageAction;
import com.jacey.game.gui.annotation.MessageClassMapping;
import com.jacey.game.gui.manager.MessageManager;
import com.jacey.game.gui.manager.ViewManager;
import com.jacey.game.gui.msg.IMessage;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.BaseBattle;
import com.jacey.game.gui.proto3.CommonEnum;
import com.jacey.game.gui.proto3.CommonMsg;
import com.jacey.game.gui.proto3.Rpc;
import com.jacey.game.gui.view.BattleFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;

/**
 * @Description: 获取对战信息
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
@Component
@MessageClassMapping(value = Rpc.RpcNameEnum.GetBattleInfo_VALUE)
public class GetBattleInfoAction extends BaseMessageAction {

    @Override
    protected void LogRequest(IMessage requestMessage) throws Exception {
        NetMessage req = (NetMessage) requestMessage;
        log.info("【获取对战信息响应】 >>{}", req.getProtobufText(BaseBattle.GetBattleInfoRequest.class));
    }

    @Override
    protected void LogResponse(IMessage responseMessage) throws Exception {
    }

    @Override
    protected IMessage doAction(IMessage requestMessage) throws Exception {
        NetMessage netMessage = (NetMessage) requestMessage;
        // 创建对战界面。并将对战信息传送过去
        switch (netMessage.getErrorCode()) {
            case Rpc.RpcErrorCodeEnum.Ok_VALUE: {
                BaseBattle.GetBattleInfoResponse battleInfoResponse = netMessage.getLite(BaseBattle.GetBattleInfoResponse.class);
                BaseBattle.BattleInfo battleInfo = battleInfoResponse.getBattleInfo(); // 获取对战信息
                if (ViewManager.getInstance().getHallFrame() != null) {
                    ViewManager.getInstance().getHallFrame().dispose();  // 关闭用户界面
                }
                if (ViewManager.getInstance().getLoginFrame() != null) {
                    ViewManager.getInstance().getLoginFrame().dispose(); // 关闭登录界面
                }
                // 在对战界面初始化的时候判断。如果已经开局就开始。如果没开局就发起确认开始请求
                ViewManager.getInstance().setBattleFrame(new BattleFrame(battleInfo));
                /** 发送加入对战聊天室请求*/
                CommonMsg.JoinChatRoomRequest.Builder builder = CommonMsg.JoinChatRoomRequest.newBuilder();
                builder.setChatRoomType(CommonEnum.ChatRoomTypeEnum.TwoPlayerBattleChatRoomType);
                NetMessage netmsg = new NetMessage(Rpc.RpcNameEnum.JoinChatRoom_VALUE, builder);
                log.info("加入聊天室请求发送....");
                MessageManager.getInstance().sendNetMsgToServer(netmsg);

                break;
            } case Rpc.RpcErrorCodeEnum.ServerError_VALUE: { //服务器内部错误
                log.error("【获取对战信息响应】 服务器内部错误....");
                String str = "Internal server error";
                JOptionPane.showMessageDialog(null, str);
                break;
            } default: {
                log.error("【获取对战信息响应】未知错误代码类型，errorCode = {}", netMessage.getErrorCode());
                String str = "Unknown mistake";
                JOptionPane.showMessageDialog(null, str);
                break;
            }
        }
        return null;
    }
}
