package com.gitee.qa.jmeter.protocol.httpud.config.gui;
import com.gitee.qa.jmeter.protocol.httpud.util.HTTPUDArgument;

import com.gitee.qa.jmeter.protocol.httpud.config.HTTPUDConfigElement;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.AdvancedPanelGui;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.HTTPHeaderParametersGui;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.HTTPUDArgumentsGui;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.UrlConfigGui;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public class HTTPUDConfigElementGui extends AbstractConfigGui {

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    // 基本tab组件
    private UrlConfigGui urlConfigGui;
    // 自定义参数组件
    private HTTPUDArgumentsGui HTTPUDArgumentsGui;
    private JLabeledTextField variableName;
    // 高级tab页组件
    private AdvancedPanelGui advancedPanelGui;
    // HTTP请求头tab页
    private HTTPHeaderParametersGui httpHeaderParametersGui;

    public HTTPUDConfigElementGui(){
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public String getStaticLabel() {
        return "HTTP User Defined Element Configuration";
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        // 标题注释
        box.add(makeTitlePanel());
        // 变量名
        box.add(getVariableNamePanel());

        // 基本tab URL CONFIG
        urlConfigGui = new UrlConfigGui(true, true, true);
        // 高级tab
        advancedPanelGui = new AdvancedPanelGui();
        // HTTP请求头tab
        httpHeaderParametersGui = new HTTPHeaderParametersGui();
        // 自定义参数tab
        HTTPUDArgumentsGui = new HTTPUDArgumentsGui("填写自定义参数与默认值");
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_basic"), urlConfigGui);
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_advanced"), advancedPanelGui);
        tabbedPane.add("请求头", httpHeaderParametersGui);
        tabbedPane.add("自定义参数", HTTPUDArgumentsGui);
        add(box, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    @Override
    public TestElement createTestElement() {
        HTTPUDConfigElement config = new HTTPUDConfigElement();
        modifyTestElement(config);
        return config;
    }

    @Override
    public void modifyTestElement(TestElement config) {
        HTTPUDConfigElement cfg = (HTTPUDConfigElement) config;
        cfg.clear();
        // 改用直接传当前组件来保存各面板数据
        urlConfigGui.modifyTestElement(cfg);
        advancedPanelGui.modifyTestElement(cfg);
        HTTPUDArgumentsGui.modifyTestElement(cfg);
        httpHeaderParametersGui.modifyTestElement(cfg);
        super.configureTestElement(config);  // 将GUI_CLASS、TEST_CLASS的内容保存
        config.setProperty(HTTPUDConfigElement.VARIABLE_NAME, variableName.getText());
    }

    @Override
    public void configure(TestElement config) {
        super.configure(config);
        urlConfigGui.configure(config);
        advancedPanelGui.configure(config);
        HTTPUDArgumentsGui.configure(config);
        httpHeaderParametersGui.configure(config);
        variableName.setText(config.getPropertyAsString(HTTPUDConfigElement.VARIABLE_NAME));
    }

    @Override
    public void clearGui() {
        super.clearGui();
        urlConfigGui.clear();
        advancedPanelGui.clear();
        HTTPUDArgumentsGui.clear();
        httpHeaderParametersGui.clear();
        variableName.setText("");
    }

    // 变量名
    protected final JPanel getVariableNamePanel() {
        JPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "HTTP请求唯一标识")); // $NON-NLS-1$
        variableName = new JLabeledTextField("标识名：", 4); // $NON-NLS-1$
        JLabel jl = new JLabel("注意：标识名必须全局唯一，否则会被其他同名组件覆盖");
        jl.setForeground(Color.red);
        jl.setFont(FONT_SMALL);
        panel.add(variableName);
        panel.add(jl);
        return panel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
}
