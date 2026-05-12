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

import com.gitee.qa.jmeter.control.ProbabilityController;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.layout.VerticalLayout;

import javax.swing.*;

public class ProbabilityControllerControlGui extends AbstractControllerGui {
    private static final long serialVersionUID = 240L;

    // 概率数值
    private JTextField weight;

    public ProbabilityControllerControlGui() {
        init();
    }

    @Override
    public TestElement createTestElement() {
        ProbabilityController ic = new ProbabilityController();
        modifyTestElement(ic);
        return ic;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement pc) {
        configureTestElement(pc);
        if (pc instanceof ProbabilityController) {
            ((ProbabilityController) pc).setProbailityValue(weight.getText());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        weight.setText("0");
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof ProbabilityController) {
            weight.setText(((ProbabilityController) el).getProbailityValue());
        }
    }

    @Override
    public String getLabelResource() {
        return "probability_control_title"; // $NON-NLS-1$
    }

    @Override
    public String getStaticLabel(){
        return "概率控制器"; // $NON-NLS-1$
    }


    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());
        add(createProbabilityValuePanel());
    }

    private JPanel createProbabilityValuePanel() {
        JPanel probabilityValuePanel = new HorizontalPanel();
        JLabel counterNameLabel = new JLabel("权重"); // $NON-NLS-1$
        probabilityValuePanel.add(counterNameLabel);
        weight = new JTextField("");
        counterNameLabel.setLabelFor(weight);
        probabilityValuePanel.add(weight);
        return probabilityValuePanel;
    }
}
