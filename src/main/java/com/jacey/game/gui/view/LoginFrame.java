/*
 * Created by JFormDesigner on Fri Jun 12 22:03:01 CST 2020
 */

package com.jacey.game.gui.view;

import java.awt.event.*;

import com.jacey.game.gui.manager.ConfigManager;
import com.jacey.game.gui.manager.MessageManager;
import com.jacey.game.gui.manager.OnlineClientManager;
import com.jacey.game.gui.msg.NetMessage;
import com.jacey.game.gui.proto3.CommonMsg;
import com.jacey.game.gui.proto3.Rpc;
import com.jacey.game.gui.utils.MD5Util;
import com.jacey.game.gui.utils.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 注册模块
 */
public class LoginFrame extends JFrame{

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel playerName_lab;
    private JLabel password_lab;
    private JTextField playerName_text;
    private JPasswordField password_text;
    private JButton registered_btn;
    private JButton login_btn;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public LoginFrame() {
        UIUtil.framtype();		//改组件样式
        initComponents();
        //窗口关闭时
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                OnlineClientManager.getInstance().removeSession();
                System.exit(0);
            }
        });
    }



    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        playerName_lab = new JLabel();
        password_lab = new JLabel();
        playerName_text = new JTextField();
        password_text = new JPasswordField();
        registered_btn = new JButton();
        login_btn = new JButton();

        //======== this ========
        Container contentPane = getContentPane();

        //---- playerName_lab ----
        playerName_lab.setText("\u7528\u6237\u540d");

        //---- password_lab ----
        password_lab.setText("\u5bc6 \u7801\uff1a");

        //---- registered_btn ----
        registered_btn.setText("\u6ce8\u518c");
        registered_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                registered_btnMouseClicked(e);
            }
        });

        //---- login_btn ----
        login_btn.setText("\u767b\u5f55");
        login_btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                login_btnMouseClicked(e);
            }
        });

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(password_lab, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                                        .addComponent(playerName_lab, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(registered_btn)
                                                .addGap(25, 25, 25)
                                                .addComponent(login_btn))
                                        .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(password_text, GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                                                .addComponent(playerName_text, GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)))
                                .addGap(36, 36, 36))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(playerName_lab, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(playerName_text, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(password_lab, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(password_text, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(login_btn)
                                        .addComponent(registered_btn))
                                .addContainerGap(22, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
        UIUtil.init(this);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents


    }

    public static void main(String[] args) {
        ConfigManager.getInstance().init();
        new LoginFrame();
    }


    /**
     * 注册按钮。鼠标点击事件处理
     * @param e
     */
    private void registered_btnMouseClicked(MouseEvent e) {
        if (OnlineClientManager.getInstance().isConnectionServer == false) {
            String str = "Server not connected !! Server Host = "+ ConfigManager.SERVER_HOST + ":" + ConfigManager.SERVER_PORT;
            JOptionPane.showMessageDialog(null, str);
        } else {
            String playerName = playerName_text.getText();
            String password = new String(password_text.getPassword());
            CommonMsg.RegistRequest.Builder registRequest = CommonMsg.RegistRequest.newBuilder();
            registRequest.setUsername(playerName);
            registRequest.setPassword(password);
            NetMessage netMessage = new NetMessage(Rpc.RpcNameEnum.Regist_VALUE, registRequest);
            MessageManager.getInstance().sendNetMsgToServer(netMessage);
        }
    }

    /**
     * 登录按钮。鼠标点击事件处理
     * @param e
     */
    private void login_btnMouseClicked(MouseEvent e)  {
        if (OnlineClientManager.getInstance().isConnectionServer == false) {
            String str = "Server not connected !! Server Host = "+ ConfigManager.SERVER_HOST + ":" + ConfigManager.SERVER_PORT;
            JOptionPane.showMessageDialog(null, str);
        } else {
            String playerName = playerName_text.getText();
            String password = new String(password_text.getPassword());
            CommonMsg.LoginRequest.Builder loginRequest = CommonMsg.LoginRequest.newBuilder();
            loginRequest.setUsername(playerName);
            loginRequest.setPasswordMD5(MD5Util.md5(password));
            NetMessage netMessage = new NetMessage(Rpc.RpcNameEnum.Login_VALUE, loginRequest);
            MessageManager.getInstance().sendNetMsgToServer(netMessage);
        }
    }

}
