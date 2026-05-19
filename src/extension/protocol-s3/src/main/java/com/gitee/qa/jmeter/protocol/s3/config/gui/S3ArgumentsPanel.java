package com.gitee.qa.jmeter.protocol.s3.config.gui;

import com.gitee.qa.jmeter.protocol.s3.util.S3Arguments;
import com.gitee.qa.jmeter.protocol.s3.util.S3Action;
import com.gitee.qa.jmeter.protocol.s3.util.S3Argument;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map;
import java.util.Objects;

public class S3ArgumentsPanel extends ArgumentsPanel {

    private static final long serialVersionUID = 240L;

    private static final String NAME = "参数名";
    private static final String VALUE = "参数值";
    private static final String DESCRIPTION = "参数说明";
    private static final String NOT_NULL = "不能为空?";
    private static final String REQUIRED = "必传参数?";

    /** When pasting from the clipboard, split lines on linebreak or '&' */
    private static final String CLIPBOARD_LINE_DELIMITERS = "\n|&"; //$NON-NLS-1$

    /** When pasting from the clipboard, split parameters on tab or '=' */
    private static final String CLIPBOARD_ARG_DELIMITERS = "\t|="; //$NON-NLS-1$


    public S3ArgumentsPanel() {
        super();
    }

    public S3ArgumentsPanel(String label) {
        super(true, label);
        JTable t = getTable();
        // 参数名 不可编辑
        TableColumn nameColumn = t.getColumnModel().getColumn(0);
        DefaultCellEditor nameEditor = new DefaultCellEditor(new JTextField());
        nameEditor.setClickCountToStart(100);
        nameEditor.getComponent().setEnabled(false);
        nameColumn.setCellEditor(nameEditor);
        // 描述列 不可编辑
        TableColumn descColumn = t.getColumnModel().getColumn(2);
        DefaultCellEditor descEditor = new DefaultCellEditor(new JTextField());
        descEditor.setClickCountToStart(100);
        descEditor.getComponent().setEnabled(false);
        descColumn.setCellEditor(descEditor);
        // 不能为空 不可编辑
        TableColumn notNullColumn = t.getColumnModel().getColumn(3);
        DefaultCellEditor notNullEditor = new DefaultCellEditor(new JCheckBox());
        notNullEditor.setClickCountToStart(100);
        notNullEditor.getComponent().setEnabled(false);
        notNullColumn.setCellEditor(notNullEditor);
        // 必传参数 不可编辑
        TableColumn requiredColumn = t.getColumnModel().getColumn(4);
        DefaultCellEditor requiredEditor = new DefaultCellEditor(new JCheckBox());
        requiredEditor.setClickCountToStart(100);
        requiredEditor.getComponent().setEnabled(false);
        requiredColumn.setCellEditor(requiredEditor);
    }


