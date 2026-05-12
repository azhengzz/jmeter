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

import com.gitee.qa.jmeter.control.DoWhileController;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.awt.*;

@GUIMenuSortOrder(4)
public class DoWhileControllerGui extends AbstractControllerGui {

    private static final long serialVersionUID = 240L;

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN,
            (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private static final String CONDITION_LABEL = "while_controller_label"; // $NON-NLS-1$

    /**
     * A field allowing the user to specify the condition (not yet used).
     */
    private JSyntaxTextArea theCondition;

    /** The name of the condition field component. */
    private static final String CONDITION = "Do_While_Condition"; // $NON-NLS-1$

    /**
     * Create a new LoopControlPanel as a standalone component.
     */
    public DoWhileControllerGui() {
        init();
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param element
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof DoWhileController) {
            theCondition.setText(((DoWhileController) element).getCondition());
        }

    }

    /**
     * Implements JMeterGUIComponent.createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        DoWhileController controller = new DoWhileController();
        modifyTestElement(controller);
        return controller;
    }

    /**
     * Implements JMeterGUIComponent.modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement controller) {
        configureTestElement(controller);
        if (controller instanceof DoWhileController) {
            if (theCondition.getText().length() > 0) {
                ((DoWhileController) controller).setCondition(theCondition.getText());
            } else {
                ((DoWhileController) controller).setCondition(""); // $NON-NLS-1$
            }
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        theCondition.setText(""); // $NON-NLS-1$
    }

    @Override
    public String getLabelResource() {
        return "do_while_controller_title"; // $NON-NLS-1$
    }

    public String getStaticLabel(){
        return "DoWhile控制器"; // $NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout for this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

//        JPanel mainPanel = new JPanel(new BorderLayout());
//        mainPanel.add(createConditionPanel(), BorderLayout.NORTH);
//        add(mainPanel, BorderLayout.CENTER);
        JPanel mainPanel = new VerticalPanel();
        mainPanel.add(createConditionPanel());
        mainPanel.add(createDescPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Create a GUI panel containing the condition. TODO make use of the field
     *
     * @return a GUI panel containing the condition components
     */
    private JPanel createConditionPanel() {
        JPanel conditionPanel = new JPanel(new BorderLayout(5, 0));

        // Condition LABEL
        JLabel conditionLabel = new JLabel(JMeterUtils.getResString(CONDITION_LABEL));
        conditionPanel.add(conditionLabel, BorderLayout.WEST);

        // Condition
        // This means exit if last sample failed
        theCondition = JSyntaxTextArea.getInstance(5, 50);  // $NON-NLS-1$
        theCondition.setName(CONDITION);
        conditionLabel.setLabelFor(theCondition);
        conditionPanel.add(JTextScrollPane.getInstance(theCondition), BorderLayout.CENTER);
        
        conditionPanel.add(Box.createHorizontalGlue(), BorderLayout.NORTH);

        return conditionPanel;
    }

    private JPanel createDescPanel() {
        JPanel panel =  new VerticalPanel();
        JLabel jl = new JLabel("注意: DoWhile循环即使不满足条件，也至少执行一次\n");
        jl.setForeground(Color.red);
        jl.setFont(FONT_SMALL);
        panel.add(jl);
        return panel;
    }
}
