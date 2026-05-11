package com.gitee.qa.jmeter.threads.common.gui;

import com.gitee.qa.jmeter.threads.PerforAutoThreadGroup;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public class PerforAutoArgsPanel extends JPanel {

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private JTextField perforAutoArgs;  // 性能自动化参数

    public PerforAutoArgsPanel() {
        super(new BorderLayout(5, 0));
        init();
    }

    private void init() {
        // 性能自动化参数
        this.add(new JLabel("可选参数"), BorderLayout.WEST);
        perforAutoArgs = new JTextField();
        JPanel labelPanel = new VerticalPanel();
        JLabel descJLabel = new JLabel("参数说明：");
        descJLabel.setForeground(Color.red);
        descJLabel.setFont(FONT_SMALL);
        JLabel arg1JLabel = new JLabel("  --group-by=<测试场景标识> 可选参数，用于汇总在同一时间段多个线程组同时执行的压测结果数据，通常可使用在混合压测场景");
        arg1JLabel.setForeground(Color.red);
        arg1JLabel.setFont(FONT_SMALL);
        JLabel arg2JLabel = new JLabel("  --slave-count=<分布式发压机数量> 可选参数，用于汇总分布式压测时多台发压机的压测结果数据");
        arg2JLabel.setForeground(Color.red);
        arg2JLabel.setFont(FONT_SMALL);
        labelPanel.add(descJLabel);
        labelPanel.add(arg1JLabel);
        labelPanel.add(arg2JLabel);
        this.add(perforAutoArgs, BorderLayout.CENTER);
        this.add(labelPanel, BorderLayout.SOUTH);
    }

    public void modifyTestElement(TestElement tg) {
        tg.setProperty(PerforAutoThreadGroup.PERFOR_AUTO_ARGS, perforAutoArgs.getText().trim());
    }

    public void configure(TestElement tg) {
        perforAutoArgs.setText(tg.getPropertyAsString(PerforAutoThreadGroup.PERFOR_AUTO_ARGS));
    }

    public void initGui() {
        perforAutoArgs.setText("");
    }

}