    @Override
    protected void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] { NAME, VALUE, DESCRIPTION, NOT_NULL, REQUIRED },
                S3Argument.class,
                new Functor[] {
                        new Functor("getName"), //$NON-NLS-1$
                        new Functor("getValue"), //$NON-NLS-1$
                        new Functor("getDescription"), //$NON-NLS-1$
                        new Functor("isNotNull"),  // $NON-NLS-1$
                        new Functor("isRequired") },  // $NON-NLS-1$
                new Functor[] {
                        new Functor("setName"), //$NON-NLS-1$
                        new Functor("setValue"), //$NON-NLS-1$
                        new Functor("setDescription"), //$NON-NLS-1$
                        new Functor("setNotNull"), // $NON-NLS-1$
                        new Functor("setRequired") },  // $NON-NLS-1$
                new Class[] {String.class, String.class, String.class, Boolean.class, Boolean.class });
    }

    public static boolean testFunctors() {
        S3ArgumentsPanel instance = new S3ArgumentsPanel("");
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null, instance.getClass());
    }

    @Override
    protected void sizeColumns(JTable table) {
        GuiUtils.fixSize(table.getColumn(REQUIRED), table);
        GuiUtils.fixSize(table.getColumn(NOT_NULL), table);
    }

    @Override
    protected S3Argument makeNewArgument() {
        return new S3Argument();
    }

    @Override
    public TestElement createTestElement() {
        S3Arguments args = getUnclonedParameters();
        super.configureTestElement(args);
        return (TestElement) args.clone();
    }

    @Override
    public void configure(TestElement el) {
//        super.configure(el);
        if (el instanceof Arguments) {
            tableModel.clearData();
            S3Argument.convertArgumentsToS3Arguments((Arguments) el);
            for (JMeterProperty jMeterProperty : ((Arguments) el).getArguments()) {
                S3Argument arg = (S3Argument) jMeterProperty.getObjectValue();
                tableModel.addRow(arg);
            }
        }
        checkButtonsStatus();
    }

    @Override
    protected void addFromClipboard() {
        addFromClipboard(CLIPBOARD_LINE_DELIMITERS, CLIPBOARD_ARG_DELIMITERS);
    }

    @Override
    protected S3Argument createArgumentFromClipboard(String[] clipboardCols) {
        S3Argument argument = makeNewArgument();
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

    /**
     * Convert the argument panel contents to an {@link Arguments} collection.
     *p
     * @return a collection of {@link S3Argument} entries
     */
    public S3Arguments getParameters() {
        S3Arguments args = getUnclonedParameters();
        return (S3Arguments) args.clone();
    }

    private S3Arguments getUnclonedParameters() {
        stopTableEditing();
        @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
        Iterator<S3Argument> modelData = (Iterator<S3Argument>) tableModel.iterator();
        S3Arguments args = new S3Arguments();
        while (modelData.hasNext()) {
            S3Argument arg = modelData.next();
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

    /**
     *
     *
     * */
    public void s3ArgsPanelConfigure(String action, Boolean actionChange) {

        // 获取sampler参数
        S3Arguments currArgs = new S3Arguments();
        @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
        Iterator<S3Argument> modelData = (Iterator<S3Argument>) tableModel.iterator();
        while (modelData.hasNext()) {
            S3Argument arg = modelData.next();
            if(StringUtils.isEmpty(arg.getName()) && StringUtils.isEmpty(arg.getValue())) {
                continue;
            }
            arg.setMetaData("="); // $NON-NLS-1$
            currArgs.addArgument(arg);
        }
        Map<String, String[]> currArgsMap = currArgs.getS3ArgumentsAsMap();

        // 获取默认参数
        S3Arguments defaultArguments = null;

        if (Objects.equals(action, S3Action.LIST_ALL_BUCKETS.getCode())) {
            defaultArguments = currArgs.getListAllBucketsParameters();
        } else if (Objects.equals(action, S3Action.CREATE_BUCKET.getCode())) {
            defaultArguments = currArgs.getCreateBucketParameters();
        } else if (Objects.equals(action, S3Action.DELETE_BUCKET.getCode())) {
            defaultArguments = currArgs.getDeleteBucketParameters();
        } else if (Objects.equals(action, S3Action.BUCKET_EXISTS.getCode())) {
            defaultArguments = currArgs.getBucketExistsParameters();
        } else if (Objects.equals(action, S3Action.LIST_OBJECTS.getCode())) {
            defaultArguments = currArgs.getListObjectsParameters();
        } else if (Objects.equals(action, S3Action.GET_FILE_METADATA.getCode())) {
            defaultArguments = currArgs.getFileMetadataParameters();
        } else if (Objects.equals(action, S3Action.UPLOAD_FILE.getCode())) {
            defaultArguments = currArgs.getUploadFileParameters();
        } else if (Objects.equals(action, S3Action.DOWNLOAD_FILE.getCode())) {
            defaultArguments = currArgs.getDownloadFileParameters();
        } else if (Objects.equals(action, S3Action.DELETE_FILE.getCode())) {
            defaultArguments = currArgs.getDeleteFileParameters();
        } else if (Objects.equals(action, S3Action.FILE_EXISTS.getCode())) {
            defaultArguments = currArgs.getFileExistsParameters();
        }

        S3Arguments newArgs = new S3Arguments();
        if (defaultArguments != null) {
            if (currArgsMap.keySet().isEmpty()){
                newArgs = defaultArguments;
            } else {
                for (JMeterProperty jMeterProperty : defaultArguments.getArguments()) {
                    S3Argument arg = (S3Argument) jMeterProperty.getObjectValue();
                    String name = arg.getName();
                    String value = arg.getValue();
                    String desc = arg.getDescription();
                    boolean isRequired = arg.isRequired();
                    boolean isNotNull = arg.isNotNull();

                    // 保留已存在参数值
                    if (currArgsMap.containsKey(name)) {
                        String[] vals = currArgsMap.get(name);
                        String currVal = vals[0];
                        String currDesc = vals[1];
                        boolean currIsRequired =  Boolean.parseBoolean(vals[2]);
                        boolean currIsNotNull = Boolean.parseBoolean(vals[3]);
                        if (currVal != null && currVal.length() > 0) {
                            value = currVal;
                        }
                        if (currDesc != null && currDesc.length() > 0) {
                            desc = currDesc;
                        }
                        // 非action切换，比如只是更新参数
                        if (!actionChange){
                            newArgs.addArgument(new S3Argument(name, value, desc, isRequired, isNotNull));
                        }
                    }
                    // action切换
                    if (actionChange){
                        // 直接使用默认参数内容更新
                        newArgs.addArgument(new S3Argument(name, value, desc, isRequired, isNotNull));
                    }
                }
            }
        }
        configure(newArgs);
    }
}
