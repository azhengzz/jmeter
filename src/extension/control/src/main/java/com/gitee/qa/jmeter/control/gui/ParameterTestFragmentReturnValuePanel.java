package com.gitee.qa.jmeter.control.gui;

import com.gitee.qa.jmeter.control.util.ParameterTestFragmentReturnValueArgument;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.util.Collection;
import java.util.Iterator;

public class ParameterTestFragmentReturnValuePanel extends ArgumentsPanel {

    private static final long serialVersionUID = 240L;

    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    private static final String NAME = "返回变量名";
    private static final String DESCRIPTION = "返回变量说明";

    /** When pasting from the clipboard, split lines on linebreak or '&' */
    private static final String CLIPBOARD_LINE_DELIMITERS = "\n|&"; //$NON-NLS-1$

    /** When pasting from the clipboard, split parameters on tab or '=' */
    private static final String CLIPBOARD_ARG_DELIMITERS = "\t|="; //$NON-NLS-1$

    public ParameterTestFragmentReturnValuePanel() {
        super();
    }

    public ParameterTestFragmentReturnValuePanel(String label) {
        super(label);
    }

    /**
     * descriptionColumnEditable: 控制 DESCRIPTION 列是否可编辑
     * requiredColumnEditable: 控制 REQUIRED 列是否可编辑
     * */
    public ParameterTestFragmentReturnValuePanel(String label, boolean descriptionColumnEditable) {
        super(label);
        JTable t = getTable();
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
        tableModel = new ObjectTableModel(new String[] { NAME, DESCRIPTION },
            Argument.class,
            new Functor[]{
                    new Functor("getName"), // $NON-NLS-1$
                    new Functor("getDescription"),  // $NON-NLS-1$
            },
            new Functor[]{
                    new Functor("setName"), // $NON-NLS-1$
                    new Functor("setDescription"), // $NON-NLS-1$
            },
            new Class[] { String.class, String.class });
    }

    public static boolean testFunctors() {
        ParameterTestFragmentReturnValuePanel instance = new ParameterTestFragmentReturnValuePanel("");
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null, instance.getClass());
    }

//    @Override
//    protected void sizeColumns(JTable table) {
//        GuiUtils.fixSize(table.getColumn(REQUIRED), table);
//    }

    @Override
    protected ParameterTestFragmentReturnValueArgument makeNewArgument() {
        return new ParameterTestFragmentReturnValueArgument();
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
            ParameterTestFragmentReturnValueArgument.convertArgumentsToParameterTestFragmentReturnValueArguments((Arguments) el);
            for (JMeterProperty jMeterProperty : ((Arguments) el).getArguments()) {
                ParameterTestFragmentReturnValueArgument arg = (ParameterTestFragmentReturnValueArgument) jMeterProperty.getObjectValue();
                tableModel.addRow(arg);
            }
        }
        checkButtonsStatus();
    }

    /**
     * Convert the argument panel contents to an {@link Arguments} collection.
     *
     * @return a collection of {@link ParameterTestFragmentReturnValueArgument} entries
     */
    public Arguments getParameters() {
        Arguments args = getUnclonedParameters();
        return (Arguments) args.clone();
    }

    private Arguments getUnclonedParameters() {
        stopTableEditing();
        @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
        Iterator<ParameterTestFragmentReturnValueArgument> modelData = (Iterator<ParameterTestFragmentReturnValueArgument>) tableModel.iterator();
        Arguments args = new Arguments();
        while (modelData.hasNext()) {
            ParameterTestFragmentReturnValueArgument arg = modelData.next();
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
        ParameterTestFragmentReturnValueArgument argument = makeNewArgument();
        argument.setName(clipboardCols[0]);
        if (clipboardCols.length > 1) {
            argument.setDescription(clipboardCols[1]);
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
