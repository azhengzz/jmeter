package com.gitee.qa.jmeter.control.gui;

import com.gitee.qa.jmeter.control.ParameterTestFragmentController;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.gui.TestFragmentControllerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;

import javax.swing.*;
import java.awt.*;

public class ParameterTestFragmentControllerGui extends TestFragmentControllerGui {

    private static final long serialVersionUID = 240L;

    ParameterTestFragmentArgumentsPanel parameterTestFragmentArgumentsPanel;

    ParameterTestFragmentReturnValuePanel parameterTestFragmentReturnValuePanel;

    public ParameterTestFragmentControllerGui() {
        init();
    }


    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public String getStaticLabel() {
        return "测试片段(带参数)";
    }

    @Override
    public TestElement createTestElement() {
        ParameterTestFragmentController controller = new ParameterTestFragmentController();
        setEnabled(false);  // 默认禁止状态
        modifyTestElement(controller);
        return controller;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        ParameterTestFragmentController controller = (ParameterTestFragmentController) element;
        Arguments args = (Arguments) parameterTestFragmentArgumentsPanel.createTestElement();
        controller.setProperty(new TestElementProperty(ParameterTestFragmentController.PARAMETER_TEST_FRAGMENT_CONTROLLER_ARGUMENTS, args));
        Arguments returnValueArgs = (Arguments) parameterTestFragmentReturnValuePanel.createTestElement();
        controller.setProperty(new TestElementProperty(ParameterTestFragmentController.PARAMETER_TEST_FRAGMENT_CONTROLLER_RETURN_VALUE_ARGUMENTS, returnValueArgs));
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        Arguments arguments = (Arguments) element.getProperty(ParameterTestFragmentController.PARAMETER_TEST_FRAGMENT_CONTROLLER_ARGUMENTS).getObjectValue();
        if (arguments == null){
            parameterTestFragmentArgumentsPanel.configure(new Arguments());;
        } else {
            parameterTestFragmentArgumentsPanel.configure(arguments);
        }
        Arguments returnValueArguments = (Arguments) element.getProperty(ParameterTestFragmentController.PARAMETER_TEST_FRAGMENT_CONTROLLER_RETURN_VALUE_ARGUMENTS).getObjectValue();
        if (returnValueArguments == null){
            parameterTestFragmentReturnValuePanel.configure(new Arguments());;
        } else {
            parameterTestFragmentReturnValuePanel.configure(returnValueArguments);
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        parameterTestFragmentArgumentsPanel.clearGui();
        parameterTestFragmentReturnValuePanel.clearGui();
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        // 创建分割面板，设置初始比例
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(this.createArgumentsPanel());
        splitPane.setBottomComponent(this.createReturnValueArgumentsPanel());
        splitPane.setDividerLocation(0.7); // 70% 给上部面板
        splitPane.setResizeWeight(0.7); // 调整时保持比例
        add(splitPane, BorderLayout.CENTER);

//        add(this.createArgumentsPanel(), BorderLayout.CENTER);
//        add(this.createReturnValueArgumentsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createArgumentsPanel() {
        JPanel argumentsPanel = new JPanel();
        argumentsPanel.setLayout(new BorderLayout());
        argumentsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "定义变量参数")); // $NON-NLS-1$
        parameterTestFragmentArgumentsPanel = new ParameterTestFragmentArgumentsPanel("参数定义", true, true, true);
        argumentsPanel.add(parameterTestFragmentArgumentsPanel, BorderLayout.CENTER);
        return argumentsPanel;
    }

    private JPanel createReturnValueArgumentsPanel() {
        JPanel argumentsPanel = new JPanel();
        argumentsPanel.setLayout(new BorderLayout());
        argumentsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "定义返回变量")); // $NON-NLS-1$
        parameterTestFragmentReturnValuePanel = new ParameterTestFragmentReturnValuePanel("返回变量");
        argumentsPanel.add(parameterTestFragmentReturnValuePanel, BorderLayout.CENTER);
        return argumentsPanel;
    }
}
