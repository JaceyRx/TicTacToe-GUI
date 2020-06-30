package com.jacey.game.gui.manager;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.jacey.game.gui.action.BaseMessageAction;
import com.jacey.game.gui.actor.*;
import com.jacey.game.gui.msg.NetMessage;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 消息管理器.主要用于协议的加载、消息的分发和发送
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class MessageManager implements IManager {

    private MessageManager(){}

    private static MessageManager instance = new MessageManager();

    public static MessageManager getInstance() {
        return instance;
    }

    /** battleServer System Actor */
    private ActorSystem system;

    // key:rpcNum, value:对应处理这个消息的Actor
    private final Map<Integer, ActorRef> rpcNumToHandleActorMap = new HashMap<Integer, ActorRef>();
    // key:actor, value:map(key:rpcNum, value:处理这个消息的Action)
    private final Map<Class<? extends BaseMessageActor>, Map<Integer, Class<BaseMessageAction>>> actorToHandleActionMap = new HashMap<Class<? extends BaseMessageActor>, Map<Integer, Class<BaseMessageAction>>>();


    @Override
    public void init() {
        system = ActorSystem.create();
        system.actorOf(Props.create(LoginActor.class), "loginActor");
        system.actorOf(Props.create(MatchActor.class), "matchActor");
        system.actorOf(Props.create(ChatRoomActor.class), "chatRoomActor");
        system.actorOf(Props.create(BattleMsgHandleActor.class, "com.jacey.game.gui.action.battle"), "battleMsgHandleActor");
    }

    @Override
    public void shutdown() {

    }

    /**
     * 消息分发
     * @param message
     */
    public void handleRequest(NetMessage message) {
        ActorRef actor = rpcNumToHandleActorMap.get(message.getRpcNum());
        if (actor != null) {
            actor.tell(message, ActorRef.noSender());
        } else {
            log.error("【消息处理异常】不支持该协议处理 rpcNum = {}", message.getRpcNum());
        }
    }


    /**
     * 发送消息给服务器
     * @param netMsg
     * @return
     */
    public boolean sendNetMsgToServer(NetMessage netMsg) {
        if (OnlineClientManager.getInstance().isConnectionServer) {
            Channel channel = OnlineClientManager.getInstance().getSession();
            channel.writeAndFlush(netMsg);
            return true;
        }
        log.error("【发送失败】 未连接远程服务器");
        return false;
    }

    /**
     * 获取 Actor 下属的所有 Action 对象
     * @param clazz  Actor代理对象Class
     * @return
     */
    public Map<Integer, Class<BaseMessageAction>> getActionClassByActor(Class<? extends BaseMessageActor> clazz) {
        return actorToHandleActionMap.get(clazz);
    }

    /**
     * 添加 actor 与其所属下属 action Map（一对多）
     * @param clazz  actor代理对象
     * @param map    actionMap
     */
    public void addActorToHandleAction(Class<? extends BaseMessageActor> clazz,
                                       Map<Integer, Class<BaseMessageAction>> map) {
        actorToHandleActionMap.put(clazz, map);
    }


    /**
     * 添加RpcNum通信协议号与其对应的消息处理Actor到map缓存中
     * @param rpcNum   通信协议号
     * @param actor	   处理该消息的actor代理
     */
    public void addRpcNumToHandleActorMap(int rpcNum, ActorRef actor) {
        if (rpcNumToHandleActorMap.containsKey(rpcNum) == true) {
            log.error(
                    "【addRpcNumToHandleActorMap error】 multiple actor to handle same rpcNum = {}, actorName = {} and {}",
                    rpcNum, rpcNumToHandleActorMap.get(rpcNum).getClass().getName(), actor.getClass().getName());
        }
        rpcNumToHandleActorMap.put(rpcNum, actor);
    }
}
