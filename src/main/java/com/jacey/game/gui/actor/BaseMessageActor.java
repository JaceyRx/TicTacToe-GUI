package com.jacey.game.gui.actor;

import akka.actor.*;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.jacey.game.gui.action.BaseMessageAction;
import com.jacey.game.gui.annotation.MessageClassMapping;
import com.jacey.game.gui.annotation.MessageMethodMapping;
import com.jacey.game.gui.exception.RpcErrorException;
import com.jacey.game.gui.manager.MessageManager;
import com.jacey.game.gui.manager.SpringManager;
import com.jacey.game.gui.msg.IMessage;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.Rpc;
import com.jacey.game.gui.utils.ClassScanner;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.Duration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 基础消息处理Actor类--所有自定义的Actor都应该继承该类
 *                  用于协议初始化加载与消息分发处理
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class BaseMessageActor extends UntypedAbstractActor {

    /** key：rpcNum, value: handleMethodName 协议编号与处理方法的映射关系 */
    protected final Map<Integer, String> mappingMethodMap = new HashMap<Integer, String>();
    /*** key：rpcNum, value: action 协议编号与action的映射关系*/
    protected Map<Integer, Class<BaseMessageAction>> mappingActionClassMap = null;

    // 如果此Actor有下属的action包，包中有处理rpc的Action类，则必须进行指定。这样才能建立rpcNum与此Actor的对应关系
    private String actionPackageName;

    // 第三方反射工具类
    protected final MethodAccess methodAccess = MethodAccess.get(this.getClass());

    public BaseMessageActor() {}

    public BaseMessageActor(String actionPackageName) {
        this.actionPackageName = actionPackageName;
    }

    @Override
    public void preStart() {
        log.info("preStart {}", this.getClass().getName());
        // 如果此Actor有下属的action包（动作包），则包中所有Action类所对应处理的rpc都将由这个Actor负责
        if (this.actionPackageName != null) {
            // 获取 Actor 下属的所有 Action 对象
            mappingActionClassMap = MessageManager.getInstance().getActionClassByActor(this.getClass());
            // 如果MessageManager中获取不到，则重新扫描包路径并添加回MessageManager
            if (mappingActionClassMap == null) {
                mappingActionClassMap = new HashMap<Integer, Class<BaseMessageAction>>();
                Set<Class<?>> actionClassNames = ClassScanner.listClassesWithAnnotation(actionPackageName,
                        MessageClassMapping.class);
                if (actionClassNames != null) {
                    for (Class clazz : actionClassNames) {
                        MessageClassMapping mapping = (MessageClassMapping) clazz
                                .getAnnotation(MessageClassMapping.class);
                        // rpcNum为消息处理协议号
                        int rpcNum = mapping.value();
                        if (mappingActionClassMap.containsKey(rpcNum) == true) {
                            log.error(
                                    "preStart error, multiple action class to handle same rpcNum = {}, action class = {} and {}",
                                    rpcNum, mappingActionClassMap.get(rpcNum).getClass().getName(),
                                    clazz.getName());
                        }
                        mappingActionClassMap.put(rpcNum, clazz);
                    }
                }
                // 赋值回MessageManager
                MessageManager.getInstance().addActorToHandleAction(this.getClass(), mappingActionClassMap);
            }
            // 设置一系列rpcNum协议，统一由哪个Actor处理
            for (Map.Entry<Integer, Class<BaseMessageAction>> entry : mappingActionClassMap.entrySet()) {
                MessageClassMapping mapping = entry.getValue().getAnnotation(MessageClassMapping.class);
                // 判断是否是网络协议
                if (mapping.isNet()) {
                    int rpcNum = mapping.value();
                    log.info("【协议加载完成】 rpcNum = {}, rpcName = {}", rpcNum, Rpc.RpcNameEnum.forNumber(rpcNum));
                    MessageManager.getInstance().addRpcNumToHandleActorMap(rpcNum, self());
                }
            }
        }
        // 查找此Actor中是否有处理rpc的函数
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            MessageMethodMapping mapping = method.getAnnotation(MessageMethodMapping.class);
            if (mapping != null) {
                int[] rpcNum = mapping.value();
                boolean isNet = mapping.isNet();
                for (int oneRpcNum : rpcNum) {
                    if (mappingMethodMap.containsKey(oneRpcNum)) {
                        log.error("multiple actor method to handle same rpcNum = {}, method name = {} and {}",
                                oneRpcNum, mappingMethodMap.get(oneRpcNum), method.getName());
                    }
                    if (mappingActionClassMap != null && mappingActionClassMap.containsKey(oneRpcNum)) {
                        log.error(
                                "multiple actor class and method to handle same rpcName {}, action class = {}, actor method = {}",
                                oneRpcNum, mappingActionClassMap.get(oneRpcNum).getName(), method.getName());
                    }
                    mappingMethodMap.put(oneRpcNum, method.getName());
                    // 如果该协议是网络协议之添加到MessageManager 的RpcNumToHandleActorMap 缓存中
                    if (isNet) {
                        log.info("【协议加载完成】 rpcNum = {}, rpcName = {}", oneRpcNum, Rpc.RpcNameEnum.forNumber(oneRpcNum));
                        MessageManager.getInstance().addRpcNumToHandleActorMap(oneRpcNum, self());
                    }
                }
            }
        }
    }

    @Override
    public void onReceive(Object o) throws Throwable {
        /** 收到请求，先尝试寻找对应的Action类处理，找不到则在Actor中找对应处理函数 */
        if (o instanceof Terminated) {
            doTerminated((Terminated) o);
        } else if (o instanceof NetMessage) {
            /** 网络请求相关 */
            NetMessage netMessage = (NetMessage) o;
            int rpcNum = netMessage.getRpcNum();
            Rpc.RpcNameEnum rpcNameEnum = Rpc.RpcNameEnum.forNumber(rpcNum);
            try {
                Class<BaseMessageAction> clazz = (mappingActionClassMap != null ? mappingActionClassMap.get(rpcNum)
                        : null);
                if (clazz != null) {
                    // action执行
                    BaseMessageAction action = SpringManager.getInstance().getBean(clazz);
                    IMessage response = action.handleMessage(netMessage);
                    if ( response != null) {
                        sender().tell(response, ActorRef.noSender());
                    }
                } else if (mappingMethodMap.containsKey(rpcNum)) {
                    // actor method执行
                    IMessage response = (IMessage) methodAccess.invoke(this, mappingMethodMap.get(rpcNum), netMessage);
                    if (response != null) {
                        sender().tell(response, ActorRef.noSender());
                    }
                } else {
                    log.error("【netMessage消息解析异常】 not support netMessage rpcNum = {}", rpcNum);
                    sendErrorToClient(netMessage, Rpc.RpcErrorCodeEnum.ServerError_VALUE);
                }
            } catch (RpcErrorException e) {
                log.error("【netMessage {} handle 执行异常】 error = ", rpcNameEnum == null ? rpcNum : rpcNameEnum, e);
                sendErrorToClient(netMessage, e.getErrorCode());
            } catch (Exception e) {
                log.error("【netMessage {} handle 执行异常】 error = ", rpcNameEnum == null ? rpcNum : rpcNameEnum, e);
            }
        }
    }


    /**
     * 发送错误信息给客户端
     * @param netMessage
     * @param errorCode
     */
    protected void sendErrorToClient(NetMessage netMessage, int errorCode) {
        NetMessage resp = new NetMessage(netMessage.getRpcNum(), errorCode);
        sender().tell(resp, ActorRef.noSender());
    }

    /**
     * 终止命令处理
     * @param t
     * @throws Exception
     */
    protected void doTerminated(Terminated t) throws Exception {
    }

    /**
     * 定时任务
     * @param initialDelaySecond
     * @param intervalSecond
     * @param msg
     * @return
     */
    protected Cancellable schedule(int initialDelaySecond, int intervalSecond, IMessage msg) {
        ActorSystem system = context().system();
        return system.scheduler().schedule(Duration.create(initialDelaySecond, TimeUnit.SECONDS),
                Duration.create(intervalSecond, TimeUnit.SECONDS), getSelf(), msg, system.dispatcher(),
                ActorRef.noSender());
    }

}
