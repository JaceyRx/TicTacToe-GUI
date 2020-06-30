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

import java.util.List;


/**
 * @Description: 事件消息列表推送接收
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
@Component
@MessageClassMapping(value = Rpc.RpcNameEnum.BattleEventMsgListPush_VALUE)
public class BattleEventMsgListPushActor extends BaseMessageAction {

    @Autowired
    private BaseBattleEventService baseBattleEventService;

    @Override
    protected void LogRequest(IMessage requestMessage) throws Exception {
        NetMessage req = (NetMessage) requestMessage;
        log.info("【对战事件列表推送接收】 >>{}", req.getProtobufText(BaseBattle.BattleEventMsgListPush.class));
    }

    @Override
    protected void LogResponse(IMessage responseMessage) throws Exception {

    }

    @Override
    protected IMessage doAction(IMessage requestMessage) throws Exception {
        NetMessage netMessage = (NetMessage) requestMessage;
        BaseBattle.BattleEventMsgListPush battleEventMsgListPush = netMessage.getLite(BaseBattle.BattleEventMsgListPush.class);
        BaseBattle.EventMsgList eventMsgLists = battleEventMsgListPush.getEventMsgList();
        List<BaseBattle.EventMsg> eventMsgList = eventMsgLists.getMsgListList();
        // 事件执行
        baseBattleEventService.doEvent(eventMsgList);
        return null;
    }
}
