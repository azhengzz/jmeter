package com.gitee.qa.jmeter.control.gui;

import com.gitee.qa.jmeter.control.CaseController;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public class CaseControlPanel extends LogicControllerGui {

    private static final long serialVersionUID = 240L;

    /**
     * Case名称
     * */
    private JTextField caseName;

    public CaseControlPanel() {
        init();
    }

    @Override
    public String getStaticLabel(){
        return "Case控制器"; // $NON-NLS-1$
    }

    private void init() {
        JPanel mainPanel = new VerticalPanel();
        mainPanel.add(createCaseNamePanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof CaseController){
            CaseController el =  (CaseController) element;
            caseName.setText(el.getCaseName());
        }
    }

    @Override
    public void modifyTestElement(TestElement el) {
        super.modifyTestElement(el);
    }

    @Override
    protected void configureTestElement(TestElement mc) {
        super.configureTestElement(mc);
        if (mc instanceof CaseController){
            CaseController el = (CaseController) mc;
            el.setCaseName(caseName.getText());
        }
    }

    @Override
    public TestElement createTestElement() {
        CaseController cc = new CaseController();
        modifyTestElement(cc);
        return cc;
    }

    @Override
    public void clearGui() {
        super.clearGui();
        caseName.setText("");
    }

    private JPanel createCaseNamePanel(){
        JPanel caseNamePanel = new HorizontalPanel();
        JLabel caseNameLabel = new JLabel("Case名称："); // $NON-NLS-1$
        caseNamePanel.add(caseNameLabel);
        caseName = new JTextField("");
        caseNameLabel.setLabelFor(caseName);
        caseNamePanel.add(caseName);
        return caseNamePanel;
    }
}
