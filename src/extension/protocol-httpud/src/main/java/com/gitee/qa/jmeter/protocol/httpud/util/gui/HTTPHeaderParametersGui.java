package com.gitee.qa.jmeter.protocol.httpud.util.gui;

import com.gitee.qa.jmeter.protocol.httpud.config.HTTPUDConfigElement;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;

import javax.swing.*;
import java.awt.*;

public class HTTPHeaderParametersGui extends JPanel {

    private static final long serialVersionUID = 240L;

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    HTTPHeaderParametersPanel httpHeaderParametersPanel;

    public HTTPHeaderParametersGui(){
        this(false);
    }

    public HTTPHeaderParametersGui(boolean readOnly){
        init();
        if (readOnly) {
            // setReadOnly removed in JMeter 5.6
        }
    }

    private void init(){
        this.setLayout(new BorderLayout());
        httpHeaderParametersPanel = new HTTPHeaderParametersPanel();
        this.add(httpHeaderTabNotePanel(), BorderLayout.NORTH);
        this.add(httpHeaderParametersPanel, BorderLayout.CENTER);
    }

    public void clear() {
        httpHeaderParametersPanel.clear();
    }

    /**
     * Save the GUI values in the sampler.
     */
    public void modifyTestElement(TestElement element) {
        Arguments args;
        args = (Arguments) httpHeaderParametersPanel.createTestElement();
        element.setProperty(new TestElementProperty(HTTPUDConfigElement.HTTP_HEADER_PARAMETERS_NAME, args));
    }

    /**
     * Set the text, etc. in the UI.
     */
    public void configure(TestElement el) {
        setName(el.getName());
        Arguments arguments = (Arguments) el.getProperty(HTTPUDConfigElement.HTTP_HEADER_PARAMETERS_NAME).getObjectValue();
        httpHeaderParametersPanel.configure(arguments);
    }

    // 请求头Tab页注释
    private JPanel httpHeaderTabNotePanel() {
        final JPanel panel = new VerticalPanel();
        JLabel jl = new JLabel("注意: 请求头Tab页中的属性值暂不支持声明自定义参数！\n");
        jl.setForeground(Color.red);
        jl.setFont(FONT_SMALL);
        panel.add(jl);
        return panel;
    }

}
