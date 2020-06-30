package com.jacey.game.gui.service;

import com.jacey.game.gui.manager.OnlineClientManager;
import com.jacey.game.gui.manager.ViewManager;
import com.jacey.game.gui.proto3.BaseBattle;
import com.jacey.game.gui.view.HallFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;

/**
 * @Description: 基础对战事件处理
 * @Author: JaceyRuan
 * @Email: jacey.ruan@outlook.com
 */
@Slf4j
@Service
public class BaseBattleEventService {

    public void doEvent(List<BaseBattle.EventMsg> eventMsgList) {
        for (BaseBattle.EventMsg eventMsg : eventMsgList) {
            // 设置事件编号。用于服务端丢包判断
            ViewManager.getInstance().getBattleFrame().setLastEventNum(eventMsg.getEventNum());
            log.info("lastEventNum: {}", eventMsg.getEventNum());
            switch (eventMsg.getEventTypeValue()) {
                case BaseBattle.EventTypeEnum.EventTypeStartTurn_VALUE: {
                    /** 开始回合 */
                    log.info("【回合开始】...");
                    BaseBattle.StartTurnEvent startTurnEvent = eventMsg.getStartTurnEvent();
                    BaseBattle.CurrentTurnInfo currentTurnInfo = startTurnEvent.getCurrentTurnInfo();
                    if (currentTurnInfo.getUserId() == OnlineClientManager.getInstance().getUserInfo().getUserId()) {
                        // 如果是我的回合。
                        // 弹框提醒回合开始
                        String username = OnlineClientManager.getInstance().getUserInfo().getNickname();
                        String str = username + " Your Round";
                        /** 回合提醒弹窗 */
                        JOptionPane.showMessageDialog(null, str);
                    }
                    // 设置界面回合标识
                    break;
                } case BaseBattle.EventTypeEnum.EventTypeEndTurn_VALUE: {
                    /** 结束回合 */
                    log.info("【回合结束】...");
                    BaseBattle.EndTurnEvent endTurnEvent = eventMsg.getEndTurnEvent();
                    int endTurnUserId = endTurnEvent.getEndTurnUserId();
                    if (endTurnUserId == OnlineClientManager.getInstance().getUserInfo().getUserId()) {
                        // 我方结束回合
                        /** 回合标签设置 */
                        ViewManager.getInstance().getBattleFrame().getRound_text().setText("\u5bf9\u65b9\u56de\u5408");  // 对方回合
                        ViewManager.getInstance().getBattleFrame().viewRefresh(); // 刷新
                    } else {
                        // 对方结束回合
                        /** 回合标签设置 */
                        ViewManager.getInstance().getBattleFrame().getRound_text().setText("\u6211\u65b9\u56de\u5408");  // 我方回合
                        ViewManager.getInstance().getBattleFrame().viewRefresh(); // 刷新
                    }


                    break;
                } case BaseBattle.EventTypeEnum.EventTypePlacePieces_VALUE: {
                    /** 落子 */
                    log.info("【落子】...");
                    BaseBattle.PlacePiecesEvent placePiecesEvent = eventMsg.getPlacePiecesEvent();
                    // 根据落子位置设置棋子（先手x,后手o）
                    int index = placePiecesEvent.getIndex();
                    List<JTextField> allIndexJtf = ViewManager.getInstance().getBattleFrame().getAllIndexJtf();
                    JTextField indexJtf = allIndexJtf.get(index);
                    int userId = placePiecesEvent.getUserId();
                    // 设置棋盘
                    if (userId == OnlineClientManager.getInstance().getUserInfo().getUserId()) {
                        // 我方落子
                        indexJtf.setText(ViewManager.getInstance().getBattleFrame().getMyPiecesStr());
                    } else {
                        // 对方棋子
                        indexJtf.setText(ViewManager.getInstance().getBattleFrame().getOpponentPiecesStr());
                    }
                    break;
                } case BaseBattle.EventTypeEnum.EventTypeGameOver_VALUE: {
                    /** 游戏结束 */
                    log.info("【游戏结束】...");
                    BaseBattle.GameOverEvent gameOverEvent = eventMsg.getGameOverEvent();
                    int userId = gameOverEvent.getWinnerUserId();
                    String str = "";
                    switch (gameOverEvent.getGameOverReasonValue()) {
                        case BaseBattle.GameOverReasonEnum.GameOverPlayerWin_VALUE: {
                            // 一方获胜
                            if (userId == OnlineClientManager.getInstance().getUserInfo().getUserId()) {
                                // 我方获胜
                                str = "WIN";
                            } else {
                                // 我方失败
                                str = "FAILURE";
                            }
                            break;
                        } case BaseBattle.GameOverReasonEnum.GameOverPlayerConcede_VALUE: {
                            // 一方投降
                            if (userId == OnlineClientManager.getInstance().getUserInfo().getUserId()) {
                                // 我方获胜
                                str = "WIN，Opponent avatar";
                            } else {
                                // 我方投降
                                str = "FAILURE，We Concede";
                            }
                            break;
                        } case BaseBattle.GameOverReasonEnum.GameOverDraw_VALUE: {
                            // 平局
                            str = "DRAW";
                            break;
                        } default: {
                            // 未知原因
                            str = "Unknown cause";
                            break;
                        }
                    }
                    // 弹框通知结束。并判断获胜还是输。还是平局
                    JOptionPane.showMessageDialog(null, str);
                    // 关闭对战界面。重启用户界面
                    ViewManager.getInstance().getBattleFrame().dispose();
                    ViewManager.getInstance().setHallFrame(new HallFrame(OnlineClientManager.getInstance().getUserInfo()));
                    break;
                } default: {
                    // 未知事件类型
                    log.error("【事件处理异常】 未知事件类型 EventType = {}", eventMsg.getEventType());
                }
            }
        }
    }

}
