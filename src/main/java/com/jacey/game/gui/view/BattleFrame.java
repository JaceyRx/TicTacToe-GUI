package com.jacey.game.gui.view;



import com.jacey.game.gui.manager.MessageManager;
import com.jacey.game.gui.manager.OnlineClientManager;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.BaseBattle;
import com.jacey.game.gui.proto3.CommonEnum;
import com.jacey.game.gui.proto3.CommonMsg;
import com.jacey.game.gui.proto3.Rpc;
import com.jacey.game.gui.utils.UIUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.GroupLayout;

/**
 * @author Brainrain
 */
@Getter
@Setter
@Slf4j
public class BattleFrame extends JFrame {

    /** 棋盘信息 */
    private java.util.List<Integer> allBattleCellInfo;
    private List<JTextField> allIndexJtf;

    /** 对手信息 */
    private CommonMsg.UserBriefInfo opponentUserInfo;
    /** 我的信息 */
    private CommonMsg.UserBriefInfo myUserInfo;
    /** 当前回合信息 */
    private BaseBattle.CurrentTurnInfo currentTurnInfo;
    /**最后一个事件编号*/
    private int lastEventNum;
    /** 我的行动顺序 */
    private int mySeq;
    /** 对手行动顺序 */
    private int opponentSeq;
    /** 我方棋子 */
    private String myPiecesStr;
    /** 敌方棋子 */
    private String opponentPiecesStr;
    /** 是否已加入聊天房间 */
    private boolean joinChatRoom = false;

    public BattleFrame(BaseBattle.BattleInfo battleInfo) {
        UIUtil.framtype();		//改组件样式
        initComponents();
        UIUtil.init(this);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                OnlineClientManager.getInstance().removeSession();
                System.exit(0);
            }
        });
        // 初始化界面信息
        init(battleInfo);
