package com.jacey.game.gui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

//import test2.RegisterFrame;

public class UIUtil {

    //换样式
    public static void framtype() {

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());  // win10样式
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setUIFont() {
        Font f = new Font("宋体",Font.PLAIN,18);
        String names[]={ "Label", "CheckBox", "PopupMenu","MenuItem", "CheckBoxMenuItem",
                "JRadioButtonMenuItem","ComboBox", "Button", "Tree", "ScrollPane",
                "TabbedPane", "EditorPane", "TitledBorder", "Menu", "TextArea",
                "OptionPane", "MenuBar", "ToolBar", "ToggleButton", "ToolTip",
                "ProgressBar", "TableHeader", "Panel", "List", "ColorChooser",
                "PasswordField","TextField", "Table", "Label", "Viewport",
                "RadioButtonMenuItem","RadioButton", "DesktopPane", "InternalFrame"
        };
        for (String item : names) {
            UIManager.put(item+ ".font",f);
        }
    }


    //初始化
    public static void init(JFrame jf) {
        jf.setTitle("[Tic-Tac-Toe]-ByJacey");
//    	jf.setLocationRelativeTo(null); // 窗口置于中央
        jf.setResizable(false);  // 窗口不可变
        jf.setVisible(true);	// 窗口显示
    }




}