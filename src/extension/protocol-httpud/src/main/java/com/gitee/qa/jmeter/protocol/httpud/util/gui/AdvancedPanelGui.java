package com.gitee.qa.jmeter.protocol.httpud.util.gui;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class AdvancedPanelGui extends VerticalPanel {

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private boolean readOnly;

    // 高级tab页组件
    private JComboBox<String> httpImplementation = new JComboBox<>(HTTPSamplerFactory.getImplementations());
    private JTextField connectTimeOut;
    private JTextField responseTimeOut;
    private JCheckBox retrieveEmbeddedResources;
    private JCheckBox concurrentDwn;
    private JTextField concurrentPool;
    private JCheckBox useMD5;
    private JLabeledTextField embeddedRE; // regular expression used to match against embedded resource URLs
    private JTextField sourceIpAddr; // does not apply to Java implementation
    private JComboBox<String> sourceIpType = new JComboBox<>(HTTPSamplerBase.getSourceTypeList());
    private JTextField proxyScheme;
    private JTextField proxyHost;
    private JTextField proxyPort;
    private JTextField proxyUser;
    private JPasswordField proxyPass;

    public AdvancedPanelGui (){
        this(false);
    }

    public AdvancedPanelGui (boolean readOnly){
        init();
        this.readOnly = readOnly;
        if (readOnly) {
            httpImplementation.setEditable(false);
            httpImplementation.setEnabled(false);
            connectTimeOut.setEditable(false);
            connectTimeOut.setEnabled(false);
            responseTimeOut.setEditable(false);
            responseTimeOut.setEnabled(false);
            retrieveEmbeddedResources.setEnabled(false);
            concurrentDwn.setEnabled(false);
            concurrentPool.setEditable(false);
            concurrentPool.setEnabled(false);
            useMD5.setEnabled(false);
            embeddedRE.setEnabled(false);
            sourceIpAddr.setEditable(false);
            sourceIpAddr.setEnabled(false);
            sourceIpType.setEditable(false);
            sourceIpType.setEnabled(false);
            proxyScheme.setEditable(false);
            proxyScheme.setEnabled(false);
            proxyHost.setEditable(false);
            proxyHost.setEnabled(false);
            proxyPort.setEditable(false);
            proxyPort.setEnabled(false);
            proxyUser.setEditable(false);
            proxyUser.setEnabled(false);
            proxyPass.setEditable(false);
            proxyPass.setEnabled(false);
        }
    }

    private void init() {
        // HTTP request options
        JPanel httpOptions = new HorizontalPanel();
        httpOptions.add(getImplementationPanel());
        httpOptions.add(getTimeOutPanel());
        add(advancedTabNotePanel());
        add(httpOptions);
        add(createEmbeddedRsrcPanel());
        add(createSourceAddrPanel());
        add(getProxyServerPanel());
        add(createOptionalTasksPanel());
    }

    /**
     * Save the GUI values in the sampler.
     *
     * @param element {@link TestElement} to modify
     */
    public void modifyTestElement(TestElement element) {
        element.setProperty(HTTPSamplerBase.IMPLEMENTATION, httpImplementation.getSelectedItem().toString(),"");
        element.setProperty(HTTPSamplerBase.CONNECT_TIMEOUT, connectTimeOut.getText());
        element.setProperty(HTTPSamplerBase.RESPONSE_TIMEOUT, responseTimeOut.getText());
        if (retrieveEmbeddedResources.isSelected()) {
            element.setProperty(new BooleanProperty(HTTPSamplerBase.IMAGE_PARSER, true));
        } else {
            element.removeProperty(HTTPSamplerBase.IMAGE_PARSER);
        }
        enableConcurrentDwn(retrieveEmbeddedResources.isSelected());
        if (concurrentDwn.isSelected()) {
            element.setProperty(new BooleanProperty(HTTPSamplerBase.CONCURRENT_DWN, true));
        } else {
            // The default is false, so we can remove the property to simplify JMX files
            // This also allows HTTPDefaults to work for this checkbox
            element.removeProperty(HTTPSamplerBase.CONCURRENT_DWN);
        }
        if(!StringUtils.isEmpty(concurrentPool.getText())) {
            element.setProperty(new StringProperty(HTTPSamplerBase.CONCURRENT_POOL,
                    concurrentPool.getText()));
        } else {
            element.setProperty(new StringProperty(HTTPSamplerBase.CONCURRENT_POOL,
                    String.valueOf(HTTPSamplerBase.CONCURRENT_POOL_SIZE)));
        }
        if(useMD5.isSelected()) {
            element.setProperty(new BooleanProperty(HTTPSamplerBase.MD5, true));
        } else {
            element.removeProperty(HTTPSamplerBase.MD5);
        }
        if (!StringUtils.isEmpty(embeddedRE.getText())) {
            element.setProperty(new StringProperty(HTTPSamplerBase.EMBEDDED_URL_RE,
                    embeddedRE.getText()));
        } else {
            element.removeProperty(HTTPSamplerBase.EMBEDDED_URL_RE);
        }

        if(!StringUtils.isEmpty(sourceIpAddr.getText())) {
            element.setProperty(new StringProperty(HTTPSamplerBase.IP_SOURCE,
                    sourceIpAddr.getText()));
            element.setProperty(new IntegerProperty(HTTPSamplerBase.IP_SOURCE_TYPE,
                    sourceIpType.getSelectedIndex()));
        } else {
            element.removeProperty(HTTPSamplerBase.IP_SOURCE);
            element.removeProperty(HTTPSamplerBase.IP_SOURCE_TYPE);
        }
        element.setProperty(HTTPSamplerBase.PROXYSCHEME, proxyScheme.getText(),"");
        element.setProperty(HTTPSamplerBase.PROXYHOST, proxyHost.getText(),"");
        element.setProperty(HTTPSamplerBase.PROXYPORT, proxyPort.getText(),"");
        element.setProperty(HTTPSamplerBase.PROXYUSER, proxyUser.getText(),"");
        element.setProperty(HTTPSamplerBase.PROXYPASS, String.valueOf(proxyPass.getPassword()),"");
    }

    /**
     * Set the text, etc. in the UI.
     *
     * @param el
     *            contains the data to be displayed
     */
    public void configure(TestElement el) {
        httpImplementation.setSelectedItem(el.getPropertyAsString(HTTPSamplerBase.IMPLEMENTATION));
        connectTimeOut.setText(el.getPropertyAsString(HTTPSamplerBase.CONNECT_TIMEOUT));
        responseTimeOut.setText(el.getPropertyAsString(HTTPSamplerBase.RESPONSE_TIMEOUT));
        retrieveEmbeddedResources.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.IMAGE_PARSER));
        concurrentDwn.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.CONCURRENT_DWN));
        concurrentPool.setText(el.getPropertyAsString(HTTPSamplerBase.CONCURRENT_POOL));
        useMD5.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.MD5, false));
        embeddedRE.setText(el.getPropertyAsString(HTTPSamplerBase.EMBEDDED_URL_RE, ""));//$NON-NLS-1$
        sourceIpAddr.setText(el.getPropertyAsString(HTTPSamplerBase.IP_SOURCE)); //$NON-NLS-1$
        sourceIpType.setSelectedIndex(
                el.getPropertyAsInt(HTTPSamplerBase.IP_SOURCE_TYPE,
                        HTTPSamplerBase.SOURCE_TYPE_DEFAULT));
        proxyScheme.setText(el.getPropertyAsString(HTTPSamplerBase.PROXYSCHEME));
        proxyHost.setText(el.getPropertyAsString(HTTPSamplerBase.PROXYHOST));
        proxyPort.setText(el.getPropertyAsString(HTTPSamplerBase.PROXYPORT));
        proxyUser.setText(el.getPropertyAsString(HTTPSamplerBase.PROXYUSER));
        proxyPass.setText(el.getPropertyAsString(HTTPSamplerBase.PROXYPASS));
    }

    public void clear() {
        httpImplementation.setSelectedItem(""); // $NON-NLS-1$
        connectTimeOut.setText(""); // $NON-NLS-1$
        responseTimeOut.setText(""); // $NON-NLS-1$
        retrieveEmbeddedResources.setSelected(false);
        concurrentDwn.setSelected(false);
        concurrentPool.setText(String.valueOf(HTTPSamplerBase.CONCURRENT_POOL_SIZE));
        useMD5.setSelected(false);
        embeddedRE.setText(""); // $NON-NLS-1$
        sourceIpAddr.setText(""); // $NON-NLS-1$
        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        proxyScheme.setText(""); // $NON-NLS-1$
        proxyHost.setText(""); // $NON-NLS-1$
        proxyPort.setText(""); // $NON-NLS-1$
        proxyUser.setText(""); // $NON-NLS-1$
        proxyPass.setText(""); // $NON-NLS-1$
    }

    protected JPanel createOptionalTasksPanel() {
        // OPTIONAL TASKS
        final JPanel checkBoxPanel = new VerticalPanel();
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("optional_tasks"))); // $NON-NLS-1$

        // Use MD5
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$

        checkBoxPanel.add(useMD5);

        return checkBoxPanel;
    }

    private JPanel getProxySchemePanel() {
        proxyScheme = new JTextField(5);

        JLabel label = new JLabel(JMeterUtils.getResString("web_proxy_scheme")); // $NON-NLS-1$
        label.setLabelFor(proxyScheme);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyScheme, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getProxyHostPanel() {
        proxyHost = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain")); // $NON-NLS-1$
        label.setLabelFor(proxyHost);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyHost, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getProxyPortPanel() {
        proxyPort = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$
        label.setLabelFor(proxyPort);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyPort, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getProxyUserPanel() {
        proxyUser = new JTextField(5);

        JLabel label = new JLabel(JMeterUtils.getResString("username")); // $NON-NLS-1$
        label.setLabelFor(proxyUser);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyUser, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getProxyPassPanel() {
        proxyPass = new JPasswordField(5);

        JLabel label = new JLabel(JMeterUtils.getResString("password")); // $NON-NLS-1$
        label.setLabelFor(proxyPass);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyPass, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create a panel containing the proxy server details
     *
     * @return the panel
     */
    protected final JPanel getProxyServerPanel(){
        JPanel proxyServer = new HorizontalPanel();
        proxyServer.add(getProxySchemePanel(), BorderLayout.WEST);
        proxyServer.add(getProxyHostPanel(), BorderLayout.CENTER);
        proxyServer.add(getProxyPortPanel(), BorderLayout.EAST);

        JPanel proxyLogin = new HorizontalPanel();
        proxyLogin.add(getProxyUserPanel());
        proxyLogin.add(getProxyPassPanel());

        JPanel proxyServerPanel = new HorizontalPanel();
        proxyServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_proxy_server_title"))); // $NON-NLS-1$
        proxyServerPanel.add(proxyServer);
        proxyServerPanel.add(proxyLogin);

        return proxyServerPanel;
    }

    protected JPanel createSourceAddrPanel() {
        final JPanel sourceAddrPanel = new HorizontalPanel();
        sourceAddrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_source_ip"))); // $NON-NLS-1$

        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        sourceAddrPanel.add(sourceIpType);

        sourceIpAddr = new JTextField();
        sourceAddrPanel.add(sourceIpAddr);
        return sourceAddrPanel;
    }

    private void enableConcurrentDwn(final boolean enable) {
        if (enable && !this.readOnly) {
            concurrentDwn.setEnabled(true);
            embeddedRE.setEnabled(true);
            if (concurrentDwn.isSelected()) {
                concurrentPool.setEnabled(true);
            }
        } else {
            concurrentDwn.setEnabled(false);
            concurrentPool.setEnabled(false);
            embeddedRE.setEnabled(false);
        }
    }

    protected JPanel createEmbeddedRsrcPanel() {
        // retrieve Embedded resources
        retrieveEmbeddedResources = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$
        // add a listener to activate or not concurrent dwn.
        retrieveEmbeddedResources.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) { enableConcurrentDwn(true); }
            else { enableConcurrentDwn(false); }
        });
        // Download concurrent resources
        concurrentDwn = new JCheckBox(JMeterUtils.getResString("web_testing_concurrent_download")); // $NON-NLS-1$
        concurrentDwn.addItemListener(e -> {
            if (retrieveEmbeddedResources.isSelected() && e.getStateChange() == ItemEvent.SELECTED) { concurrentPool.setEnabled(true); }
            else { concurrentPool.setEnabled(false); }
        });
        concurrentPool = new JTextField(2); // 2 columns size
        concurrentPool.setMinimumSize(new Dimension(10, (int) concurrentPool.getPreferredSize().getHeight()));
        concurrentPool.setMaximumSize(new Dimension(30, (int) concurrentPool.getPreferredSize().getHeight()));

        final JPanel embeddedRsrcPanel = new HorizontalPanel();
        embeddedRsrcPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_retrieve_title"))); // $NON-NLS-1$
        embeddedRsrcPanel.add(retrieveEmbeddedResources);
        embeddedRsrcPanel.add(concurrentDwn);
        embeddedRsrcPanel.add(concurrentPool);

        // Embedded URL match regex
        embeddedRE = new JLabeledTextField(JMeterUtils.getResString("web_testing_embedded_url_pattern"),20); // $NON-NLS-1$
        embeddedRsrcPanel.add(embeddedRE);

        return embeddedRsrcPanel;
    }

    private JPanel getConnectTimeOutPanel() {
        connectTimeOut = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_connect")); // $NON-NLS-1$
        label.setLabelFor(connectTimeOut);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(connectTimeOut, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getResponseTimeOutPanel() {
        responseTimeOut = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_response")); // $NON-NLS-1$
        label.setLabelFor(responseTimeOut);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(responseTimeOut, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getTimeOutPanel() {
        JPanel timeOut = new HorizontalPanel();
        timeOut.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server_timeout_title"))); // $NON-NLS-1$
        final JPanel connPanel = getConnectTimeOutPanel();
        final JPanel reqPanel = getResponseTimeOutPanel();
        timeOut.add(connPanel);
        timeOut.add(reqPanel);
        return timeOut;
    }

    /**
     * Create a panel containing the implementation details
     *
     * @return the panel
     */
    protected final JPanel getImplementationPanel(){
        JPanel implPanel = new HorizontalPanel();
        implPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server_client"))); // $NON-NLS-1$
        implPanel.add(new JLabel(JMeterUtils.getResString("http_implementation"))); // $NON-NLS-1$
        httpImplementation.addItem("");// $NON-NLS-1$
        implPanel.add(httpImplementation);
        return implPanel;
    }

    // 高级Tab页注释
    private JPanel advancedTabNotePanel() {
        final JPanel panel = new VerticalPanel();
        JLabel jl = new JLabel("注意: 高级Tab页中的属性值暂不支持声明自定义参数！\n");
        jl.setForeground(Color.red);
        jl.setFont(FONT_SMALL);
        panel.add(jl);
        return panel;
    }
}
