package com.gitee.qa.jmeter.protocol.httpud.sampler.gui;

import com.gitee.qa.jmeter.protocol.httpud.control.HTTUDIncludeController;
import com.gitee.qa.jmeter.protocol.httpud.config.HTTPUDIncludeConfig;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.AdvancedPanelGui;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.HTTPHeaderParametersGui;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.UrlConfigGui;
import com.gitee.qa.jmeter.protocol.httpud.util.HTTPUDArgument;
import com.gitee.qa.jmeter.protocol.httpud.config.HTTPUDConfigElement;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.HTTPUDArgumentsGui;
import com.gitee.qa.jmeter.protocol.httpud.sampler.HTTPUDSampler;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.gui.action.LookAndFeelCommand;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jmeter.gui.GuiPackage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


public class HTTPUDSamplerGui extends AbstractSamplerGui {

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));
    private static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, (int) Math.round(FONT_DEFAULT.getSize()));

    // 标识名下拉框
    private JComboBox<Map<String, String>> variableNameComboBox;
    private JButton reloadButton;
    private HTTPUDArgumentsGui HTTPUDArgumentsGui;
    private boolean MATCHING = false;  // 标识下拉框是否处于查询状态
    private String MATCH_STR = "";  // 表示下拉框模糊查找字符串

    private JDialog note;
    private JDialog reloadedBeforeNote;

    // 预览区域
    private JPanel previewAreaPanel;
    // 基本tab页
    private UrlConfigGui urlConfigGui;
    // 高级tab页组件
    private AdvancedPanelGui advancedPanelGui;
    // HTTP请求头tab页
    private HTTPHeaderParametersGui httpHeaderParametersGui;

    private List<Map<String, String>> VARIABLE_MAP_LIST = new ArrayList<>();
    private List<Map<String, String>> MATCHED_VARIABLE_MAP_LIST = new ArrayList<>();
    private Map<String, String> DEFAULT_VARIABLE_MAP = new HashMap<>();
    {
        DEFAULT_VARIABLE_MAP.put("缺省值", "点击下拉框选择标识名");
        VARIABLE_MAP_LIST.add(DEFAULT_VARIABLE_MAP);
    }

    public HTTPUDSamplerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public String getStaticLabel() {
        return "HTTP User Defined Sampler";
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        // 标题注释
        box.add(makeTitlePanel());
        // 变量名
        box.add(getVariableNamePanel());
        add(box, BorderLayout.NORTH);
        // 实参
        HTTPUDArgumentsGui = new HTTPUDArgumentsGui("传递参数与值", false, false);
//        HTTPUDArgumentsGui.getTableModel().addTableModelListener(new TableModelListener() {
//            public void tableChanged(TableModelEvent e) {
//                // 手动代码
//                configurePreviewArea();
//            }
//        });

        HTTPUDArgumentsGui.getTable().addMouseListener(new MouseAdapter() {
            // 鼠标点击事件
            public void mouseClicked(MouseEvent e) {
                configurePreviewArea();
            }
        });
        HTTPUDArgumentsGui.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // Tab键通常表示焦点切换
                    // 可以认为表格失去焦点
                    configurePreviewArea();
                }
            }
        });
        // 预览区域
        previewAreaPanel = new JPanel();
        previewAreaPanel.setLayout(new BorderLayout());
        previewAreaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "请求预览(只读)[标识名: ]", TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, Color.red)); // $NON-NLS-1$
        // 基本tab
        urlConfigGui = new UrlConfigGui(true, true, true, true);
        // 高级tab
        advancedPanelGui = new AdvancedPanelGui(true);
        // HTTP请求头tab
        httpHeaderParametersGui = new HTTPHeaderParametersGui(true);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_basic"), urlConfigGui);
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_advanced"), advancedPanelGui);
        tabbedPane.add("请求头", httpHeaderParametersGui);
        previewAreaPanel.add(tabbedPane, BorderLayout.CENTER);
        // 分割线
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, HTTPUDArgumentsGui, previewAreaPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(1);
        add(splitPane);
    }

    @Override
    public TestElement createTestElement() {
        HTTPUDSampler sampler = new HTTPUDSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Save the GUI values in the sampler.
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        super.configureTestElement(sampler);
        sampler.setProperty(HTTPUDSampler.VARIABLE_NAME, getSelectedVariableName());
        sampler.setProperty(HTTPUDSampler.VARIABLE_NAME_DESC, getSelectedVariableNameDesc());
        HTTPUDArgumentsGui.modifyTestElement(sampler);
    }

    /**
     * Show the GUI values from sampler.
     */
    @Override
    public void configure(TestElement sampler) {
        super.configure(sampler);
        String variableName = sampler.getPropertyAsString(HTTPUDSampler.VARIABLE_NAME);
        String variableNameDesc = sampler.getPropertyAsString(HTTPUDSampler.VARIABLE_NAME_DESC);
        Map<String, String> varMap = new HashMap<String, String>() {{ put(variableName, variableNameDesc); }};
        if (comboBoxContain(varMap)) {
            variableNameComboBox.setSelectedItem(varMap);
        } else {
            variableNameComboBox.addItem(varMap);
            variableNameComboBox.setSelectedItem(varMap);
        }
        // 传递参数区域
        HTTPUDArgumentsGui.configure(sampler);
        // 预览区域
        configurePreviewArea();
    }

    @Override
    public void clearGui() {
        super.clearGui();
        MATCH_STR = "";
        MATCHING = false;
        variableNameComboBox.setSelectedIndex(0);
        HTTPUDArgumentsGui.clear();
        if (note != null) {
            note.setVisible(false);
        }
        if (reloadedBeforeNote != null) {
            reloadedBeforeNote.setVisible(false);
        }
        // 预览区域
        clearPreviewArea();
    }

    // 更新预览区域UI
    private void configurePreviewArea() {
        // 查找所有 HTTPUDConfigElement 组件
        Collection<HTTPUDConfigElement> httpUDConfigElements = findHTTPUDConfigElements();
        HTTPUDConfigElement matchedHTTPUDConfigElement = getHTTPUDConfigElement(httpUDConfigElements);
        if (matchedHTTPUDConfigElement != null) {
//            urlConfigGui.configure(matchedHTTPUDConfigElement);
            urlConfigGui.configure(matchedHTTPUDConfigElement, HTTPUDArgumentsGui.getUserDefinedParametersPanel().getParameters());
            advancedPanelGui.configure(matchedHTTPUDConfigElement);
            httpHeaderParametersGui.configure(matchedHTTPUDConfigElement);
        }
        previewAreaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "请求预览(只读)[标识名: " + getSelectedVariableName() + "]", TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, Color.red)); // $NON-NLS-1$
    }

    // Clear预览区域UI
    private void clearPreviewArea() {
        previewAreaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "请求预览(只读)[标识名: ]", TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, Color.red)); // $NON-NLS-1$
        urlConfigGui.clear();
        advancedPanelGui.clear();
        httpHeaderParametersGui.clear();
    }

    /**
     * 获取当前comboBox所选择的标识名
     * */
    private String getSelectedVariableName() {
        Map<String, String> selectedItem = (Map<String, String>) variableNameComboBox.getSelectedItem();
        return selectedItem.keySet().iterator().next();
    }
    /**
     * 获取当前comboBox所选择的标识描述
     * */
    private String getSelectedVariableNameDesc() {
        Map<String, String> selectedItem = (Map<String, String>) variableNameComboBox.getSelectedItem();
        return selectedItem.get(getSelectedVariableName());
    }

    /**
     * 用于判断下拉框列表中是否存在对应的数据项
     * */
    private Boolean comboBoxContain(Object target){
        // 遍历Model判断是否存在于下拉框数据中
        ComboBoxModel<Map<String, String>> model = variableNameComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object item = model.getElementAt(i);
            if (item.equals(target)) {
                return true;
            }
        }
        return false;
    }

    // 变量名
    protected final JPanel getVariableNamePanel() {
        JPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "HTTP请求唯一标识")); // $NON-NLS-1$
        JPanel horizontalPanel = new HorizontalPanel();
        JLabel jl = new JLabel("注意：必须填写已在HTTP User Defined Element Configuration声明过的标识名");
        jl.setForeground(Color.red);
        jl.setFont(FONT_SMALL);
        reloadButton = new JButton("更新参数");
        reloadButton.addActionListener(this::reloadButtonActionListener);
        horizontalPanel.add(new JLabel("标识名: "));
        horizontalPanel.add(getVariableNameComboBox());
        horizontalPanel.add(reloadButton);
        panel.add(horizontalPanel);
        panel.add(jl);
        return panel;
    }

    protected final JComboBox<Map<String, String>> getVariableNameComboBox() {
        variableNameComboBox = new JComboBox<>();
        variableNameComboBox.setModel(new DefaultComboBoxModel(VARIABLE_MAP_LIST.toArray()));
        variableNameComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                // 优化点击下拉框卡顿
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Map<String, String> map = (Map<String, String>) value;
                String name = map.keySet().iterator().next();
                String desc = map.get(name);
                setText(name + "  [" + desc + "]");
                return this;
//                Map<String, String> map = (Map<String, String>) value;
//                String name = map.keySet().iterator().next();
//                String desc = map.get(name);
//                JPanel panel = new HorizontalPanel();
//                JLabel jName = new JLabel(name);
//                JLabel jDesc = new JLabel("  [" + desc + "]");
//                jDesc.setForeground(Color.gray);
//                jDesc.setFont(FONT_SMALL);
//                panel.add(jName);
//                panel.add(jDesc);
//                if (isSelected) {
//                    // 根据主题切换颜色
//                    if (JMeterMenuBar.DARCULA_LAF_CLASS.equals(LookAndFeelCommand.getJMeterLaf())) {
//                        panel.setBackground(new Color(75, 110, 175));
//                    } else {
//                        panel.setBackground(new Color(163, 184, 204));
//                    }
//                }
//                return panel;
            }
        });
        variableNameComboBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                MATCHING = true;
                char keyChar = e.getKeyChar();
                String keyString = String.valueOf(keyChar);
                if (keyChar == KeyEvent.VK_BACK_SPACE) {  // Backspace
                    if (MATCH_STR.length() > 0) {
                        MATCH_STR = MATCH_STR.substring(0, MATCH_STR.length() - 1);
                    }
                } else if (keyChar == KeyEvent.VK_ENTER || keyChar == KeyEvent.VK_PAUSE) {  // Enter Or Ctrl+S
                    MATCH_STR = "";
                    MATCHING = false;
                    return;
                } else if (KeyEvent.VK_ENTER <= keyChar && keyChar <= KeyEvent.VK_HOME) {
                    return;
                } else {
                    MATCH_STR += keyString;
                }
                MATCHED_VARIABLE_MAP_LIST.clear();
                for (Map<String, String> map: VARIABLE_MAP_LIST) {
                    String name = map.keySet().iterator().next();
                    String desc = map.get(name);
                    if (name.toLowerCase().contains(MATCH_STR.toLowerCase()) || desc.toLowerCase().contains(MATCH_STR.toLowerCase())) {
                        MATCHED_VARIABLE_MAP_LIST.add(map);
                    }
                }
                if (MATCHED_VARIABLE_MAP_LIST.isEmpty()) {
                    MATCHED_VARIABLE_MAP_LIST.add(0, new HashMap<String, String>() {{ put(MATCH_STR, "未匹配到任何标识"); }});
                } else {
                    MATCHED_VARIABLE_MAP_LIST.add(0, new HashMap<String, String>() {{ put(MATCH_STR, "匹配到下列标识"); }});
                }
                variableNameComboBox.showPopup();
            }
        });
        variableNameComboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // 下拉框将要显示,可以在这里加载数据
                Object selectedItem = variableNameComboBox.getSelectedItem();
                if (!MATCHING) {
                    MATCH_STR = "";
                    VARIABLE_MAP_LIST.clear();
                    VARIABLE_MAP_LIST.add(DEFAULT_VARIABLE_MAP);
                    Collection<HTTPUDConfigElement> httpUDConfigElements = findHTTPUDConfigElements();
                    for (HTTPUDConfigElement element: httpUDConfigElements) {
                        String elementName = element.getName();
                        String variableName = element.getVariableName();
                        VARIABLE_MAP_LIST.add(new HashMap<String, String>() {{
                            put(variableName, elementName);
                        }});
                    }
                    variableNameComboBox.setModel(new DefaultComboBoxModel(VARIABLE_MAP_LIST.toArray()));
                    variableNameComboBox.setSelectedItem(selectedItem);  // 滚动到指定Item
                } else {
                    variableNameComboBox.setModel(new DefaultComboBoxModel(MATCHED_VARIABLE_MAP_LIST.toArray()));
                    variableNameComboBox.setSelectedIndex(0);  // 滚动到匹配的第一个
                    MATCHING = false;
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // 下拉框将要隐藏,可以做隐藏前的处理
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // 下拉框隐藏被取消
            }
        });
        return variableNameComboBox;
    }

    private void reloadButtonActionListener(ActionEvent evt){
        // 清空预览区域
        clearPreviewArea();
        // 查找所有 HTTPUDConfigElement 组件
        Collection<HTTPUDConfigElement> httpUDConfigElements = this.findHTTPUDConfigElements();

        if (httpUDConfigElements.isEmpty()) {
            this.note("未找到任何 HTTPUDConfigElement 组件\n");
            return;
        }
        // 根据当前标识名更新参数 开始
        // 1. 查找当前标识名对应的 HTTPUDConfigElement
        HTTPUDConfigElement matchedHTTPUDConfigElement = this.getHTTPUDConfigElement(httpUDConfigElements);
        if (matchedHTTPUDConfigElement == null) {
            this.note("未找到标识名为 " + getSelectedVariableName().trim() + " 的 HTTPUDConfigElement 组件\n");
            return;
        }
        // 2. 根据 HTTPUDConfigElement 自定义参数内容更新当前组件内参数
        Map<String, String> parametersValueMap = matchedHTTPUDConfigElement.getUserDefinedParametersValueAsMap();
        Map<String, Boolean> parametersRequiredAsMap = matchedHTTPUDConfigElement.getUserDefinedParametersRequiredAsMap();
        Map<String, String> parametersDescAsMap = matchedHTTPUDConfigElement.getUserDefinedParametersDescAsMap();
        // 2.1. 根据 HTTPUDConfigElement 更新已有参数的描述
        // 2.2. 如果已有参数不在 HTTPUDConfigElement 自定义参数中，则删除该参数
        ArrayList<String> addArgNames = new ArrayList<>();  // 更新后新增的参数名列表
        ArrayList<String> removeArgNames = new ArrayList<>();  // 更新后删除的参数名列表
        Arguments newArgs = new Arguments();
        Arguments currentArgs = HTTPUDArgumentsGui.getUserDefinedParametersPanel().getParameters();
        Map<String, String> currentArgsAsMap = currentArgs.getArgumentsAsMap();
        for (String name: parametersValueMap.keySet()) {
            // 更新参数
            HTTPUDArgument newArg = new HTTPUDArgument();
            newArg.setName(name);
            if (currentArgsAsMap.containsKey(name)) {
                newArg.setValue(currentArgsAsMap.get(name));
            } else {
                newArg.setValue(parametersValueMap.get(name));
            }
            newArg.setDescription(parametersDescAsMap.get(name));
            newArg.setRequired(parametersRequiredAsMap.get(name));
            newArgs.addArgument(newArg);
        }
        // 2.3. 分析哪些参数是新增的哪些参数是被移除
        Map<String, String> newArgsAsMap = newArgs.getArgumentsAsMap();
        for (String name: newArgsAsMap.keySet()) {
            if (!currentArgsAsMap.containsKey(name)) {
                addArgNames.add(name);
            }
        }
        for (String name: currentArgsAsMap.keySet()) {
            if (!newArgsAsMap.containsKey(name)) {
                removeArgNames.add(name);
            }
        }
        // 3. 弹框确认是否更新到参数表格中
        if (!addArgNames.isEmpty() || !removeArgNames.isEmpty()) { // 弹框确认是否新增或者删除参数
            this.reloadedBeforeNote(addArgNames, removeArgNames, newArgs);
        } else { // 不弹框直接更新描述和必填信息
            HTTPUDArgumentsGui.getUserDefinedParametersPanel().configure(newArgs);
        }
        // 根据当前标识名更新参数 结束
        // 根据新的参数和值重置预览区域
        configurePreviewArea();
    }

    // TODO 递归加载include控制器
