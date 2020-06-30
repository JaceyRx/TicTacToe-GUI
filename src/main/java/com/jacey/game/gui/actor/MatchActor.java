package com.jacey.game.gui.actor;

import com.jacey.game.gui.annotation.MessageMethodMapping;
import com.jacey.game.gui.manager.MessageManager;
import com.jacey.game.gui.manager.OnlineClientManager;
import com.jacey.game.gui.manager.ViewManager;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.BaseBattle;
import com.jacey.game.gui.proto3.CommonMsg;
import com.jacey.game.gui.proto3.Rpc;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * @Description: 匹配响应&取消匹配响应处理
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
public class MatchActor extends BaseMessageActor {

    public MatchActor() {
        super();
    }

    public MatchActor(String actionPackageName) {
        super(actionPackageName);
    }

    /**
     * 匹配响应接收
     */
    @MessageMethodMapping(value = Rpc.RpcNameEnum.Match_VALUE, isNet = true)
    public void matchResponse(NetMessage netMessage) {
        switch (netMessage.getErrorCode()) {
            case Rpc.RpcErrorCodeEnum.Ok_VALUE: {
                log.info("【匹配响应】 正在匹配中...");
                ViewManager.getInstance().getHallFrame().getMatch_btn().setEnabled(false);  // 设置匹配按钮不可用
                ViewManager.getInstance().getHallFrame().getUnmatch_btn().setEnabled(true);  // 设置取消匹配按钮可用
                break;
            } case Rpc.RpcErrorCodeEnum.MatchErrorMatching_VALUE: { // 无法匹配，目前就是匹配状态
                log.error("【匹配响应】 无法匹配，目前就是匹配状态....");
                String str = "Unable to match, this is currently the match status";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.MatchErrorPlaying_VALUE: { // 无法匹配，已经在对战中
                log.error("【匹配响应】 无法匹配，已经在对战中....");
                String str = "Unmatchable, already in battle";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.MatchErrorOtherActionState_VALUE: { // 无法匹配，处于其他状态
                log.error("【匹配响应】 无法匹配，处于其他状态....");
                String str = "Unable to match, in other states";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.ServerError_VALUE: { //服务器内部错误
                log.error("【匹配响应】 服务器内部错误...");
                String str = "Internal server error";
                JOptionPane.showMessageDialog(null, str);
                break;
            } default: {
                log.error("【匹配响应】未知错误代码类型，errorCode = {}", netMessage.getErrorCode());
                String str = "Unknown mistake";
                JOptionPane.showMessageDialog(null, str);
                break;
            }
        }
    }

    /**
     * 取消匹配响应接收
     */
    @MessageMethodMapping(value = Rpc.RpcNameEnum.CancelMatch_VALUE, isNet = true)
    public void cancelMatchResponse(NetMessage netMessage) {
        switch (netMessage.getErrorCode()) {
            case Rpc.RpcErrorCodeEnum.Ok_VALUE: {
                log.info("【取消匹配响应】 取消匹配成功...");
                ViewManager.getInstance().getHallFrame().getMatch_btn().setEnabled(true);     // 设置匹配按钮可用
                ViewManager.getInstance().getHallFrame().getUnmatch_btn().setEnabled(false);  // 设置取消匹配按钮不可用
                break;
            } case Rpc.RpcErrorCodeEnum.CancelMatchErrorNotMatching_VALUE: { // 取消匹配失败，没有在匹配中
                log.error("【取消匹配响应】 取消匹配失败，没有在匹配中....");
                String str = "Unmatch failed, no match";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.CancelMatchErrorPlaying_VALUE: { //取消匹配失败，已经在对战中
                log.error("【取消匹配响应】 取消匹配失败，已经在对战中....");
                String str = "Unmatch failed, already in the game";
                JOptionPane.showMessageDialog(null, str);
                break;
            } case Rpc.RpcErrorCodeEnum.ServerError_VALUE: { //服务器内部错误
                log.error("【匹配结果推送】 服务器内部错误...");
                String str = "Internal server error";
                JOptionPane.showMessageDialog(null, str);
                break;
            } default: {
                log.error("【取消匹配响应】未知错误代码类型，errorCode = {}", netMessage.getErrorCode());
                String str = "Unknown mistake";
                JOptionPane.showMessageDialog(null, str);
                break;
            }
        }
    }

    /**
     * 匹配结果推送
     */
    @MessageMethodMapping(value = Rpc.RpcNameEnum.MatchResultPush_VALUE, isNet = true)
    public void matchResultResponse(NetMessage netMessage) {
        switch (netMessage.getErrorCode()) {
            case Rpc.RpcErrorCodeEnum.Ok_VALUE: {
                CommonMsg.MatchResultPush matchResultPush = netMessage.getLite(CommonMsg.MatchResultPush.class);
                if (matchResultPush.getIsSuccess()) {
                    log.info("【匹配结果推送】 匹配成功 >>\n" + netMessage.getProtobufText(CommonMsg.MatchResultPush.class));
                    // 发送【获取当前所在对局的信息】在对局信息响应里创建【对战界面】
                    BaseBattle.GetBattleInfoRequest.Builder builder = BaseBattle.GetBattleInfoRequest.newBuilder();
                    NetMessage netMsg = new NetMessage(Rpc.RpcNameEnum.GetBattleInfo_VALUE, builder);
                    MessageManager.getInstance().sendNetMsgToServer(netMsg);
                }
                break;
            } case Rpc.RpcErrorCodeEnum.ServerError_VALUE: { //服务器内部错误
                log.error("【匹配结果推送】 服务器内部错误...");
                String str = "Internal server error";
                JOptionPane.showMessageDialog(null, str);
                break;
            } default: {
                log.error("【匹配结果推送】未知错误代码类型，errorCode = {}", netMessage.getErrorCode());
                String str = "Unknown mistake";
                JOptionPane.showMessageDialog(null, str);
                break;
            }
        }
    }
}
