/*
 * Created by JFormDesigner on Fri Jun 12 22:21:14 CST 2020
 */

package com.jacey.game.gui.view;

import com.jacey.game.gui.manager.MessageManager;
import com.jacey.game.gui.manager.OnlineClientManager;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.CommonEnum;
import com.jacey.game.gui.proto3.CommonMsg;
import com.jacey.game.gui.proto3.Rpc;
import com.jacey.game.gui.utils.UIUtil;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 大厅
 */
@Getter
@Setter
public class HallFrame extends JFrame {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel palyerName_lab;
    private JTextField palyerName_text;
    private JButton match_btn;
    private JButton unmatch_btn;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private CommonMsg.UserInfo userInfo;

    public HallFrame(CommonMsg.UserInfo userInfo) {
        this.userInfo = userInfo;
        UIUtil.framtype();		//改组件样式
        initComponents();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                OnlineClientManager.getInstance().removeSession();
                System.exit(0);
            }
        });
        palyerName_text.setText(userInfo.getNickname());
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        palyerName_lab = new JLabel();
        palyerName_text = new JTextField();
        match_btn = new JButton();
        unmatch_btn = new JButton();

        //======== this ========
        Container contentPane = getContentPane();

        //---- palyerName_lab ----
        palyerName_lab.setText("\u7528\u6237\u540d:");

        //---- palyerName_text ----
        palyerName_text.setEditable(false);
        palyerName_text.setText("\u5f20\u4e09");

        //---- match_btn ----
        match_btn.setText("\u5339\u914d");
        match_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                match_btnMouseClicked(e);
            }
        });

        //---- unmatch_btn ----
        unmatch_btn.setText("\u53d6\u6d88\u5339\u914d");
        unmatch_btn.setEnabled(false);
        unmatch_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                unmatch_btnMouseClicked(e);
            }
        });

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(palyerName_lab, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(palyerName_text, GroupLayout.PREFERRED_SIZE, 144, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(unmatch_btn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(match_btn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(45, 45, 45))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGap(46, 46, 46)
                                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(palyerName_lab, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(palyerName_text, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGap(23, 23, 23)
                                                .addComponent(match_btn)
                                                .addGap(28, 28, 28)
                                                .addComponent(unmatch_btn)))
                                .addContainerGap(27, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
        UIUtil.init(this);
    }

    /**
     * 匹配按钮点击事件监听
     * @param e
     */
    private void match_btnMouseClicked(MouseEvent e) {
        // 发起匹配
        CommonMsg.MatchRequest.Builder builder = CommonMsg.MatchRequest.newBuilder();
        builder.setBattleTypeValue(CommonEnum.BattleTypeEnum.BattleTypeTwoPlayer_VALUE);
        NetMessage netMessage = new NetMessage(Rpc.RpcNameEnum.Match_VALUE, builder);
        MessageManager.getInstance().sendNetMsgToServer(netMessage);
        // 当接收到匹配响应时才把取消匹配按钮恢复
    }

    /**
     * 取消匹配按钮事件监听
     * @param e
     */
    private void unmatch_btnMouseClicked(MouseEvent e) {
        // 发起匹配
        CommonMsg.CancelMatchRequest.Builder builder = CommonMsg.CancelMatchRequest.newBuilder();
        NetMessage netMessage = new NetMessage(Rpc.RpcNameEnum.CancelMatch_VALUE, builder);
        MessageManager.getInstance().sendNetMsgToServer(netMessage);
    }


    public static void main(String[] args) {
        CommonMsg.UserInfo.Builder builder = CommonMsg.UserInfo.newBuilder();
        builder.setNickname("Jacey");
        new HallFrame(builder.build());
    }
}
