package com.gitee.qa.jmeter.control.gui;

import com.gitee.qa.jmeter.control.util.ParameterIncludeControllerArgument;
import com.gitee.qa.jmeter.control.util.ParameterIncludeControllerReturnValueArgument;
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
import java.util.Iterator;

public class ParameterIncludeControllerReturnValuePanel extends ArgumentsPanel {

    private static final long serialVersionUID = 240L;

    private static final String NAME = "返回参数";
    private static final String VALUE = "变量名(返回值会保存到当前变量中)";
    private static final String DESCRIPTION = "返回参数说明";

    /** When pasting from the clipboard, split lines on linebreak or '&' */
    private static final String CLIPBOARD_LINE_DELIMITERS = "\n|&"; //$NON-NLS-1$

    /** When pasting from the clipboard, split parameters on tab or '=' */
    private static final String CLIPBOARD_ARG_DELIMITERS = "\t|="; //$NON-NLS-1$


    public ParameterIncludeControllerReturnValuePanel() {
        super();
    }

    public ParameterIncludeControllerReturnValuePanel(String label) {
        super(label);
    }

    /**
     * descriptionColumnEditable: 控制 DESCRIPTION 列是否可编辑
     * requiredColumnEditable: 控制 REQUIRED 列是否可编辑
     * */
    public ParameterIncludeControllerReturnValuePanel(String label, boolean nameColumnEditable, boolean descriptionColumnEditable) {
        super(label);
        // 禁用添加、粘贴按钮
        super.getAddButton().setEnabled(false);
        // getAddFromClipboardButton removed in JMeter 5.6
        JTable t = getTable();
        if (!nameColumnEditable) {
            TableColumn column = t.getColumnModel().getColumn(0);
            // 创建一个只读的单元格编辑器，允许复制但不能编辑
            JTextField textField = new JTextField();
            textField.setEditable(false); // 设置为不可编辑
            textField.setBackground(t.getBackground()); // 设置背景色与表格一致
            textField.setBorder(null); // 移除边框
            DefaultCellEditor editor = new DefaultCellEditor(textField);
//            editor.setClickCountToStart(100);
//            editor.getComponent().setEnabled(true);
            column.setCellEditor(editor);
        }
        if (!descriptionColumnEditable) {
            TableColumn column = t.getColumnModel().getColumn(2);
            // 创建一个只读的单元格编辑器，允许复制但不能编辑
            JTextField textField = new JTextField();
            textField.setEditable(false); // 设置为不可编辑
            textField.setBackground(t.getBackground()); // 设置背景色与表格一致
            textField.setBorder(null); // 移除边框
            DefaultCellEditor editor = new DefaultCellEditor(textField);
//            editor.setClickCountToStart(100);
//            editor.getComponent().setEnabled(false);
            column.setCellEditor(editor);
        }
    }


    @Override
    protected void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] { NAME, VALUE, DESCRIPTION },
            Argument.class,
            new Functor[]{
                    new Functor("getName"), //$NON-NLS-1$
                    new Functor("getValue"), //$NON-NLS-1$
                    new Functor("getDescription"), //$NON-NLS-1$
            },
            new Functor[]{
                    new Functor("setName"), //$NON-NLS-1$
                    new Functor("setValue"), //$NON-NLS-1$
                    new Functor("setDescription"), //$NON-NLS-1$
            },
            new Class[] {String.class, String.class, String.class });
    }

    public static boolean testFunctors() {
        ParameterIncludeControllerReturnValuePanel instance = new ParameterIncludeControllerReturnValuePanel("");
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null, instance.getClass());
    }

//    @Override
//    protected void sizeColumns(JTable table) {
//        GuiUtils.fixSize(table.getColumn(REQUIRED), table);
//    }

    @Override
    protected ParameterIncludeControllerReturnValueArgument makeNewArgument() {
        return new ParameterIncludeControllerReturnValueArgument();
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
            ParameterIncludeControllerReturnValueArgument.convertArgumentsToParameterIncludeControllerReturnValueArguments((Arguments) el);
            for (JMeterProperty jMeterProperty : ((Arguments) el).getArguments()) {
                ParameterIncludeControllerReturnValueArgument arg = (ParameterIncludeControllerReturnValueArgument) jMeterProperty.getObjectValue();
                tableModel.addRow(arg);
            }
        }
        checkButtonsStatus();
    }

    // 不支持手动添加
//    @Override
//    protected void addFromClipboard() {
//        addFromClipboard(CLIPBOARD_LINE_DELIMITERS, CLIPBOARD_ARG_DELIMITERS);
//    }

    // 不支持手动添加
//    @Override
//    protected Argument createArgumentFromClipboard(String[] clipboardCols) {
//        ParameterIncludeControllerArgument argument = makeNewArgument();
//        argument.setName(clipboardCols[0]);
//        if (clipboardCols.length > 1) {
//            argument.setValue(clipboardCols[1]);
//            if (clipboardCols.length > 2) {
//                argument.setDescription(clipboardCols[2]);
//                if (clipboardCols.length > 3) {
//                    Boolean isRequired = BooleanUtils.toBooleanObject(clipboardCols[3].trim());
//                    argument.setRequired(isRequired);
//                }
//            }
//        }
//        return argument;
//    }

    /**
     * Convert the argument panel contents to an {@link Arguments} collection.
     *p
     * @return a collection of {@link ParameterIncludeControllerArgument} entries
     */
    public Arguments getParameters() {
        Arguments args = getUnclonedParameters();
        return (Arguments) args.clone();
    }

    private Arguments getUnclonedParameters() {
        stopTableEditing();
        @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
        Iterator<ParameterIncludeControllerReturnValueArgument> modelData = (Iterator< ParameterIncludeControllerReturnValueArgument>) tableModel.iterator();
        Arguments args = new Arguments();
        while (modelData.hasNext()) {
            ParameterIncludeControllerReturnValueArgument arg = modelData.next();
            args.addArgument(arg);
        }
        return args;
    }

    public TableModel getTableModel(){
        return getTable().getModel();
    }

    public JTable getTable(){
        return super.getTable();
    }
}