//    public Collection<IncludeController> loadIncludedControllers(IncludeController controller1){
//        IncludeController controller = (IncludeController) controller1.clone();
//        controller.resolveReplacementSubTree(null);
//        HashTree tree = controller.getReplacementSubTree();
//        SearchByClass<IncludeController> includeControllers =
//                new SearchByClass<>(IncludeController.class);
//        tree.traverse(includeControllers);
//        Collection<IncludeController> includeControllersRes = includeControllers.getSearchResults();
//        for (IncludeController c: includeControllersRes) {
//            includeControllersRes.addAll(this.loadIncludedControllers(c));
//        }
//        return includeControllersRes;
//    }

    /**
     * Recursively build a HashTree with TestElement keys from the JMeterTreeNode tree.
     * This is needed because getTestPlan()/getCurrentSubTree() returns a tree with
     * JMeterTreeNode keys, which SearchByClass cannot match against TestElement subtypes.
     */
    private ListedHashTree buildTestElementTree(JMeterTreeNode node) {
        TestElement te = node.getTestElement();
        ListedHashTree tree = new ListedHashTree(te);
        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode) children.nextElement();
            tree.add(te, buildTestElementTree(child));
        }
        return tree;
    }

    /**
     * 查找被 HTTUDIncludeController 组件引用的 HTTPUDConfigElement 组件 <br>
     * 注意：不支持对嵌套更深层次 HTTUDIncludeController 下的 HTTPUDConfigElement 组件进行查找。（HTTUDIncludeController 中测试片单再嵌套 HTTUDIncludeController 组件）
     * */
    private Collection<HTTPUDConfigElement> findHTTPUDConfigElements() {
        SearchByClass<HTTPUDConfigElement> httpUDConfigElementSearchByClass = new SearchByClass<>(HTTPUDConfigElement.class);
        SearchByClass<HTTUDIncludeController> httUDIncludeControllerSearchByClass = new SearchByClass<>(HTTUDIncludeController.class);  // TODO 可以放弃使用该组件 HTTUDIncludeController，只使用 HTTPUDIncludeConfig
        SearchByClass<HTTPUDIncludeConfig> httpUDIncludeConfigSearchByClass = new SearchByClass<>(HTTPUDIncludeConfig.class);
        // 获取当前JMX下所有 HTTUDIncludeController 组件
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeModel treeModel = guiPackage.getTreeModel();
        // getTestPlan() returns a tree with JMeterTreeNode keys, but SearchByClass
        // checks isAssignableFrom on the key class. JMeterTreeNode is NOT a TestElement,
        // so SearchByClass can never find HTTPUDIncludeConfig etc. in that tree.
        // Build a tree with TestElement keys instead.
        JMeterTreeNode rootNode = (JMeterTreeNode) treeModel.getRoot();
        JMeterTreeNode testPlanNode = (JMeterTreeNode) rootNode.getChildAt(0);
        HashTree tree = buildTestElementTree(testPlanNode);
        // 查找匹配的节点并添加到 httpUDConfigElementSearchByClass.objectsOfClass 中
        tree.traverse(httpUDConfigElementSearchByClass);
        // 查找匹配的节点并添加到 httUDIncludeControllerSearchByClass.objectsOfClass 中
        tree.traverse(httUDIncludeControllerSearchByClass);
        // 查找匹配的节点并添加到 httpUDIncludeConfigSearchByClass.objectsOfClass 中
        tree.traverse(httpUDIncludeConfigSearchByClass);
        // 获取当前脚本中所有匹配到的 HTTUDIncludeController 节点
        Collection<HTTUDIncludeController> httpUDIncludeControllers = httUDIncludeControllerSearchByClass.getSearchResults();
        // 获取当前脚本中所有匹配到的 HTTPUDIncludeConfig 节点
        Collection<HTTPUDIncludeConfig> httpUDIncludeConfigs = httpUDIncludeConfigSearchByClass.getSearchResults();
        // 加载Include控制器中的jmx文件 (这里比较耗时，影响gui流畅度)
        for (HTTUDIncludeController httudIncludeController : httpUDIncludeControllers) {
            if (!httudIncludeController.isEnabled()) continue;
            httudIncludeController.resolveReplacementSubTree(null);
        }
        // 加载HTTPUDIncludeConfig的jmx文件 (这里比较耗时，影响gui流畅度)
        for (HTTPUDIncludeConfig httpUDIncludeConfig : httpUDIncludeConfigs) {
            if (!httpUDIncludeConfig.isEnabled()) continue;
            httpUDIncludeConfig.resolveReplacementSubTree(null);
        }
        // 获取 HTTPUDConfigElement 组件
        for (HTTUDIncludeController httpUDIncludeController : httpUDIncludeControllers) {
            if (!httpUDIncludeController.isEnabled()) continue; // 跳过禁用状态的控制器
            HashTree hashTree = httpUDIncludeController.getReplacementSubTree();
            if (hashTree == null) continue;  // 如果include文件路径错误，会导致hashtree为null
            hashTree.traverse(httpUDConfigElementSearchByClass);
        }
        // 获取 HTTPUDConfigElement 组件
        for (HTTPUDIncludeConfig httpUDIncludeConfig : httpUDIncludeConfigs) {
            if (!httpUDIncludeConfig.isEnabled()) continue; // 跳过禁用状态的控制器
            HashTree hashTree = httpUDIncludeConfig.getReplacementSubTree();
            if (hashTree == null) continue;  // 如果include文件路径错误，会导致hashtree为null
            hashTree.traverse(httpUDConfigElementSearchByClass);
        }
        // 获取当前脚本中所有匹配到的 HTTPUDConfigElement 节点，并进行去重和状态判断处理
        Collection<HTTPUDConfigElement> httpUDConfigElements = httpUDConfigElementSearchByClass.getSearchResults();
        Collection<HTTPUDConfigElement> filteredHTTPUDConfigElements = new ArrayList<>();
        ArrayList<String> httpUDConfigElementNames = new ArrayList<>();
        for (HTTPUDConfigElement httpUDConfigElement: httpUDConfigElements) {
            String name = httpUDConfigElement.getVariableName();
            if (httpUDConfigElement.isEnabled() && !httpUDConfigElementNames.contains(name)) {
                httpUDConfigElementNames.add(name);
                filteredHTTPUDConfigElements.add(httpUDConfigElement);
            }
        }
        return filteredHTTPUDConfigElements;
    }

    /**
     * 查找当前标识名对应的 HTTPUDConfigElement
     * */
    private HTTPUDConfigElement getHTTPUDConfigElement(Collection<HTTPUDConfigElement> httpUDConfigElements) {
        String name = getSelectedVariableName();

        for (HTTPUDConfigElement httpudConfigElement: httpUDConfigElements) {
            // 如果存在多个匹配，则返回第一个，与HTTPUDConfigElement.testStarted方法逻辑一致
            if (httpudConfigElement.isEnabled() && name.equals(httpudConfigElement.getVariableName())) {
                return httpudConfigElement;
            }
        }
        return null;
    }

    /**
     * 弹框提示消息
     * */
    private void note(String message) {
        JFrame mainFrame = GuiPackage.getInstance().getMainFrame();
        JDialog dialog = initDialog(mainFrame, message);

        // NOTE: these lines center the about dialog in the current window.
        Point p = mainFrame.getLocationOnScreen();
        Dimension d1 = mainFrame.getSize();
        Dimension d2 = dialog.getSize();
        dialog.setLocation(p.x + (d1.width - d2.width) / 2, p.y + (d1.height - d2.height) / 2);
        dialog.setVisible(true);
    }

    private JDialog initDialog(JFrame mainFrame, String message) {
        if (note != null) {
            note.setVisible(false);
        }
        note = new EscapeDialog(mainFrame, "提示", false);
        note.setMinimumSize(new Dimension(500, 200));
        note.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                note.setVisible(false);
            }
        });

        JLabel jLabelMessage = new JLabel(message, SwingConstants.CENTER);
        JPanel infos = new JPanel();
        infos.setOpaque(false);
        infos.setLayout(new GridLayout(0, 1));
        infos.setBorder(new EmptyBorder(5, 5, 5, 5));
        infos.add(jLabelMessage);

        Container panel = note.getContentPane();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.white);
        panel.add(infos, BorderLayout.CENTER);

        note.pack();
        return note;
    }

    /**
     * 更新参数前弹框确认更新
     * */
    private void reloadedBeforeNote(ArrayList<String> addArgNames,  ArrayList<String> removeArgNames, Arguments newArgs) {
        JFrame mainFrame = GuiPackage.getInstance().getMainFrame();
        JDialog dialog = initReloadedBeforeNoteDialog(mainFrame, addArgNames, removeArgNames, newArgs);

        // NOTE: these lines center the about dialog in the current window.
        Point p = mainFrame.getLocationOnScreen();
        Dimension d1 = mainFrame.getSize();
        Dimension d2 = dialog.getSize();
        dialog.setLocation(p.x + (d1.width - d2.width) / 2, p.y + (d1.height - d2.height) / 2);
        dialog.setVisible(true);
    }

    private JDialog initReloadedBeforeNoteDialog(JFrame mainFrame, ArrayList<String> addArgNames,  ArrayList<String> removeArgNames, Arguments newArgs) {
        if (reloadedBeforeNote != null) {
            reloadedBeforeNote.setVisible(false);
        }
        reloadedBeforeNote = new EscapeDialog(mainFrame, "提示", false);
        reloadedBeforeNote.setMinimumSize(new Dimension(700, 300));
        reloadedBeforeNote.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                reloadedBeforeNote.setVisible(false);
            }
        });

        JTextArea jAddArgsMessage = new JTextArea("[添加参数]\n" + String.join("\n", addArgNames));
        jAddArgsMessage.setEditable(false);
        jAddArgsMessage.setForeground(new Color(12, 177, 0));
        jAddArgsMessage.setFont(FONT_SMALL);
        JTextArea jRemoveArgsMessage = new JTextArea("[移除参数]\n" + String.join("\n", removeArgNames));
        jRemoveArgsMessage.setEditable(false);
        jRemoveArgsMessage.setForeground(Color.red);
        jRemoveArgsMessage.setFont(FONT_SMALL);

        JPanel infos = new JPanel();
        infos.setOpaque(false);
        infos.setLayout(new GridLayout(0, 1));
        infos.setBorder(new EmptyBorder(5, 5, 5, 5));
        infos.add(jAddArgsMessage);
        infos.add(jRemoveArgsMessage);

        JButton confirmButton = new JButton("确认更新");
        JButton cancelButton = new JButton("取消更新");
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                reloadedBeforeNote.setVisible(false);
            }
        });
        confirmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                HTTPUDArgumentsGui.getUserDefinedParametersPanel().configure(newArgs);
                reloadedBeforeNote.setVisible(false);
                // 重置预览区域
                configurePreviewArea();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        Container panel = reloadedBeforeNote.getContentPane();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.white);
        panel.add(infos, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        reloadedBeforeNote.pack();
        return reloadedBeforeNote;
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
}