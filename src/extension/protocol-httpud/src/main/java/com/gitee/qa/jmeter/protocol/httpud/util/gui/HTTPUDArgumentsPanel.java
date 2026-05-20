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

package com.gitee.qa.jmeter.protocol.httpud.util.gui;

import com.gitee.qa.jmeter.protocol.httpud.util.HTTPUDArgument;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.util.Collection;
import java.util.Iterator;

/**
 * A GUI panel allowing the user to enter HTTP Parameters.
 * These have names and values, as well as check-boxes to determine whether or not to
 * include the "=" sign in the output and whether or not to encode the output.
 */
public class HTTPUDArgumentsPanel extends ArgumentsPanel {

    private static final long serialVersionUID = 240L;

    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    private static final String NAME = "参数名";
    private static final String VALUE = "参数值";
    private static final String DESCRIPTION = "参数说明";
    private static final String REQUIRED = "必传参数?";

    /** When pasting from the clipboard, split lines on linebreak or '&' */
    private static final String CLIPBOARD_LINE_DELIMITERS = "\n|&"; //$NON-NLS-1$

    /** When pasting from the clipboard, split parameters on tab or '=' */
    private static final String CLIPBOARD_ARG_DELIMITERS = "\t|="; //$NON-NLS-1$

    @Override
    protected void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] {
                NAME, VALUE, DESCRIPTION, REQUIRED },
                HTTPUDArgument.class,
                new Functor[] {
                new Functor("getName"), //$NON-NLS-1$
                new Functor("getValue"), //$NON-NLS-1$
                new Functor("getDescription"), //$NON-NLS-1$
                new Functor("isRequired") },  // $NON-NLS-1$
                new Functor[] {
                new Functor("setName"), //$NON-NLS-1$
                new Functor("setValue"), //$NON-NLS-1$
                new Functor("setDescription"), //$NON-NLS-1$
                new Functor("setRequired") },  // $NON-NLS-1$
                new Class[] {String.class, String.class, String.class, Boolean.class });
    }

    public static boolean testFunctors() {
        HTTPUDArgumentsPanel instance = new HTTPUDArgumentsPanel("");
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null, instance.getClass());
    }

    @Override
    protected void sizeColumns(JTable table) {
        GuiUtils.fixSize(table.getColumn(REQUIRED), table);
    }

    @Override
    protected HTTPUDArgument makeNewArgument() {
        HTTPUDArgument arg = new HTTPUDArgument("", "", "");
        arg.setRequired(false);
        return arg;
    }

    public HTTPUDArgumentsPanel() {
        super();
    }

    public HTTPUDArgumentsPanel(String label) {
        super(label);
        init();
    }

    /**
     * descriptionColumnEditable: 控制 DESCRIPTION 列是否可编辑
     * requiredColumnEditable: 控制 REQUIRED 列是否可编辑
     * */
    public HTTPUDArgumentsPanel(String label, boolean descriptionColumnEditable, boolean requiredColumnEditable) {
        super(label);
        init();
        JTable t = getTable();
        if (!requiredColumnEditable) {
            TableColumn column = t.getColumnModel().getColumn(3);
            DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox());
            editor.setClickCountToStart(100);
            editor.getComponent().setEnabled(false);
            column.setCellEditor(editor);
        }
        if (!descriptionColumnEditable) {
            TableColumn column = t.getColumnModel().getColumn(2);
            DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
            editor.setClickCountToStart(100);
            editor.getComponent().setEnabled(false);
            column.setCellEditor(editor);
        }
    }

    @Override
    public TestElement createTestElement() {
        Arguments args = getUnclonedParameters();
        super.configureTestElement(args);
        return (TestElement) args.clone();
    }

    /**
     * Convert the argument panel contents to an {@link Arguments} collection.
     *
     * @return a collection of {@link HTTPUDArgument} entries
     */
    public Arguments getParameters() {
        Arguments args = getUnclonedParameters();
        return (Arguments) args.clone();
    }

    private Arguments getUnclonedParameters() {
        stopTableEditing();
        @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
        Iterator<HTTPUDArgument> modelData = (Iterator<HTTPUDArgument>) tableModel.iterator();
        Arguments args = new Arguments();
        while (modelData.hasNext()) {
            HTTPUDArgument arg = modelData.next();
            args.addArgument(arg);
        }
        return args;
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof Arguments) {
            tableModel.clearData();
            HTTPUDArgument.convertArgumentsToHTTPUD((Arguments) el);
            for (JMeterProperty jMeterProperty : ((Arguments) el).getArguments()) {
                HTTPUDArgument arg = (HTTPUDArgument) jMeterProperty.getObjectValue();
                tableModel.addRow(arg);
            }
        }
        checkButtonsStatus();
    }

//    protected boolean isMetaDataNormal(HTTPUDArgument arg) {
//        return arg.getMetaData() == null || arg.getMetaData().equals("=")
//                || (arg.getValue() != null && arg.getValue().length() > 0);
//    }

    @Override
    protected void addFromClipboard() {
        addFromClipboard(CLIPBOARD_LINE_DELIMITERS, CLIPBOARD_ARG_DELIMITERS);
    }

    @Override
    protected Argument createArgumentFromClipboard(String[] clipboardCols) {
        HTTPUDArgument argument = makeNewArgument();
        argument.setName(clipboardCols[0]);
        if (clipboardCols.length > 1) {
            argument.setValue(clipboardCols[1]);
            if (clipboardCols.length > 2) {
                argument.setDescription(clipboardCols[2]);
                if (clipboardCols.length > 3) {
                    Boolean isRequired = BooleanUtils.toBooleanObject(clipboardCols[3].trim());
                    argument.setRequired(isRequired);
                }
            }
        }
        return argument;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        // register the right click menu
        JTable table = getTable();
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem variabilizeItem = new JMenuItem(JMeterUtils.getResString("transform_into_variable"));
        variabilizeItem.addActionListener(e -> transformNameIntoVariable());
        popupMenu.add(variabilizeItem);
        table.setComponentPopupMenu(popupMenu);
    }

    /**
     * replace the argument value of the selection with a variable
     * the variable name is derived from the parameter name
     */
    private void transformNameIntoVariable() {
        int[] rowsSelected = getTable().getSelectedRows();
        for (int selectedRow : rowsSelected) {
            String name = (String) tableModel.getValueAt(selectedRow, 0);
            if (StringUtils.isNotBlank(name)) {
                name = name.trim();
                name = name.replaceAll("\\$", "_");
                name = name.replaceAll("\\{", "_");
                name = name.replaceAll("\\}", "_");
                tableModel.setValueAt("${" + name + "}", selectedRow, 1);
            }
        }
    }

    public TableModel getTableModel(){
        return getTable().getModel();
    }

    public JTable getHTTPUDArgsTable(){
        return getTable();
    }

}
