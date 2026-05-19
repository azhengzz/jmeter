package com.gitee.qa.jmeter.protocol.s3.gui;

import com.gitee.qa.jmeter.protocol.s3.config.gui.S3ArgumentsPanel;
import com.gitee.qa.jmeter.protocol.s3.sampler.S3Sampler;
import com.gitee.qa.jmeter.protocol.s3.util.S3Action;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class S3SamplerGui extends AbstractSamplerGui {

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    // 组件
    private JLabeledTextField s3UidName;
    private JLabeledChoice lcAction;
    private JLabel jlAction;
    private JLabel jlActionDesc;
    private JButton actionButton;
    private JPanel S3ActionArgumentsPanel;
    private S3ArgumentsPanel argsPanel;


    public S3SamplerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public String getStaticLabel() {
        return "S3 Sampler";
    }

    @Override
    public TestElement createTestElement() {
        S3Sampler sampler = new S3Sampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        S3Sampler sampler = (S3Sampler) element;
        sampler.setUidName(s3UidName.getText());
        sampler.setAction(jlAction.getText());  // 避免Ctrl+S时触发更新参数
        sampler.setArguments(argsPanel.createTestElement());
    }

    /**
     * Show the GUI values from sampler.
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        S3Sampler sampler = (S3Sampler) element;
        s3UidName.setText(sampler.getUidName());
        lcAction.setText(sampler.getAction());
        jlAction.setText(sampler.getAction());
        argsPanel.configure(sampler.getArguments());
        argsPanel.s3ArgsPanelConfigure(lcAction.getText(), false);
        S3ActionArgumentsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "S3 " + lcAction.getText()));
        jlActionDesc.setText(S3Action.getDescriptionByCode(lcAction.getText()));
    }

    @Override
    public void clearGui() {
        super.clearGui();
        s3UidName.setText("");
        lcAction.setText(S3Action.LIST_ALL_BUCKETS.getCode());
        jlAction.setText(S3Action.LIST_ALL_BUCKETS.getCode());
        jlActionDesc.setText(S3Action.LIST_ALL_BUCKETS.getDescription());
        argsPanel.clearGui();
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(getS3UidNamePanel());
        box.add(this.createChooseActionPanel());
        add(box, BorderLayout.NORTH);
        add(this.createS3ActionArgumentsPanel(), BorderLayout.CENTER);
    }

    protected final JPanel getS3UidNamePanel() {
        JPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "S3 链接标识")); // $NON-NLS-1$
        s3UidName = new JLabeledTextField("标识名：", 4); // $NON-NLS-1$
        JLabel jl = new JLabel("注意：标识必须在S3 Connection Configuration组件中已定义");
        jl.setForeground(Color.red);
        jl.setFont(FONT_SMALL);
        panel.add(s3UidName);
        panel.add(jl);
        return panel;
    }

    private JPanel createChooseActionPanel(){
        JPanel chooseActionPanel = new JPanel();
        chooseActionPanel.setLayout(new BorderLayout());
        chooseActionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "选择S3操作")); // $NON-NLS-1$

        lcAction = new JLabeledChoice("操作：", // $NON-NLS-1$
                new String[] {
                        S3Action.LIST_ALL_BUCKETS.getCode(),
                        S3Action.CREATE_BUCKET.getCode(),
                        S3Action.DELETE_BUCKET.getCode(),
                        S3Action.BUCKET_EXISTS.getCode(),
                        S3Action.LIST_OBJECTS.getCode(),
                        S3Action.GET_FILE_METADATA.getCode(),
                        S3Action.UPLOAD_FILE.getCode(),
                        S3Action.DOWNLOAD_FILE.getCode(),
                        S3Action.DELETE_FILE.getCode(),
                        S3Action.FILE_EXISTS.getCode(),
                }, false, false);
        jlAction = new JLabel();
        jlAction.setVisible(false);
        actionButton = new JButton("确定"); // $NON-NLS-1$
        actionButton.addActionListener(this::actionPerformed);
        jlActionDesc = new JLabel();
        jlActionDesc.setForeground(Color.blue);
        JPanel panel =  new HorizontalPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(lcAction);
        panel.add(jlAction);
        panel.add(actionButton);
        panel.add(jlActionDesc);
        chooseActionPanel.add(panel);

        return chooseActionPanel;
    }

    private JPanel createS3ActionArgumentsPanel() {
        S3ActionArgumentsPanel = new JPanel();
        S3ActionArgumentsPanel.setLayout(new BorderLayout());
        S3ActionArgumentsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "S3 " + lcAction.getText())); // $NON-NLS-1$
        argsPanel = new S3ArgumentsPanel("参数");
        S3ActionArgumentsPanel.add(argsPanel, BorderLayout.CENTER);
        return S3ActionArgumentsPanel;
    }

    private void actionPerformed(ActionEvent evt){
        jlAction.setText(lcAction.getText());
        // 更新参数表格
        argsPanel.s3ArgsPanelConfigure(lcAction.getText(), true);
        S3ActionArgumentsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "S3 " + lcAction.getText()));
        jlActionDesc.setText(S3Action.getDescriptionByCode(lcAction.getText()));
    }


}
