package com.gitee.qa.jmeter.protocol.httpud.config.gui;

import com.gitee.qa.jmeter.protocol.httpud.config.HTTPUDIncludeConfig;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

public class HTTPUDIncludeConfigGui extends AbstractConfigGui {

    private final FilePanel includePanel =
            new FilePanel(JMeterUtils.getResString("include_path"), ".jmx"); //$NON-NLS-1$ //$NON-NLS-2$


    public HTTPUDIncludeConfigGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public String getStaticLabel() {
        return "HTTP User Defined Include Configuration";
    }


    @Override
    public void configure(TestElement element) {
        super.configure(element);
        HTTPUDIncludeConfig config = (HTTPUDIncludeConfig)element;
        this.includePanel.setFilename(config.getIncludePath());
    }

    @Override
    public TestElement createTestElement() {
        HTTPUDIncludeConfig config = new HTTPUDIncludeConfig();
        modifyTestElement(config);
        return config;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);  // 将GUI_CLASS、TEST_CLASS的内容保存
        HTTPUDIncludeConfig config = (HTTPUDIncludeConfig)element;
        config.setIncludePath(this.includePanel.getFilename());
    }

    @Override
    public void clearGui() {
        super.clearGui();
        this.includePanel.clearGui();
    }

    private void init() {
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());

        add(includePanel);
    }
}
