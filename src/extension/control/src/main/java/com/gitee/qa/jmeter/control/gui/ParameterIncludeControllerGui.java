/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.gitee.qa.jmeter.control.gui;

import com.gitee.qa.jmeter.control.ParameterIncludeController;
import com.gitee.qa.jmeter.control.ParameterTestFragmentController;
import com.gitee.qa.jmeter.control.util.ParameterIncludeControllerArgument;
import com.gitee.qa.jmeter.control.util.ParameterIncludeControllerReturnValueArgument;
import com.gitee.qa.jmeter.control.util.ParameterTestFragmentReturnValueArgument;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ParameterIncludeControllerGui extends AbstractControllerGui{

    private static final Logger log = LoggerFactory.getLogger(ParameterIncludeControllerGui.class);

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));
    private static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, (int) Math.round(FONT_DEFAULT.getSize()));

    private static final long serialVersionUID = 240L;

    private final FilePanel includePanel =
        new FilePanel(JMeterUtils.getResString("include_path"), ".jmx"); //$NON-NLS-1$ //$NON-NLS-2$
    private JButton reloadButton;
    private ParameterIncludeControllerArgumentsPanel parameterIncludeControllerArgumentsPanel;
    private ParameterIncludeControllerReturnValuePanel parameterIncludeControllerReturnValuePanel;

    // 入参更新提示框
    private JDialog argsReloadedBeforeNoteDialog;
    // 返回变量更新提示框
    private JDialog returnValueArgsReloadedBeforeNoteDialog;

    private ParameterIncludeController controller;

    /**
     * Initializes the gui panel for the ModuleController instance.
     */
    public ParameterIncludeControllerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public String getStaticLabel() {
        return "Include控制器(带参数)";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        ParameterIncludeController controller = (ParameterIncludeController) el;
        this.includePanel.setFilename(controller.getIncludePath());
        Arguments arguments = (Arguments) controller.getProperty(ParameterIncludeController.PARAMETER_INCLUDE_CONTROLLER_ARGUMENTS).getObjectValue();
        if (arguments == null){
            parameterIncludeControllerArgumentsPanel.configure(new Arguments());;
        } else {
            parameterIncludeControllerArgumentsPanel.configure(arguments);
        }
        Arguments returnValueArguments = (Arguments) controller.getProperty(ParameterIncludeController.PARAMETER_INCLUDE_CONTROLLER_RETURN_VALUE_ARGUMENTS).getObjectValue();
        if (returnValueArguments == null){
            parameterIncludeControllerReturnValuePanel.configure(new Arguments());;
        } else {
            parameterIncludeControllerReturnValuePanel.configure(returnValueArguments);
        }

        // 保存 controller 对象
        this.controller = controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        ParameterIncludeController pic = new ParameterIncludeController();
        modifyTestElement(pic);
        return pic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        ParameterIncludeController controller = (ParameterIncludeController)element;
        controller.setIncludePath(this.includePanel.getFilename());
        Arguments args = (Arguments) parameterIncludeControllerArgumentsPanel.createTestElement();
        controller.setProperty(new TestElementProperty(ParameterIncludeController.PARAMETER_INCLUDE_CONTROLLER_ARGUMENTS, args));
        Arguments returnValueArgs = (Arguments) parameterIncludeControllerReturnValuePanel.createTestElement();
        controller.setProperty(new TestElementProperty(ParameterIncludeController.PARAMETER_INCLUDE_CONTROLLER_RETURN_VALUE_ARGUMENTS, returnValueArgs));
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        includePanel.clearGui();
        parameterIncludeControllerArgumentsPanel.clearGui();
        parameterIncludeControllerReturnValuePanel.clearGui();
    }

    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        MenuFactory.addEditMenu(menu, true);
        MenuFactory.addFileMenu(menu);
        return menu;
    }

    private JPanel createArgumentsPanel() {
        JPanel argumentsPanel = new JPanel();
        argumentsPanel.setLayout(new BorderLayout());
        argumentsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "传递参数与值")); // $NON-NLS-1$
        parameterIncludeControllerArgumentsPanel = new ParameterIncludeControllerArgumentsPanel("参数与值", false, false, false);
        argumentsPanel.add(parameterIncludeControllerArgumentsPanel, BorderLayout.CENTER);
        return argumentsPanel;
    }

    private JPanel createReturnValueArgumentsPanel() {
        JPanel argumentsPanel = new JPanel();
        argumentsPanel.setLayout(new BorderLayout());
        argumentsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "引用返回值并保存到变量")); // $NON-NLS-1$
        parameterIncludeControllerReturnValuePanel = new ParameterIncludeControllerReturnValuePanel("定义变量名引用返回值", false, false);
        argumentsPanel.add(parameterIncludeControllerReturnValuePanel, BorderLayout.CENTER);
        return argumentsPanel;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());

        JPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.add(includePanel);
        reloadButton = new JButton("更新参数");
        reloadButton.addActionListener(this::reloadButtonActionListener);
        horizontalPanel.add(reloadButton);
        box.add(horizontalPanel);

        add(box, BorderLayout.NORTH);

        // 创建分割面板，设置初始比例
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(this.createArgumentsPanel());
        splitPane.setBottomComponent(this.createReturnValueArgumentsPanel());
        splitPane.setDividerLocation(0.7); // 70% 给上部面板
        splitPane.setResizeWeight(0.7); // 调整时保持比例
        add(splitPane, BorderLayout.CENTER);
    }

    private void reloadButtonActionListener(ActionEvent evt){
        // 加载Include控制器中的jmx文件 (这里比较耗时)
//        this.controller.resolveReplacementSubTree(null);
        // 获取jmx文件中的测试片段
        TestFragmentController controller = this.controller.getTestFragmentController(this.includePanel.getFilename());
        ParameterTestFragmentController parameterTestFragmentController;
        if (controller == null) {
            log.error("'Include控制器(带参数)'组件引用的脚本中未找到任何'测试片段'组件，请检查");
            return;
        }
        if (!(controller instanceof ParameterTestFragmentController)) {
            log.warn("'Include控制器(带参数)'组件引用的测试片段并非是组件'测试片段(带参数)'，没有查找到定义的参数数据");
            return;
        } else {
            parameterTestFragmentController = (ParameterTestFragmentController) controller;
        }
        // 更新参数面板
        updateArgumentsPanel(parameterTestFragmentController);
        updateReturnValueArgumentsPanel(parameterTestFragmentController);
    }

    /**
     * 根据测试片段中定义的入参，更新Include控制器中的参数
     * */
    private void updateArgumentsPanel(ParameterTestFragmentController parameterTestFragmentController) {
        ArrayList<String> addArgNames = new ArrayList<>();  // 更新后新增的参数名列表
        ArrayList<String> removeArgNames = new ArrayList<>();  // 更新后删除的参数名列表

        // 当前Include控制器的参数
        Arguments currentArgs = parameterIncludeControllerArgumentsPanel.getParameters();
        Map<String, String> currentArgValueMap = currentArgs.getArgumentsAsMap();

        // 测试片段控制器的参数
        Arguments tfArgs = parameterTestFragmentController.getArguments();

        // 构造新的参数
        Arguments newArgs = new Arguments();

        // 遍历测试片段中定义的参数
        PropertyIterator tfArgsIter = tfArgs.iterator();
        while (tfArgsIter.hasNext()) {
            ParameterIncludeControllerArgument tfArg = (ParameterIncludeControllerArgument) tfArgsIter.next().getObjectValue();
            // 如果当前Include控制器中设置的参数包含测试片段中定义的参数，则参数值不变
            if (currentArgs.getArgumentsAsMap().containsKey(tfArg.getName())) {
                ParameterIncludeControllerArgument arg = new ParameterIncludeControllerArgument(
                        tfArg.getName(),
                        currentArgValueMap.get(tfArg.getName()),
                        tfArg.getDescription(),
                        tfArg.isNotNull(),
                        tfArg.isRequired()
                );
                newArgs.addArgument(arg);
            } else {  // 如果当前参数中没有测试片段中定义的参数，则加载显示
                newArgs.addArgument(tfArg);
            }
        }

        // 分析哪些参数是新增的哪些参数是被移除
        Map<String, String> newArgsAsMap = newArgs.getArgumentsAsMap();
        for (String name: newArgsAsMap.keySet()) {
            if (!currentArgValueMap.containsKey(name)) {
                addArgNames.add(name);
            }
        }
        for (String name: currentArgValueMap.keySet()) {
            if (!newArgsAsMap.containsKey(name)) {
                removeArgNames.add(name);
            }
        }

        // 弹框确认是否更新到参数表格中
        if (!addArgNames.isEmpty() || !removeArgNames.isEmpty()) { // 弹框确认是否新增或者删除参数
            this.reloadedBeforeNote(parameterIncludeControllerArgumentsPanel, addArgNames, removeArgNames, newArgs);
        } else { // 不弹框直接更新描述和必填信息
            parameterIncludeControllerArgumentsPanel.configure(newArgs);
        }
    }

    /**
     * 根据测试片段中定义的返回值，更新Include控制器中的返回参数
     * */
    private void updateReturnValueArgumentsPanel(ParameterTestFragmentController parameterTestFragmentController) {
        ArrayList<String> addArgNames = new ArrayList<>();  // 更新后新增的返回变量列表
        ArrayList<String> removeArgNames = new ArrayList<>();  // 更新后删除的返回变量列表

        // 当前Include控制器的参数
        Arguments currentRVArgs = parameterIncludeControllerReturnValuePanel.getParameters();
        Map<String, String> currentRVArgValueMap = currentRVArgs.getArgumentsAsMap();

        // 测试片段控制器的参数
        Arguments tfRVArgs = parameterTestFragmentController.getReturnValueArguments();

        // 构造新的参数
        Arguments newArgs = new Arguments();

        // 遍历测试片段中定义的返回参数
        PropertyIterator tfRVArgsIter = tfRVArgs.iterator();
        while (tfRVArgsIter.hasNext()) {
            ParameterTestFragmentReturnValueArgument tfRVArg = (ParameterTestFragmentReturnValueArgument) tfRVArgsIter.next().getObjectValue();  // 测试片段中定义的返回参数
            // 如果当前Include控制器中设置的参数包含测试片段中定义的参数，则参数值不变
            if (currentRVArgs.getArgumentsAsMap().containsKey(tfRVArg.getName())) {
                ParameterIncludeControllerReturnValueArgument arg = new ParameterIncludeControllerReturnValueArgument(
                        tfRVArg.getName(),
                        currentRVArgValueMap.get(tfRVArg.getName()),
                        tfRVArg.getDescription()
                );
                newArgs.addArgument(arg);
            } else {  // 如果当前参数中没有测试片段中定义的参数，则加载显示
                newArgs.addArgument(tfRVArg);
            }
        }

        // 分析哪些参数是新增的哪些参数是被移除
        Map<String, String> newArgsAsMap = newArgs.getArgumentsAsMap();
        for (String name: newArgsAsMap.keySet()) {
            if (!currentRVArgValueMap.containsKey(name)) {
                addArgNames.add(name);
            }
        }
        for (String name: currentRVArgValueMap.keySet()) {
            if (!newArgsAsMap.containsKey(name)) {
                removeArgNames.add(name);
            }
        }

        // 弹框确认是否更新到参数表格中
        if (!addArgNames.isEmpty() || !removeArgNames.isEmpty()) { // 弹框确认是否新增或者删除参数
            this.reloadedBeforeNote(parameterIncludeControllerReturnValuePanel, addArgNames, removeArgNames, newArgs);
        } else { // 不弹框直接更新描述
            parameterIncludeControllerReturnValuePanel.configure(newArgs);
        }
    }


    private Map<String, String> getArgumentsDescAsMap(Arguments args) {
        PropertyIterator iter = args.iterator();
        Map<String, String> argMap = new LinkedHashMap<>();
        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next().getObjectValue();
            // Because CollectionProperty.mergeIn will not prevent adding two
            // properties of the same name, we need to select the first value so
            // that this element's values prevail over defaults provided by
            // configuration
            // elements:
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), arg.getDescription());
            }
        }
        return argMap;
    }

    /**
     * 更新参数前弹框确认更新
     * */
    private void reloadedBeforeNote(ArgumentsPanel argsPanel, ArrayList<String> addArgNames, ArrayList<String> removeArgNames, Arguments newArgs) {
        JFrame mainFrame = GuiPackage.getInstance().getMainFrame();
        JDialog dialog;

        if (argsPanel instanceof ParameterIncludeControllerArgumentsPanel) {
            dialog = initArgsReloadedBeforeNoteDialog(mainFrame, addArgNames, removeArgNames, newArgs);
        } else if (argsPanel instanceof  ParameterIncludeControllerReturnValuePanel) {
            dialog = initReturnValueReloadedBeforeNoteDialog(mainFrame, addArgNames, removeArgNames, newArgs);
        } else {
            return;
        }
        // NOTE: these lines center the about dialog in the current window.
        Point p = mainFrame.getLocationOnScreen();
        Dimension d1 = mainFrame.getSize();
        Dimension d2 = dialog.getSize();
        dialog.setLocation(p.x + (d1.width - d2.width) / 2, p.y + (d1.height - d2.height) / 2);
        dialog.setVisible(true);
    }

    /**
     * 入参提示框
     * */
    private JDialog initArgsReloadedBeforeNoteDialog(JFrame mainFrame, ArrayList<String> addArgNames, ArrayList<String> removeArgNames, Arguments newArgs) {
        if (argsReloadedBeforeNoteDialog != null) {
            argsReloadedBeforeNoteDialog.setVisible(false);
        }
        argsReloadedBeforeNoteDialog = new EscapeDialog(mainFrame, "入参更新提示", false);
        argsReloadedBeforeNoteDialog.setMinimumSize(new Dimension(700, 300));
        // 获取屏幕尺寸
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // 计算最大尺寸（例如屏幕宽高的50%）
        int maxWidth = (int) (screenSize.width * 0.3);
        int maxHeight = (int) (screenSize.height * 0.5);
        argsReloadedBeforeNoteDialog.setPreferredSize(new Dimension(maxWidth, maxHeight));
        argsReloadedBeforeNoteDialog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                argsReloadedBeforeNoteDialog.setVisible(false);
            }
        });

        JTextArea jAddArgsMessage = new JTextArea("[添加入参]\n" + String.join("\n", addArgNames));
        jAddArgsMessage.setEditable(false);
        jAddArgsMessage.setForeground(new Color(12, 177, 0));
        jAddArgsMessage.setFont(FONT_SMALL);
        JTextArea jRemoveArgsMessage = new JTextArea("[移除入参]\n" + String.join("\n", removeArgNames));
        jRemoveArgsMessage.setEditable(false);
        jRemoveArgsMessage.setForeground(Color.red);
        jRemoveArgsMessage.setFont(FONT_SMALL);
        // 创建滚动窗格，并将内容面板放入其中
        JScrollPane jsAddArgsPanel = new JScrollPane(jAddArgsMessage);
        jsAddArgsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);   // 垂直滚动条按需显示
        JScrollPane jsRemoveArgsPanel = new JScrollPane(jRemoveArgsMessage);
        jsRemoveArgsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);   // 垂直滚动条按需显示

        JPanel infos = new JPanel();
        infos.setOpaque(false);
        infos.setLayout(new GridLayout(0, 1));
        infos.setBorder(new EmptyBorder(5, 5, 5, 5));
        infos.add(jsAddArgsPanel);
        infos.add(jsRemoveArgsPanel);

        JButton confirmButton = new JButton("确认更新");
        JButton cancelButton = new JButton("取消更新");
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                argsReloadedBeforeNoteDialog.setVisible(false);
            }
        });
        confirmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parameterIncludeControllerArgumentsPanel.configure(newArgs);
                argsReloadedBeforeNoteDialog.setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        Container panel = argsReloadedBeforeNoteDialog.getContentPane();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.white);
        panel.add(infos, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        argsReloadedBeforeNoteDialog.pack();
        return argsReloadedBeforeNoteDialog;
    }

    /**
     * 返回值参数提示框
     * */
    private JDialog initReturnValueReloadedBeforeNoteDialog(JFrame mainFrame, ArrayList<String> addArgNames, ArrayList<String> removeArgNames, Arguments newArgs) {
        if (returnValueArgsReloadedBeforeNoteDialog != null) {
            returnValueArgsReloadedBeforeNoteDialog.setVisible(false);
        }
        returnValueArgsReloadedBeforeNoteDialog = new EscapeDialog(mainFrame, "返回参数更新提示", false);
        returnValueArgsReloadedBeforeNoteDialog.setMinimumSize(new Dimension(700, 300));
        // 获取屏幕尺寸
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // 计算最大尺寸（例如屏幕宽高的50%）
        int maxWidth = (int) (screenSize.width * 0.3);
        int maxHeight = (int) (screenSize.height * 0.5);
        returnValueArgsReloadedBeforeNoteDialog.setPreferredSize(new Dimension(maxWidth, maxHeight));
        returnValueArgsReloadedBeforeNoteDialog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                returnValueArgsReloadedBeforeNoteDialog.setVisible(false);
            }
        });

        JTextArea jAddArgsMessage = new JTextArea("[添加返回参数]\n" + String.join("\n", addArgNames));
        jAddArgsMessage.setEditable(false);
        jAddArgsMessage.setForeground(new Color(12, 177, 0));
        jAddArgsMessage.setFont(FONT_SMALL);
        JTextArea jRemoveArgsMessage = new JTextArea("[移除返回参数]\n" + String.join("\n", removeArgNames));
        jRemoveArgsMessage.setEditable(false);
        jRemoveArgsMessage.setForeground(Color.red);
        jRemoveArgsMessage.setFont(FONT_SMALL);
        // 创建滚动窗格，并将内容面板放入其中
        JScrollPane jsAddArgsPanel = new JScrollPane(jAddArgsMessage);
        jsAddArgsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);   // 垂直滚动条按需显示
        JScrollPane jsRemoveArgsPanel = new JScrollPane(jRemoveArgsMessage);
        jsRemoveArgsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);   // 垂直滚动条按需显示


        JPanel infos = new JPanel();
        infos.setOpaque(false);
        infos.setLayout(new GridLayout(0, 1));
        infos.setBorder(new EmptyBorder(5, 5, 5, 5));
        infos.add(jsAddArgsPanel);
        infos.add(jsRemoveArgsPanel);

        JButton confirmButton = new JButton("确认更新");
        JButton cancelButton = new JButton("取消更新");
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                returnValueArgsReloadedBeforeNoteDialog.setVisible(false);
            }
        });
        confirmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parameterIncludeControllerReturnValuePanel.configure(newArgs);
                returnValueArgsReloadedBeforeNoteDialog.setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        Container panel = returnValueArgsReloadedBeforeNoteDialog.getContentPane();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.white);
        panel.add(infos, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        returnValueArgsReloadedBeforeNoteDialog.pack();
        return returnValueArgsReloadedBeforeNoteDialog;
    }

}