//        viewRefresh();
    }

    /**
     * 聊天文本发送
     * @param e
     */
    private void send_btnMouseClicked(MouseEvent e) {
        String sendText = input_text.getText();
        String str = myUserInfo.getNickname() + ": " + sendText + "\n";
        left_textArea.append(str);
        //添加内容后使滚动条滚动到最底部
        refreshChatBox();
        input_text.setText("");

        CommonMsg.BattleChatTextSendRequest.Builder builder = CommonMsg.BattleChatTextSendRequest.newBuilder();
        builder.setChatRoomType(CommonEnum.ChatRoomTypeEnum.TwoPlayerBattleChatRoomType);
        builder.setBattleChatTextScope(CommonEnum.BattleChatTextScopeEnum.EveryoneScope);
        builder.setSendTimestamp(System.currentTimeMillis());
        builder.setText(sendText);
        NetMessage netMessage = new NetMessage(Rpc.RpcNameEnum.BattleChatText_VALUE, builder);
        MessageManager.getInstance().sendNetMsgToServer(netMessage);
        log.info("推送聊天文本: {}", str);
    }

    /**
     * 投降
     * @param e
     */
    private void surrender_btnMouseClicked(MouseEvent e) {
        log.info("投降....");
        BaseBattle.ConcedeRequest.Builder builder = BaseBattle.ConcedeRequest.newBuilder();
        NetMessage netMessage = new NetMessage(Rpc.RpcNameEnum.Concede_VALUE, builder);
        MessageManager.getInstance().sendNetMsgToServer(netMessage);
    }

    private void index_0MouseClicked(MouseEvent e) {
        placePieces(0);
    }

    private void index_1MouseClicked(MouseEvent e) {
        placePieces(1);
    }

    private void index_2MouseClicked(MouseEvent e) {
        placePieces(2);
    }

    private void index_3MouseClicked(MouseEvent e) {
        placePieces(3);
    }

    private void index_4MouseClicked(MouseEvent e) {
        placePieces(4);
    }

    private void index_5MouseClicked(MouseEvent e) {
        placePieces(5);
    }

    private void index_6MouseClicked(MouseEvent e) {
        placePieces(6);
    }

    private void index_7MouseClicked(MouseEvent e) {
        placePieces(7);
    }

    private void index_8MouseClicked(MouseEvent e) {
        placePieces(8);
    }

    /**
     * 对战界面初始化
     */
    private void init(BaseBattle.BattleInfo battleInfo) {
        // 给各标签框赋值
        List<CommonMsg.UserBriefInfo> list = battleInfo.getUserBriefInfosList();
        opponentUserInfo = getOpponentUserInfo(list);                                       // 对手信息
        mySeq = getSeq(OnlineClientManager.getInstance().getUserInfo().getUserId(), list);  // 我方行动顺序
        myUserInfo = list.get(mySeq-1);
        opponentSeq = getSeq(opponentUserInfo.getUserId(), list);                           // 敌方行动顺序
        lastEventNum = battleInfo.getLastEventNum();
        /** 我方名称 */
        playerName_text.setText(myUserInfo.getNickname());      // 设置我方名称
        /** 对战Id */
        battleId_text.setText(opponentUserInfo.getUserState().getBattleId()); // 设置对战id
        /** 对手名称 */
        opponent_text.setText(opponentUserInfo.getNickname());  // 设置对手名称显示
        /** 加载棋盘 */
        allBattleCellInfo = battleInfo.getBattleCellInfoList();
        firstLoadingCellInfo(allBattleCellInfo);

        // 设置我方棋子
        if (mySeq == 1) {
            myPiecesStr = "X";
            opponentPiecesStr = "O";
        } else {
            myPiecesStr = "O";
            opponentPiecesStr = "X";
        }
        /** 设置我方棋子 */
        pieces_text.setText(myPiecesStr);

        log.info("PlayName: {}",  myUserInfo.getNickname());
        log.info("OpponentUserName: {}", opponentUserInfo.getNickname());
        log.info("BattleId：{}", opponentUserInfo.getUserState().getBattleId());
        log.info("我方棋子：{}", myPiecesStr);
        // 判断我方是否已经确认开始
        List<Integer> notReadyUserIds = battleInfo.getNotReadyUserIdsList();
        if (notReadyUserIds == null || notReadyUserIds.size() < 1) {
            // 游戏开始
            currentTurnInfo = battleInfo.getCurrentTurnInfo();            // 获取回合信息
            if (currentTurnInfo.getUserId() == myUserInfo.getUserId()) {
                /** 回合标签修改 */
                log.info("我方回合");
                round_text.setText("\u6211\u65b9\u56de\u5408"); // 我方回合
            } else {
                /** 回合标签修改 */
                log.info("对方回合");
                round_text.setText("\u5bf9\u65b9\u56de\u5408");  // 对方回合
            }
        } else {
            /** 回合标签修改 */
            log.info("游戏未开始");
            round_text.setText("\u6e38\u620f\u672a\u5f00\u59cb");  // 游戏未开始
            if (notReadyUserIds.contains(myUserInfo.getUserId())); {
                // 我方未准备.放送准备完毕请求
                BaseBattle.ReadyToStartGameRequest.Builder builder = BaseBattle.ReadyToStartGameRequest.newBuilder();
                NetMessage netMessage = new NetMessage(Rpc.RpcNameEnum.ReadyToStartGame_VALUE, builder);
                MessageManager.getInstance().sendNetMsgToServer(netMessage);
            }
        }

    }

    /**
     * 落子事件处理
     */
    public void placePieces(int index) {
        BaseBattle.PlacePiecesRequest.Builder placePiecesRequest = BaseBattle.PlacePiecesRequest.newBuilder();
        log.info("{}", index);
        placePiecesRequest.setIndex(index);
        placePiecesRequest.setLastEventNum(lastEventNum);
        NetMessage netMessage = new NetMessage(Rpc.RpcNameEnum.PlacePieces_VALUE, placePiecesRequest);
        MessageManager.getInstance().sendNetMsgToServer(netMessage);
    }

    /**
     * 第一次加载棋盘
     * @param allBattleCellInfo
     */
    public void firstLoadingCellInfo(List<Integer> allBattleCellInfo) {
        /* 加载棋盘 */
        allIndexJtf = new ArrayList<>();
        allIndexJtf.add(index_0);
        allIndexJtf.add(index_1);
        allIndexJtf.add(index_2);
        allIndexJtf.add(index_3);
        allIndexJtf.add(index_4);
        allIndexJtf.add(index_5);
        allIndexJtf.add(index_6);
        allIndexJtf.add(index_7);
        allIndexJtf.add(index_8);
        for (int i = 0; i < 9; i++) {
            Integer cellInfo = allBattleCellInfo.get(i);
            JTextField indexJtf = allIndexJtf.get(i);
            // 先收X.后手O
            if (cellInfo == 1) {
                indexJtf.setText("X");
            } else if (cellInfo == 0){
                indexJtf.setText("");
            } else {
                indexJtf.setText("O");
            }
        }
    }

    /**
     * 重载棋盘
     */
    public void reloadCellInfo(List<Integer> allBattleCellInfo) {
        for (int i = 0; i < 9; i++) {
            Integer cellInfo = allBattleCellInfo.get(i);
            JTextField indexJtf = allIndexJtf.get(i);
            if (cellInfo == 1) {
                indexJtf.setText("X");
            } else if (cellInfo == 0){
                indexJtf.setText("");
            } else {
                indexJtf.setText("O");
            }
        }
    }

    /**
     * 根据UserId获取行动顺序
     * @param userId
     * @param userBriefInfos
     * @return
     */
    public int getSeq(int userId, List<CommonMsg.UserBriefInfo> userBriefInfos) {
        for (CommonMsg.UserBriefInfo userBriefInfo :userBriefInfos) {
            if (userBriefInfo.getUserId() == OnlineClientManager.getInstance().getUserInfo().getUserId()) {
                int index = userBriefInfos.indexOf(userBriefInfo);
                return index +1;
            }
        }
        return -1;
    }

    /**
     * 获取对手信息
     * @param userBriefInfos
     * @return
     */
    public CommonMsg.UserBriefInfo getOpponentUserInfo(List<CommonMsg.UserBriefInfo> userBriefInfos) {
        for (CommonMsg.UserBriefInfo userBriefInfo :userBriefInfos) {
            if (userBriefInfo.getUserId() != OnlineClientManager.getInstance().getUserInfo().getUserId()) {
                return userBriefInfo;
            }
        }
        return null;
    }

    /**
     * 滚动到文本框底部
     */
    public void refreshChatBox() {
        left_textArea.setCaretPosition(left_textArea.getText().length());
    }

    /**
     * 界面控件刷新
     */
    public void viewRefresh() {
//        playerName_text.updateUI();


//        battleId_text.validate();
//        battleId_text.repaint();
//
//        opponent_text.validate();
//        opponent_text.repaint();
//
//        pieces_text.validate();
//        pieces_text.repaint();
//
//        countdown_text.validate();
//        countdown_text.repaint();
//
//        left_textArea.validate();
//        left_textArea.repaint();

        playerName_text.revalidate();
        battleId_text.revalidate();
        opponent_text.revalidate();
        pieces_text.revalidate();
        round_text.revalidate();
        left_textArea.revalidate();

//        top.updateUI();
//
//        top.validate();
//        top.repaint();
//
////        top.revalidate();
//        center.revalidate();
//        left.revalidate();
//        right.revalidate();
//        down.revalidate();
//        panel1.updateUI();
//        panel2.updateUI();
    }

    /**界面初始化*/
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel1 = new JPanel();
        panel2 = new JPanel();
        input_text = new JTextField();
        send_bt = new JButton();
        scrollPane1 = new JScrollPane();
        left_textArea = new JTextArea();
        panel3 = new JPanel();
        playerName_text = new JTextField();
        battleId_text = new JTextField();
        opponent_text = new JTextField();
        label2 = new JLabel();
        label3 = new JLabel();
        label4 = new JLabel();
        surrender_btn = new JButton();
        panel4 = new JPanel();
        panel5 = new JPanel();
        label6 = new JLabel();
        label1 = new JLabel();
        round_text = new JTextField();
        pieces_text = new JTextField();
        panel6 = new JPanel();
        index_0 = new JTextField();
        index_1 = new JTextField();
        index_2 = new JTextField();
        index_3 = new JTextField();
        index_4 = new JTextField();
        index_5 = new JTextField();
        index_6 = new JTextField();
        index_7 = new JTextField();
        index_8 = new JTextField();

        //======== this ========
        Container contentPane = getContentPane();

        //======== panel1 ========
        {
            panel1.setLayout(new BorderLayout());

            //======== panel2 ========
            {

                //---- send_bt ----
                send_bt.setText("\u53d1\u9001");
                send_bt.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        send_btnMouseClicked(e);
                    }
                });

                //======== scrollPane1 ========
                {

                    //---- left_textArea ----
                    left_textArea.setEditable(false);
                    left_textArea.setEnabled(false);
                    scrollPane1.setViewportView(left_textArea);
                }

                GroupLayout panel2Layout = new GroupLayout(panel2);
                panel2.setLayout(panel2Layout);
                panel2Layout.setHorizontalGroup(
                        panel2Layout.createParallelGroup()
                                .addGroup(panel2Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(panel2Layout.createParallelGroup()
                                                .addComponent(scrollPane1)
                                                .addGroup(panel2Layout.createSequentialGroup()
                                                        .addComponent(input_text, GroupLayout.PREFERRED_SIZE, 133, GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(send_bt, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)))
                                        .addContainerGap(9, Short.MAX_VALUE))
                );
                panel2Layout.setVerticalGroup(
                        panel2Layout.createParallelGroup()
                                .addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(send_bt)
                                                .addComponent(input_text, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap())
                );
            }
            panel1.add(panel2, BorderLayout.WEST);

            //======== panel3 ========
            {
                panel3.setPreferredSize(new Dimension(496, 120));

                //---- playerName_text ----
                playerName_text.setEditable(false);
                playerName_text.setEnabled(false);

                //---- battleId_text ----
                battleId_text.setEditable(false);
                battleId_text.setEnabled(false);

                //---- opponent_text ----
                opponent_text.setEditable(false);
                opponent_text.setEnabled(false);

                //---- label2 ----
                label2.setText("BattleId\uff1a");

                //---- label3 ----
                label3.setText("\u7528\u6237\u540d");

                //---- label4 ----
                label4.setText("\u5bf9  \u624b\uff1a");

                //---- surrender_btn ----
                surrender_btn.setText("\u6295\u964d");
                surrender_btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        surrender_btnMouseClicked(e);
                    }
                });

                GroupLayout panel3Layout = new GroupLayout(panel3);
                panel3.setLayout(panel3Layout);
                panel3Layout.setHorizontalGroup(
                        panel3Layout.createParallelGroup()
                                .addGroup(panel3Layout.createSequentialGroup()
                                        .addGap(24, 24, 24)
                                        .addGroup(panel3Layout.createParallelGroup()
                                                .addComponent(label3)
                                                .addComponent(label2)
                                                .addComponent(label4))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(panel3Layout.createParallelGroup()
                                                .addComponent(battleId_text, GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                                                .addComponent(opponent_text, GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                                                .addComponent(playerName_text, GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE))
                                        .addGap(51, 51, 51)
                                        .addComponent(surrender_btn)
                                        .addGap(39, 39, 39))
                );
                panel3Layout.setVerticalGroup(
                        panel3Layout.createParallelGroup()
                                .addGroup(panel3Layout.createSequentialGroup()
                                        .addGap(17, 17, 17)
                                        .addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label3)
                                                .addComponent(playerName_text, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label2)
                                                .addComponent(battleId_text, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(surrender_btn))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label4)
                                                .addComponent(opponent_text, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            }
            panel1.add(panel3, BorderLayout.NORTH);

            //======== panel4 ========
            {
                panel4.setLayout(new BorderLayout());

                //======== panel5 ========
                {
                    panel5.setPreferredSize(new Dimension(244, 40));

                    //---- label1 ----
                    label1.setText("\u4f60\u7684\u68cb\u5b50\uff1a");

                    //---- round_text ----
                    round_text.setText("\u56de\u5408\u672a\u5f00\u59cb");
                    round_text.setHorizontalAlignment(SwingConstants.CENTER);
                    round_text.setEditable(false);
                    round_text.setEnabled(false);

                    //---- pieces_text ----
                    pieces_text.setText("O");
                    pieces_text.setHorizontalAlignment(SwingConstants.CENTER);
                    pieces_text.setEditable(false);
                    pieces_text.setEnabled(false);

                    GroupLayout panel5Layout = new GroupLayout(panel5);
                    panel5.setLayout(panel5Layout);
                    panel5Layout.setHorizontalGroup(
                            panel5Layout.createParallelGroup()
                                    .addGroup(GroupLayout.Alignment.TRAILING, panel5Layout.createSequentialGroup()
                                            .addContainerGap(154, Short.MAX_VALUE)
                                            .addComponent(label6)
                                            .addGap(125, 125, 125))
                                    .addGroup(panel5Layout.createSequentialGroup()
                                            .addGap(38, 38, 38)
                                            .addComponent(label1)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(pieces_text, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                            .addGap(28, 28, 28)
                                            .addComponent(round_text, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
                                            .addContainerGap(23, Short.MAX_VALUE))
                    );
                    panel5Layout.setVerticalGroup(
                            panel5Layout.createParallelGroup()
                                    .addGroup(GroupLayout.Alignment.TRAILING, panel5Layout.createSequentialGroup()
                                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(panel5Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                    .addComponent(label1)
                                                    .addComponent(round_text, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(pieces_text, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(label6)
                                            .addContainerGap())
                    );
                }
                panel4.add(panel5, BorderLayout.NORTH);

                //======== panel6 ========
                {
                    panel6.setLayout(new GridLayout(3, 3));

                    //---- index_0 ----
                    index_0.setEditable(false);
                    index_0.setEnabled(false);
                    index_0.setHorizontalAlignment(SwingConstants.CENTER);
                    index_0.setFont(index_0.getFont().deriveFont(index_0.getFont().getSize() + 25f));
                    index_0.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            index_0MouseClicked(e);
                        }
                    });
                    panel6.add(index_0);

                    //---- index_1 ----
                    index_1.setEditable(false);
                    index_1.setEnabled(false);
                    index_1.setHorizontalAlignment(SwingConstants.CENTER);
                    index_1.setFont(index_1.getFont().deriveFont(index_1.getFont().getSize() + 25f));
                    index_1.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            index_1MouseClicked(e);
                        }
                    });
                    panel6.add(index_1);

                    //---- index_2 ----
                    index_2.setEditable(false);
                    index_2.setEnabled(false);
                    index_2.setHorizontalAlignment(SwingConstants.CENTER);
                    index_2.setFont(index_2.getFont().deriveFont(index_2.getFont().getSize() + 25f));
                    index_2.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            index_2MouseClicked(e);
                        }
                    });
                    panel6.add(index_2);

                    //---- index_3 ----
                    index_3.setEditable(false);
                    index_3.setEnabled(false);
                    index_3.setHorizontalAlignment(SwingConstants.CENTER);
                    index_3.setFont(index_3.getFont().deriveFont(index_3.getFont().getSize() + 25f));
                    index_3.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            index_3MouseClicked(e);
                        }
                    });
                    panel6.add(index_3);

                    //---- index_4 ----
                    index_4.setEditable(false);
                    index_4.setEnabled(false);
                    index_4.setHorizontalAlignment(SwingConstants.CENTER);
                    index_4.setFont(index_4.getFont().deriveFont(index_4.getFont().getSize() + 25f));
                    index_4.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            index_4MouseClicked(e);
                        }
                    });
                    panel6.add(index_4);

                    //---- index_5 ----
                    index_5.setEditable(false);
                    index_5.setEnabled(false);
                    index_5.setHorizontalAlignment(SwingConstants.CENTER);
                    index_5.setFont(index_5.getFont().deriveFont(index_5.getFont().getSize() + 25f));
                    index_5.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            index_5MouseClicked(e);
                        }
                    });
                    panel6.add(index_5);

                    //---- index_6 ----
                    index_6.setEditable(false);
                    index_6.setEnabled(false);
                    index_6.setHorizontalAlignment(SwingConstants.CENTER);
                    index_6.setFont(index_6.getFont().deriveFont(index_6.getFont().getSize() + 25f));
                    index_6.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            index_6MouseClicked(e);
                        }
                    });
                    panel6.add(index_6);

                    //---- index_7 ----
                    index_7.setEditable(false);
                    index_7.setEnabled(false);
                    index_7.setHorizontalAlignment(SwingConstants.CENTER);
                    index_7.setFont(index_7.getFont().deriveFont(index_7.getFont().getSize() + 25f));
                    index_7.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            index_7MouseClicked(e);
                        }
                    });
                    panel6.add(index_7);

                    //---- index_8 ----
                    index_8.setEditable(false);
                    index_8.setEnabled(false);
                    index_8.setHorizontalAlignment(SwingConstants.CENTER);
                    index_8.setFont(index_8.getFont().deriveFont(index_8.getFont().getSize() + 25f));
                    index_8.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            index_8MouseClicked(e);
                        }
                    });
                    panel6.add(index_8);
                }
                panel4.add(panel6, BorderLayout.CENTER);
            }
            panel1.add(panel4, BorderLayout.CENTER);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel1;
    private JPanel panel2;
    private JTextField input_text;
    private JButton send_bt;
    private JScrollPane scrollPane1;
    private JTextArea left_textArea;
    private JPanel panel3;
    private JTextField playerName_text;
    private JTextField battleId_text;
    private JTextField opponent_text;
    private JLabel label2;
    private JLabel label3;
    private JLabel label4;
    private JButton surrender_btn;
    private JPanel panel4;
    private JPanel panel5;
    private JLabel label6;
    private JLabel label1;
    private JTextField round_text;
    private JTextField pieces_text;
    private JPanel panel6;
    private JTextField index_0;
    private JTextField index_1;
    private JTextField index_2;
    private JTextField index_3;
    private JTextField index_4;
    private JTextField index_5;
    private JTextField index_6;
    private JTextField index_7;
    private JTextField index_8;
    // JFormDesigner - End of variables declaration  //GEN-END:variables




    public static void main(String[] args) {
    }
}
