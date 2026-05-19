package com.gitee.qa.jmeter.protocol.s3.config.gui;

import com.gitee.qa.jmeter.protocol.s3.config.S3ConfigElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public class S3ConfigGui extends AbstractConfigGui {

//    private static final long serialVersionUID = 241L;

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    // 组件
    private JLabeledTextField s3UidName;
    private JLabeledTextField s3EndPoint;
    private JLabeledTextField s3Region;
    private JLabeledTextField S3AccessKeyId;
    private JLabeledTextField S3SecretAccessKey;


    public S3ConfigGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public String getStaticLabel() {
        return "S3 Connection Configuration";
    }


    @Override
    public TestElement createTestElement() {
        S3ConfigElement config = new S3ConfigElement();
        modifyTestElement(config);
        return config;
    }

    @Override
    public void modifyTestElement(TestElement config) {
        S3ConfigElement cfg = (S3ConfigElement) config;
        cfg.clear();

        cfg.setS3UidName(s3UidName.getText());
        cfg.setS3EndPoint(s3EndPoint.getText());
        cfg.setS3Region(s3Region.getText());
        cfg.setS3AccessKeyId(S3AccessKeyId.getText());
        cfg.setS3SecretAccessKey(S3SecretAccessKey.getText());

        super.configureTestElement(config);  // 将GUI_CLASS、TEST_CLASS的内容保存
    }

    @Override
    public void configure(TestElement config) {
        super.configure(config);
        S3ConfigElement cfg = (S3ConfigElement) config;
        s3UidName.setText(cfg.getS3UidName());
        s3EndPoint.setText(cfg.getS3Endpoint());
        s3Region.setText(cfg.getS3Region());
        S3AccessKeyId.setText(cfg.getS3AccessKeyId());
        S3SecretAccessKey.setText(cfg.getS3SecretAccessKey());
    }

    @Override
    public void clearGui() {
        super.clearGui();
        s3UidName.setText("");
        s3EndPoint.setText("");
        s3Region.setText("");
        S3AccessKeyId.setText("");
        S3SecretAccessKey.setText("");
    }

    // Gui
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        // 标题注释
        box.add(makeTitlePanel());
        // 组件
        box.add(getS3UidNamePanel());
        box.add(getS3EndPointPanel());

        add(box, BorderLayout.NORTH);
    }


    /**
     * S3链接唯一标识
     *
     * @return the panel
     * */
    protected final JPanel getS3UidNamePanel() {
        JPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "S3 链接唯一标识")); // $NON-NLS-1$
        s3UidName = new JLabeledTextField("标识名：", 4); // $NON-NLS-1$
        JLabel jl = new JLabel("注意：标识名必须全局唯一");
        jl.setForeground(Color.red);
        jl.setFont(FONT_SMALL);
        panel.add(s3UidName);
        panel.add(jl);
        return panel;
    }

    protected final JPanel getS3EndPointPanel() {
        JPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "S3 链接配置")); // $NON-NLS-1$
        s3EndPoint = new JLabeledTextField("S3 访问地址：", 4); // $NON-NLS-1$
        s3Region = new JLabeledTextField("S3 Region：", 4); // $NON-NLS-1$
        JLabel jl = new JLabel("示例：http://127.0.0.1:9000");
        jl.setForeground(Color.blue);
        jl.setFont(FONT_SMALL);
        S3AccessKeyId = new JLabeledTextField("S3 accessKeyId：", 4); // $NON-NLS-1$
        S3SecretAccessKey = new JLabeledTextField("S3 secretAccessKey：", 4); // $NON-NLS-1$
        panel.add(s3EndPoint);
        panel.add(s3Region);
        panel.add(jl);
        panel.add(S3AccessKeyId);
        panel.add(S3SecretAccessKey);
        return panel;
    }


}