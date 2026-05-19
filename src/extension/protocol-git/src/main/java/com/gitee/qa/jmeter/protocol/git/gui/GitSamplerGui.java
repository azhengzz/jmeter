package com.gitee.qa.jmeter.protocol.git.gui;

import com.gitee.qa.jmeter.protocol.git.GitSampler;
import com.gitee.qa.jmeter.protocol.git.util.GitArguments;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.gui.JLabeledChoice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

public class GitSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 241L;

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN,
            (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private JRadioButton rbUseSSH;
    private JRadioButton rbUseHTTP;
    private JTextField tfSshKey;
    private JLabel jlSshKey;
    private JTextField tfUserName;
    private JLabel jlUserName;
    private JTextField tfUserPassword;
    private JLabel jlUserPassword;
    private ButtonGroup bgGitProtocol;
    private GitArgumentsPanel argsPanel;
    private JLabeledChoice lcAction;
    private JLabel jlAction;
    private JButton addGitActionButton;
    private JPanel gitClonePanel;

    public GitSamplerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public String getStaticLabel() {
        return "Git Sampler";
    }

    /**
     * Show the GUI values from sampler.
     */
    @Override
    public void configure(TestElement sampler) {
        super.configure(sampler);
        tfSshKey.setText(sampler.getPropertyAsString(GitSampler.SSH_KEY_PATH));
        tfUserName.setText(sampler.getPropertyAsString(GitSampler.USER_NAME));
        tfUserPassword.setText(sampler.getPropertyAsString(GitSampler.USER_PASSWORD));
        lcAction.setText(sampler.getPropertyAsString(GitSampler.ACTION));
        jlAction.setText(sampler.getPropertyAsString(GitSampler.ACTION));
        rbUseSSH.setSelected(sampler.getPropertyAsBoolean(GitSampler.USE_SSH_PROTOCOL));
        rbUseHTTP.setSelected(sampler.getPropertyAsBoolean(GitSampler.USE_HTTP_PROTOCOL));
        argsPanel.configure((GitArguments) sampler.getProperty(GitSampler.ARGUMENTS).getObjectValue());
        argsPanel.gitArgsPanelConfigure(sampler.getPropertyAsString(GitSampler.ACTION), false);
        gitClonePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Git " + lcAction.getText()));
    }

    @Override
    public TestElement createTestElement() {
        GitSampler sampler = new GitSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Save the GUI values in the sampler.
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        super.configureTestElement(sampler);
        sampler.setProperty(GitSampler.SSH_KEY_PATH, tfSshKey.getText());
        sampler.setProperty(GitSampler.USER_NAME, tfUserName.getText());
        sampler.setProperty(GitSampler.USER_PASSWORD, tfUserPassword.getText());
        sampler.setProperty(GitSampler.ACTION, jlAction.getText());
        sampler.setProperty(GitSampler.USE_SSH_PROTOCOL, Boolean.toString(this.isUseSSH()));
        sampler.setProperty(GitSampler.USE_HTTP_PROTOCOL, Boolean.toString(this.isUseHTTP()));
        sampler.setProperty(new TestElementProperty(GitSampler.ARGUMENTS, argsPanel.createTestElement()));
    }

    @Override
    public void clearGui() {
        super.clearGui();
        rbUseSSH.setSelected(true);
        rbUseHTTP.setSelected(false);
        tfUserName.setText("");
        tfUserPassword.setText("");
        tfSshKey.setEditable(true);
        tfUserName.setEditable(false);
        tfUserPassword.setEditable(false);
        lcAction.setText("Clone");
        jlAction.setText("Clone");
        argsPanel.clearGui();
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(this.createGitProtocolPanel());
        box.add(this.createGitActionPanel());
        add(box, BorderLayout.NORTH);
        add(this.createGitArgumentsPanel(), BorderLayout.CENTER);
    }

    private JPanel createGitActionPanel(){
        JPanel gitActionPanel = new JPanel();
        gitActionPanel.setLayout(new BorderLayout());
        gitActionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "选择Git动作")); // $NON-NLS-1$

        lcAction = new JLabeledChoice("动作", // $NON-NLS-1$
                new String[] {"Clone", "Add", "Commit", "Push", "Pull", "Branch"}, false, false);
        jlAction = new JLabel();
        jlAction.setVisible(false);
        addGitActionButton = new JButton("确定"); // $NON-NLS-1$
        addGitActionButton.addActionListener(this::addGitActionPerformed);
        JPanel panel =  new HorizontalPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(lcAction);
        panel.add(jlAction);
        panel.add(addGitActionButton);
        gitActionPanel.add(panel);

        return gitActionPanel;
    }

    private JPanel createGitProtocolPanel(){
        JPanel gitProtocolPanel = new JPanel();
        gitProtocolPanel.setLayout(new BorderLayout());
        gitProtocolPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "选择Git协议")); // $NON-NLS-1$
        JPanel gitProtocolVerticalPanel = new VerticalPanel();


        rbUseSSH = new JRadioButton("SSH"); // $NON-NLS-1$
        rbUseHTTP = new JRadioButton("HTTP"); // $NON-NLS-1$

        rbUseSSH.setSelected(true);
        bgGitProtocol = new ButtonGroup();
        bgGitProtocol.add(rbUseSSH);
        bgGitProtocol.add(rbUseHTTP);

        rbUseSSH.addItemListener(this::rbGitProtocolItemStateChanged);
        rbUseHTTP.addItemListener(this::rbGitProtocolItemStateChanged);

        jlSshKey = new JLabel("id_rsa路径");
        jlUserName = new JLabel("用户名");
        jlUserPassword = new JLabel("密码");
        tfSshKey = new JTextField(40);
        tfUserName = new JTextField(20);
        tfUserPassword = new JTextField(20);

        JPanel panel1 =  new HorizontalPanel();

        panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel1.add(rbUseSSH);
        panel1.add(rbUseHTTP);
        gitProtocolVerticalPanel.add(panel1);

        JPanel panel2 =  new HorizontalPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel2.add(jlSshKey);
        panel2.add(tfSshKey);
        panel2.add(jlUserName);
        panel2.add(tfUserName);
        panel2.add(jlUserPassword);
        panel2.add(tfUserPassword);
        gitProtocolVerticalPanel.add(panel2);

        JPanel panel3 =  new VerticalPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel jl1 = new JLabel("注意: id_rsa路径填写的是私钥!!!\n");
        jl1.setForeground(Color.red);
        jl1.setFont(FONT_SMALL);
        panel3.add(jl1);
        JLabel jl2 = new JLabel("注意: windows下路径分隔符为\"/\"，例如：C:/Users/ZhangSan/.ssh/id_rsa");
        jl2.setForeground(Color.red);
        jl2.setFont(FONT_SMALL);
        panel3.add(jl2);
        JLabel jl3 = new JLabel("注意: 如果报错invalid privatekey，可以尝试使用命令重新生成私钥，命令：ssh-keygen -t rsa 或 ssh-keygen -t rsa -m PEM");
        jl3.setForeground(Color.red);
        jl3.setFont(FONT_SMALL);
        panel3.add(jl3);
        JLabel jl4 = new JLabel("注意: 如果报错Auth fail，通常是公钥没有添加到git服务端，可以尝试重新添加公钥");
        jl4.setForeground(Color.red);
        jl4.setFont(FONT_SMALL);
        panel3.add(jl4);
        gitProtocolVerticalPanel.add(panel3);

        gitProtocolPanel.add(gitProtocolVerticalPanel);
        return gitProtocolPanel;
    }

    private JPanel createGitArgumentsPanel() {
        gitClonePanel = new JPanel();
        gitClonePanel.setLayout(new BorderLayout());
        gitClonePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Git " + lcAction.getText())); // $NON-NLS-1$

//        JPanel gitCloneVerticalPanel = new VerticalPanel();
        argsPanel = new GitArgumentsPanel("参数");
//        argsPanel.add(Box.createVerticalStrut(200), BorderLayout.WEST); // 设置面板的高度
//        gitCloneVerticalPanel.add(argsPanel);
//        gitClonePanel.add(gitCloneVerticalPanel);

        gitClonePanel.add(argsPanel, BorderLayout.CENTER);
        return gitClonePanel;
    }

    private void addGitActionPerformed(ActionEvent evt){
        jlAction.setText(lcAction.getText());
        argsPanel.gitArgsPanelConfigure(lcAction.getText(), true);
        gitClonePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Git " + lcAction.getText()));
    }

    private void rbGitProtocolItemStateChanged(ItemEvent evt){
        final Object source = evt.getSource();
        if (source == rbUseSSH) {
            tfSshKey.setEditable(true);
            tfUserName.setEditable(false);
            tfUserPassword.setEditable(false);
        } else if (source == rbUseHTTP) {
            tfSshKey.setEditable(false);
            tfUserName.setEditable(true);
            tfUserPassword.setEditable(true);
        }
    }

    private boolean isUseSSH() {
        return rbUseSSH.isSelected();
    }

    private boolean isUseHTTP() {
        return rbUseHTTP.isSelected();
    }

}
