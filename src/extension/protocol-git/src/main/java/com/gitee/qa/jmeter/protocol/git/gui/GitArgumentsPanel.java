package com.gitee.qa.jmeter.protocol.git.gui;

import com.gitee.qa.jmeter.protocol.git.util.GitAction;
import com.gitee.qa.jmeter.protocol.git.util.GitArguments;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class GitArgumentsPanel extends ArgumentsPanel {

    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    public GitArgumentsPanel() {
        super();
    }

    public GitArgumentsPanel(String lable) {
        super(lable);
    }

    @Override
    protected void initializeTableModel() {
        if (tableModel == null) {
            tableModel = new ObjectTableModel(new String[] { COLUMN_RESOURCE_NAMES_0, COLUMN_RESOURCE_NAMES_1, COLUMN_RESOURCE_NAMES_2 },
                Argument.class,
                new Functor[] {
                        new Functor("getName"), // $NON-NLS-1$
                        new Functor("getValue"),  // $NON-NLS-1$
                        new Functor("getDescription") },  // $NON-NLS-1$
                new Functor[] {
                        new Functor("setName"), // $NON-NLS-1$
                        new Functor("setValue"), // $NON-NLS-1$
                        new Functor("setDescription") },  // $NON-NLS-1$
                new Class[] { String.class, String.class, String.class });
        }
    }

    @Override
    public TestElement createTestElement() {
        GitArguments args = new GitArguments();
        modifyTestElement(args);
        return args;
    }

    @Override
    public void configure(TestElement el) {
        if (el != null) {
            super.configure(el);
        }
   }

    /**
     * action: git action, 比如 clone push pull
     * actionChange: 是否是切换了git action
     *
     * */
    public void gitArgsPanelConfigure(String action, Boolean actionChange) {

        // 获取sampler参数
        GitArguments currArgs = new GitArguments();
        @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
        Iterator<Argument> modelData = (Iterator<Argument>) tableModel.iterator();
        while (modelData.hasNext()) {
            Argument arg = modelData.next();
            if(StringUtils.isEmpty(arg.getName()) && StringUtils.isEmpty(arg.getValue())) {
                continue;
            }
            arg.setMetaData("="); // $NON-NLS-1$
            currArgs.addArgument(arg);
        }
        Map<String, String[]> currArgsMap = currArgs.getGitArgumentsAsMap();

        // 获取默认参数
        GitArguments defaultArguments = null;
        switch (action) {
            case GitAction.CLONE:
                defaultArguments = currArgs.getGitCloneDefaultParameters();
                break;
            case GitAction.ADD:
                defaultArguments = currArgs.getGitAddDefaultParameters();
                break;
            case GitAction.COMMIT:
                defaultArguments = currArgs.getGitCommitDefaultParameters();
                break;
            case GitAction.PUSH:
                defaultArguments = currArgs.getGitPushDefaultParameters();
                break;
            case GitAction.PULL:
                defaultArguments = currArgs.getGitPullDefaultParameters();
                break;
            case GitAction.BRANCH:
                defaultArguments = currArgs.getGitBranchDefaultParameters();
                break;
        }

        GitArguments newArgs = new GitArguments();
        if (defaultArguments != null) {
            if (currArgsMap.keySet().isEmpty()){
                newArgs = defaultArguments;
            }else{
                for (JMeterProperty jMeterProperty : defaultArguments.getArguments()) {
                    Argument arg = (Argument) jMeterProperty.getObjectValue();
                    String name = arg.getName();
                    String value = arg.getValue();
                    String desc = arg.getDescription();

                    if (currArgsMap.containsKey(name)) {
                        String[] vals = currArgsMap.get(name);
                        String newVal = vals[0];
                        String newDesc = vals[1];
                        if (newVal != null && newVal.length() > 0) {
                            value = newVal;
                        }
                        if (newDesc != null && newDesc.length() > 0) {
                            desc = newDesc;
                        }
                        // 非action切换，比如只是更新参数
                        if (!actionChange){
                            newArgs.addArgument(name, value, null, desc);
                        }
                    }
                    // action切换
                    if (actionChange){
                        newArgs.addArgument(name, value, null, desc);
                    }
                }
            }
        }
        super.configure(newArgs);
    }
}
