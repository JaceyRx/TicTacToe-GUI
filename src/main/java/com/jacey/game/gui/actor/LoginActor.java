package com.jacey.game.gui.actor;

import com.jacey.game.gui.annotation.MessageMethodMapping;
import com.jacey.game.gui.manager.MessageManager;
import com.jacey.game.gui.manager.OnlineClientManager;
import com.jacey.game.gui.manager.ViewManager;
import com.jacey.game.gui.msg.IMessage;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.BaseBattle;
import com.jacey.game.gui.proto3.CommonEnum;
import com.jacey.game.gui.proto3.CommonMsg;
import com.jacey.game.gui.proto3.Rpc;
import com.jacey.game.gui.view.HallFrame;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * @Description: 登录与注册响应处理
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class LoginActor extends BaseMessageActor {

    public LoginActor() {
        super();
    }

    public LoginActor(String actionPackageName) {
        super(actionPackageName);
    }

    /**
     * 接收登录响应
     */
    @MessageMethodMapping(value = Rpc.RpcNameEnum.Login_VALUE, isNet = true)
    public void loginResponse(NetMessage netMessage) {
        switch (netMessage.getErrorCode()) {
            case Rpc.RpcErrorCodeEnum.Ok_VALUE: {
                CommonMsg.LoginResponse loginResponse = netMessage.getLite(CommonMsg.LoginResponse.class);
                log.info("【登录响应】 登录成功 >>" + netMessage.getProtobufText(CommonMsg.LoginResponse.class));
                CommonMsg.UserInfo userInfo = loginResponse.getUserInfo();                      // 获取玩家信息
                CommonMsg.UserState userState = userInfo.getUserState();                       // 获取玩家状态信息（含在线状态、行为状态等）
                CommonEnum.UserActionStateEnum userActionState = userState.getActionState();  // 获取行为玩家状态
                OnlineClientManager.getInstance().setUserInfo(userInfo);
                OnlineClientManager.getInstance().setLogin(true);
                switch (userActionState.getNumber()) {
                    case CommonEnum.UserActionStateEnum.ActionNone_VALUE:
                    case CommonEnum.UserActionStateEnum.Matching_VALUE: {
                        // 如果处于非对战状态。则跳转到用户界面
                        ViewManager.getInstance().setHallFrame(new HallFrame(userInfo));    // 创建用户界面
                        ViewManager.getInstance().getLoginFrame().dispose();                // 关闭登录界面
                        break;
                    } case CommonEnum.UserActionStateEnum.Playing_VALUE: {
                        // 如果处于对战状态，发起获取对战信息请求。对战界面有对战信息请求响应创建
                        BaseBattle.GetBattleInfoRequest.Builder builder = BaseBattle.GetBattleInfoRequest.newBuilder();
                        NetMessage netMsg = new NetMessage(Rpc.RpcNameEnum.GetBattleInfo_VALUE, builder);
                        MessageManager.getInstance().sendNetMsgToServer(netMsg);
                        break;
                    } default:{
                        log.error("【登录响应异常】 未知玩家行为状态 UserActionState = {}", userActionState);
                        break;
                    }
                }
                break;
            } case Rpc.RpcErrorCodeEnum.LoginErrorForbid_VALUE: {   // 账号被封禁
                log.error("【登录响应】 无法登录，账号被封禁....");
                String str = "Unable to login, the user has been banned";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.LoginErrorPasswordWrong_VALUE: { // 密码错误
                log.error("【登录响应】 无法登录，密码错误....");
                String str = "Unable to login, the password is wrong";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.LoginErrorUsernameIsNotExist_VALUE: { // 用户名不存在
                log.error("【登录响应】 无法登录，用户名不存在....");
                String str = "Cannot login, the username cannot exist";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.ServerError_VALUE: { //服务器内部错误
                log.error("【登录响应】 服务器内部错误....");
                String str = "Internal server error";
                JOptionPane.showMessageDialog(null, str);
                break;
            } default: {
                log.error("【登录响应异常】未知错误代码类型，errorCode = {}", netMessage.getErrorCode());  // 未知错误
                String str = "Unknown mistake";
                JOptionPane.showMessageDialog(null, str);
                break;
            }
        }

    }

    /**
     * 接收注册响应
     */
    @MessageMethodMapping(value = Rpc.RpcNameEnum.Regist_VALUE, isNet = true)
    public void registResponse(NetMessage netMessage) {
        switch (netMessage.getErrorCode()) {
            case Rpc.RpcErrorCodeEnum.Ok_VALUE: {
                log.info("【注册响应】 注册成功");
                String str = "Registration success";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.RegisErrorUsernameIllegal_VALUE: {   // 用户名非法
                log.error("【注册响应】 无法注册，用户名非法....");
                String str = "Unable to register, the username is illegal";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.RegisErrorUsernameIsExist_VALUE: { // 用户名已存在
                log.error("【注册响应】 无法注册，用户名已存在....");
                String str = "Unable to register, the username already exists";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.RegisErrorPasswordIllegal_VALUE: {  // 密码非法
                log.error("【注册响应】 无法注册，密码非法....");
                String str = "Unable to register, the password is illegal";
                JOptionPane.showMessageDialog(null, str);
            } case Rpc.RpcErrorCodeEnum.ServerError_VALUE: { //服务器内部错误
                log.error("【注册响应】 服务器内部错误....");
                String str = "Internal server error";
                JOptionPane.showMessageDialog(null, str);
                break;
            } default: {
                log.error("【注册响应】未知错误代码类型，errorCode = {}", netMessage.getErrorCode());
                String str = "Unknown mistake";
                JOptionPane.showMessageDialog(null, str);
                break;
            }
        }
    }

}
