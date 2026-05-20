package com.gitee.qa.jmeter.protocol.httpud.util.gui;

import com.gitee.qa.jmeter.protocol.httpud.sampler.HTTPUDSampler;
import com.gitee.qa.jmeter.protocol.httpud.util.HTTPUDArgument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

public class HTTPUDArgumentsGui extends JPanel {

    private static final long serialVersionUID = 240L;

    public static final String HTTP_USER_DEFINED_ARGUMENTS = "HTTPUDArgumentsGui.HTTPUDArguments";

    HTTPUDArgumentsPanel HTTPUDArgumentsPanel;

    public HTTPUDArgumentsGui(String label){
        init(label, true, true);
    }

    public HTTPUDArgumentsGui(String label, boolean descriptionColumnEditable, boolean requiredColumnEditable){
        init(label, descriptionColumnEditable, requiredColumnEditable);
    }

    private void init(String label, boolean descriptionColumnEditable, boolean requiredColumnEditable){
        this.setLayout(new BorderLayout());
        HTTPUDArgumentsPanel = new HTTPUDArgumentsPanel(label, descriptionColumnEditable, requiredColumnEditable);
        this.add(HTTPUDArgumentsPanel);
    }

    public void clear() {
        HTTPUDArgumentsPanel.clear();
    }


//    public TestElement createTestElement() {
//        ConfigTestElement element = new ConfigTestElement();
//
////        element.setName(this.getName());
////        element.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
////        element.setProperty(TestElement.TEST_CLASS, element.getClass().getName());
//        modifyTestElement(element);
//        return element;
//    }


    /**
     * Save the GUI values in the sampler.
     */
    public void modifyTestElement(TestElement element) {
        Arguments args;
        args = (Arguments) HTTPUDArgumentsPanel.createTestElement();
        HTTPUDArgument.convertArgumentsToHTTPUD(args);
        element.setProperty(new TestElementProperty(HTTPUDArgumentsGui.HTTP_USER_DEFINED_ARGUMENTS, args));
    }

    /**
     * Set the text, etc. in the UI.
     */
    public void configure(TestElement el) {
        setName(el.getName());
        Arguments arguments = (Arguments) el.getProperty(HTTPUDArgumentsGui.HTTP_USER_DEFINED_ARGUMENTS).getObjectValue();
        if (arguments == null) {  // 兼容之前的参数类型
            arguments = (Arguments) el.getProperty(HTTPUDSampler.ARGUMENTS).getObjectValue();
            HTTPUDArgument.convertArgumentsToHTTPUD(arguments);
        }
        HTTPUDArgumentsPanel.configure(arguments);
    }

    public HTTPUDArgumentsPanel getUserDefinedParametersPanel() {
        return HTTPUDArgumentsPanel;
    }

    public TableModel getTableModel() {
        return HTTPUDArgumentsPanel.getTableModel();
    }

    public JTable getTable() {
        return HTTPUDArgumentsPanel.getHTTPUDArgsTable();
    }

}
