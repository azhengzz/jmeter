package com.gitee.qa.jmeter.control.gui;

import com.gitee.qa.jmeter.control.util.ParameterIncludeControllerArgument;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.util.Collection;
import java.util.Iterator;

public class ParameterTestFragmentArgumentsPanel extends ArgumentsPanel {

    private static final long serialVersionUID = 240L;

    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    private static final String NAME = "参数名";
    private static final String VALUE = "默认值";
    private static final String DESCRIPTION = "参数说明";
    private static final String NOT_NULL = "不能为空?";
    private static final String REQUIRED = "必传参数?";

    /** When pasting from the clipboard, split lines on linebreak or '&' */
    private static final String CLIPBOARD_LINE_DELIMITERS = "\n|&"; //$NON-NLS-1$

    /** When pasting from the clipboard, split parameters on tab or '=' */
    private static final String CLIPBOARD_ARG_DELIMITERS = "\t|="; //$NON-NLS-1$

    public ParameterTestFragmentArgumentsPanel() {
        super();
    }

    public ParameterTestFragmentArgumentsPanel(String label) {
        super(label);
    }

    /**
     * descriptionColumnEditable: 控制 DESCRIPTION 列是否可编辑
     * requiredColumnEditable: 控制 REQUIRED 列是否可编辑
     * */
    public ParameterTestFragmentArgumentsPanel(String label, boolean descriptionColumnEditable, boolean notNullColumnEditable, boolean requiredColumnEditable) {
        super(label);
        JTable t = getTable();
        if (!requiredColumnEditable) {
            TableColumn column = t.getColumnModel().getColumn(4);
            DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox());
            editor.setClickCountToStart(100);
            editor.getComponent().setEnabled(false);
            column.setCellEditor(editor);
        }
        if (!notNullColumnEditable) {
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
    protected void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] { NAME, VALUE, DESCRIPTION, NOT_NULL, REQUIRED},
            Argument.class,
            new Functor[] {
                new Functor("getName"), // $NON-NLS-1$
                new Functor("getValue"), //$NON-NLS-1$
                new Functor("getDescription"),  // $NON-NLS-1$
                new Functor("isNotNull"),  // $NON-NLS-1$
                new Functor("isRequired") },  // $NON-NLS-1$
            new Functor[] {
                new Functor("setName"), // $NON-NLS-1$
                new Functor("setValue"), // $NON-NLS-1$
                new Functor("setDescription"), // $NON-NLS-1$
                new Functor("setNotNull"), // $NON-NLS-1$
                new Functor("setRequired") },  // $NON-NLS-1$
            new Class[] { String.class, String.class, String.class, Boolean.class, Boolean.class });
    }

    public static boolean testFunctors() {
        ParameterTestFragmentArgumentsPanel instance = new ParameterTestFragmentArgumentsPanel("");
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null, instance.getClass());
    }

    @Override
    protected void sizeColumns(JTable table) {
        GuiUtils.fixSize(table.getColumn(REQUIRED), table);
        GuiUtils.fixSize(table.getColumn(NOT_NULL), table);
    }

    @Override
    protected ParameterIncludeControllerArgument makeNewArgument() {
        return new ParameterIncludeControllerArgument();
    }

    @Override
    public TestElement createTestElement() {
        Arguments args = getUnclonedParameters();
        super.configureTestElement(args);
        return (TestElement) args.clone();
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof Arguments) {
            tableModel.clearData();
            ParameterIncludeControllerArgument.convertArgumentsToParameterIncludeControllerArguments((Arguments) el);
            for (JMeterProperty jMeterProperty : ((Arguments) el).getArguments()) {
                ParameterIncludeControllerArgument arg = (ParameterIncludeControllerArgument) jMeterProperty.getObjectValue();
                tableModel.addRow(arg);
            }
        }
        checkButtonsStatus();
    }

    /**
     * Convert the argument panel contents to an {@link Arguments} collection.
     *
     * @return a collection of {@link ParameterIncludeControllerArgument} entries
     */
    public Arguments getParameters() {
        Arguments args = getUnclonedParameters();
        return (Arguments) args.clone();
    }

    private Arguments getUnclonedParameters() {
        stopTableEditing();
        @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
        Iterator<ParameterIncludeControllerArgument> modelData = (Iterator<ParameterIncludeControllerArgument>) tableModel.iterator();
        Arguments args = new Arguments();
        while (modelData.hasNext()) {
            ParameterIncludeControllerArgument arg = modelData.next();
            args.addArgument(arg);
        }
        return args;
    }

    @Override
    protected void addFromClipboard() {
        addFromClipboard(CLIPBOARD_LINE_DELIMITERS, CLIPBOARD_ARG_DELIMITERS);
    }

    @Override
    protected Argument createArgumentFromClipboard(String[] clipboardCols) {
        ParameterIncludeControllerArgument argument = makeNewArgument();
        argument.setName(clipboardCols[0]);
        if (clipboardCols.length > 1) {
            argument.setValue(clipboardCols[1]);
            if (clipboardCols.length > 2) {
                argument.setDescription(clipboardCols[2]);
                if (clipboardCols.length > 3) {
                    Boolean isNotNull = BooleanUtils.toBooleanObject(clipboardCols[3].trim());
                    argument.setNotNull(isNotNull);
                    if (clipboardCols.length > 4) {
                        Boolean isRequired = BooleanUtils.toBooleanObject(clipboardCols[4].trim());
                        argument.setRequired(isRequired);
                    }
                }
            }
        }
        return argument;
    }

    public TableModel getTableModel(){
        return getTable().getModel();
    }

    public JTable getTable(){
        return super.getTable();
    }
}
