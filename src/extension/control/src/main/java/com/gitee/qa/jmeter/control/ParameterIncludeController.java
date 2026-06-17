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

package com.gitee.qa.jmeter.control;

import com.gitee.qa.jmeter.control.util.ParameterIncludeControllerArgument;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.*;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class ParameterIncludeController extends GenericController implements ReplaceableController {
    private static final Logger log = LoggerFactory.getLogger(ParameterIncludeController.class);

    private static final long serialVersionUID = 241L;

    private static final String INCLUDE_PATH = "ParameterIncludeController.includepath"; //$NON-NLS-1$

    private static  final String PREFIX =
        JMeterUtils.getPropDefault(
                "ParameterIncludeController.prefix", //$NON-NLS-1$
                ""); //$NON-NLS-1$

    public static final String PARAMETER_INCLUDE_CONTROLLER_ARGUMENTS = "ParameterIncludeController.Arguments"; // $NON-NLS-1$
    public static final String PARAMETER_INCLUDE_CONTROLLER_RETURN_VALUE_ARGUMENTS = "ParameterIncludeController.ReturnValueArguments"; // $NON-NLS-1$

    private HashTree subtree = null;
    private TestElement sub = null;
    // 保存测试片段组件（第一个匹配到的测试片段）
    private TestFragmentController testFragmentController = null;

    // 保存控制器执行前线程的变量池
    private transient JMeterVariables outerVars = new JMeterVariables();

    /**
     * No-arg constructor
     *
     * @see Object#Object()
     */
    public ParameterIncludeController() {
        super();
    }

    @Override
    public Object clone() {
        // TODO - fix so that this is only called once per test, instead of at every clone
        // Perhaps save previous filename, and only load if it has changed?
        this.resolveReplacementSubTree(null);
        ParameterIncludeController clone = (ParameterIncludeController) super.clone();
        // outerVars 是 mutable 字段，super.clone() 只是浅拷贝引用；
        // 不重置会导致多线程下所有克隆体共享同一个变量池备份，引发数据竞争。
        clone.outerVars = new JMeterVariables();
        clone.setIncludePath(this.getIncludePath());
        if (this.subtree != null) {
            if (this.subtree.size() == 1) {
                for (Object o : this.subtree.keySet()) {
                    this.sub = (TestElement) o;
                }
            }
            clone.subtree = (HashTree)this.subtree.clone();
            clone.sub = this.sub==null ? null : (TestElement) this.sub.clone();
        }
        if (this.testFragmentController != null) {
            clone.testFragmentController = (TestFragmentController) this.testFragmentController.clone();
        }
        return clone;
    }

    @Override
    public Sampler next() {
        // 开始执行include控制器内的组件前
        if (this.isFirst()){
            // 检查必传参数是否定义，如果检查失败则抛出异常
            checkRequiredArgs();
            // 检查非空参数
            checkNotNullArgs();
            JMeterVariables vars = JMeterContextService.getContext().getVariables();
            // 在执行控制器前备份变量池
            // 注意：PropertyUtils.copyProperties 对 JMeterVariables 无效，
            // 因为其内部存储 Map 并非 JavaBean 属性（没有 setter），
            // 必须使用 putAll 完整复制变量与 putObject 存储的对象引用，
            // 否则后续 setVariables(outerVars) 会丢失 HTTPUDConfigElement 等关键对象。
            outerVars = new JMeterVariables();
            outerVars.putAll(vars);
            // 如果是测试片段(带参数)，则将参数以及默认值作为jmeter变量
            if (this.testFragmentController instanceof ParameterTestFragmentController) {
                ParameterTestFragmentController parameterTestFragmentController = (ParameterTestFragmentController) this.testFragmentController;
                Map<String, String> ptfControllerArgsMap = parameterTestFragmentController.getArgumentsAsMap();
                for (String key: ptfControllerArgsMap.keySet()) {
                    vars.put(key, ptfControllerArgsMap.get(key));
                }
            }
            // 将当前Include控制器(带参数)中设置的参数与值作为jmeter变量
            Map<String, String> argsMap = this.getArgumentsAsMap();
            for (String key: argsMap.keySet()) {
                vars.put(key, argsMap.get(key));
            }
        }
        // Copy From GenericController.next()
        fireIterEvents();
        log.debug("Calling next on: {}", GenericController.class);
        if (isDone()) {
            return null;
        }
        Sampler returnValue = null;
        try {
            TestElement currentElement = getCurrentElement();
            setCurrentElement(currentElement);
            if (currentElement == null) {
                returnValue = nextIsNull();
                // include控制器执行结束
                // 获取执行完include控制器后的变量池
                JMeterVariables vars = JMeterContextService.getContext().getVariables();
                // 如果是测试片段(带参数)，则将测试片段中定义的返回变量添加到当前上下文变量池中
                if (this.testFragmentController instanceof ParameterTestFragmentController) {
                    ParameterTestFragmentController parameterTestFragmentController = (ParameterTestFragmentController) this.testFragmentController;
                    Map<String, String> ptfControllerReturnValueArgsMap = parameterTestFragmentController.getReturnValueArgumentsAsMap();
                    Map<String, String> piControllerReturnValueArgsMap = this.getReturnValueArgumentsAsMap();
                    for (String key: piControllerReturnValueArgsMap.keySet()) {
                        String newVarName = piControllerReturnValueArgsMap.get(key);
                        String newVarValue = vars.get(key);
                        // 如果include控制器中定义的返回变量名称为空，则不会设置返回变量值
                        if (StringUtils.isEmpty(newVarName)) {
//                            outerVars.put(key, newVarValue);
                        } else {
                            outerVars.put(newVarName, newVarValue);
                        }
                    }
                }
                // 恢复原上下文中的变量池
                JMeterContextService.getContext().setVariables(outerVars);
                outerVars = new JMeterVariables();
            } else {
                if (currentElement instanceof Sampler) {
                    returnValue = nextIsASampler((Sampler) currentElement);
                } else { // must be a controller
                    returnValue = nextIsAController((Controller) currentElement);
                }
            }
        } catch (NextIsNullException e) {
            // NOOP
        }
        return returnValue;
    }

    /**
     * In the event an user wants to include an external JMX test plan
     * the GUI would call this.
     * @param jmxfile The path to the JMX test plan to include
     */
    public void setIncludePath(String jmxfile) {
        this.setProperty(INCLUDE_PATH,jmxfile);
    }

    /**
     * return the JMX file path.
     * @return the JMX file path
     */
    public String getIncludePath() {
        return this.getPropertyAsString(INCLUDE_PATH);
    }

    public Arguments getArguments() {
        return  (Arguments) getProperty(ParameterIncludeController.PARAMETER_INCLUDE_CONTROLLER_ARGUMENTS).getObjectValue();
    }

    public Map<String, String> getArgumentsAsMap() {
        return getArguments().getArgumentsAsMap();
    }


    public Arguments getReturnValueArguments() {
        return  (Arguments) getProperty(ParameterIncludeController.PARAMETER_INCLUDE_CONTROLLER_RETURN_VALUE_ARGUMENTS).getObjectValue();
    }

    public Map<String, String> getReturnValueArgumentsAsMap() {
        return getReturnValueArguments().getArgumentsAsMap();
    }

    public Map<String, Boolean> getArgumentsRequiredAsMap() {
        PropertyIterator iter = getArguments().iterator();
        Map<String, Boolean> argMap = new LinkedHashMap<>();
        while (iter.hasNext()) {
            ParameterIncludeControllerArgument arg = (ParameterIncludeControllerArgument) iter.next().getObjectValue();
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), arg.isRequired());
            }
        }
        return argMap;
    }

    public Map<String, Boolean> getArgumentsNotNullAsMap() {
        PropertyIterator iter = getArguments().iterator();
        Map<String, Boolean> argMap = new LinkedHashMap<>();
        while (iter.hasNext()) {
            ParameterIncludeControllerArgument arg = (ParameterIncludeControllerArgument) iter.next().getObjectValue();
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), arg.isNotNull());
            }
        }
        return argMap;
    }

    /**
     * The way ReplaceableController works is clone is called first,
     * followed by replace(HashTree) and finally getReplacement().
     */
    @Override
    public HashTree getReplacementSubTree() {
        return subtree;
    }

    public TestElement getReplacementElement() {
        return sub;
    }

    /**
     * 在Gui操作时获取实时最新的测试片段
     * */
    public TestFragmentController getTestFragmentController(final String includePath) {
        this.loadIncludedElements(includePath);  // 会在getProperBranch方法中获取测试片段
        return testFragmentController;
    }

    @Override
    public void resolveReplacementSubTree(JMeterTreeNode context) {
        this.subtree = this.loadIncludedElements();
    }

    /**
     * load the included elements using SaveService
     *
     * @return tree with loaded elements
     */
    protected HashTree loadIncludedElements() {
        final String includePath = getIncludePath();
        return this.loadIncludedElements(includePath);
    }

    protected HashTree loadIncludedElements(final String includePath) {
        // only try to load the JMX test plan if there is one
//        final String includePath = getIncludePath();
        HashTree tree = null;
        if (includePath != null && includePath.length() > 0) {
            String fileName=PREFIX+includePath;
            try {
                File file = new File(fileName.trim());
                final String absolutePath = file.getAbsolutePath();
                log.info("loadIncludedElements -- try to load included module: {}", absolutePath);
                if(!file.exists() && !file.isAbsolute()){
                    log.info("loadIncludedElements -failed for: {}", absolutePath);
                    file = new File(FileServer.getFileServer().getBaseDir(), includePath);
                    if (log.isInfoEnabled()) {
                        log.info("loadIncludedElements -Attempting to read it from: {}", file.getAbsolutePath());
                    }
                    if(!file.canRead() || !file.isFile()){
                        log.error("Include Controller '{}' can't load '{}' - see log for details", this.getName(),
                                fileName);
                        throw new IOException("loadIncludedElements -failed for: " + absolutePath +
                                " and " + file.getAbsolutePath());
                    }
                }

                tree = SaveService.loadTree(file);
                // filter the tree for a TestFragment.
                tree = getProperBranch(tree);
                removeDisabledItems(tree);
                return tree;
            } catch (NoClassDefFoundError ex) // Allow for missing optional jars
            {
                String msg = "Including file \""+ fileName
                            + "\" failed for Include Controller \""+ this.getName()
                            +"\", missing jar file";
                log.warn(msg, ex);
                JMeterUtils.reportErrorToUser(msg+" - see log for details");
            } catch (FileNotFoundException ex) {
                String msg = "File \""+ fileName 
                        + "\" not found for Include Controller \""+ this.getName()+"\"";
                JMeterUtils.reportErrorToUser(msg+" - see log for details");
                log.warn(msg, ex);
            } catch (Exception ex) {
                String msg = "Including file \"" + fileName 
                            + "\" failed for Include Controller \"" + this.getName()
                            +"\", unexpected error";
                JMeterUtils.reportErrorToUser(msg+" - see log for details");
                log.warn(msg, ex);
            }
        }
        return tree;
    }

    /**
     * Extract from tree (included test plan) all Test Elements located in a Test Fragment
     * @param tree HashTree included Test Plan
     * @return HashTree Subset within Test Fragment or Empty HashTree
     */
    private HashTree getProperBranch(HashTree tree) {
        for (Object o : new LinkedList<>(tree.list())) {
            TestElement item = (TestElement) o;

            //if we found a TestPlan, then we are on our way to the TestFragment
            if (item instanceof TestPlan)
            {
                return getProperBranch(tree.getTree(item));
            }

            if (item instanceof TestFragmentController)
            {
                this.testFragmentController = (TestFragmentController) item;
                return tree.getTree(item);
            }
        }
        log.warn("No Test Fragment was found in included Test Plan, returning empty HashTree");
        return new HashTree();
    }


    private void removeDisabledItems(HashTree tree) {
        for (Object o : new LinkedList<>(tree.list())) {
            TestElement item = (TestElement) o;
            if (!item.isEnabled()) {
                tree.remove(item);
            } else {
                removeDisabledItems(tree.getTree(item));// Recursive call
            }
        }
    }

    /**
     * 检查Include控制器(带参数)中是否定义了必传参数，未传则抛出异常
     * */
    public void checkRequiredArgs() throws IllegalArgumentException{
        // 如果是测试片段(带参数)
        if (this.testFragmentController instanceof ParameterTestFragmentController) {
            ParameterTestFragmentController parameterTestFragmentController = (ParameterTestFragmentController) this.testFragmentController;
            Map<String, Boolean> ptfControllerArgsRequiredAsMap = parameterTestFragmentController.getArgumentsRequiredAsMap();
            // 当前Include控制器(带参数)中设置的参数
            Map<String, Boolean> piControllerArgsRequiredAsMap = this.getArgumentsRequiredAsMap();
            // 将当前Include控制器(带参数)中设置的参数与测试片段(带参数)中定义的必传参数进行比对
            for (String key: ptfControllerArgsRequiredAsMap.keySet()){
                // 如果测试片段(带参数)中定义的必传参数在当前Include控制器(带参数)中没有设置
                if (ptfControllerArgsRequiredAsMap.get(key) && !piControllerArgsRequiredAsMap.containsKey(key)) {
                    String err = String.format("组件“%s”，缺失必传参数“%s”。\n！！！请检查修改并重新保存！！！", this.getName(), key);
                    throw new IllegalArgumentException(err);
                }
            }
        }
    }

    /**
     * 检查Include控制器(带参数)中非空字段是否传值
     * */
    public void checkNotNullArgs() throws IllegalArgumentException{
        // 如果是测试片段(带参数)
        if (this.testFragmentController instanceof ParameterTestFragmentController) {
            ParameterTestFragmentController parameterTestFragmentController = (ParameterTestFragmentController) this.testFragmentController;
            Map<String, Boolean> ptfControllerArgsNotNullAsMap = parameterTestFragmentController.getArgumentsNotNullAsMap();
            // 当前Include控制器(带参数)中设置的参数
            Map<String, String> piControllerArgsMap = this.getArgumentsAsMap();
            // 遍历Include中的参数默认值，如果在测试片段中声明非空，但当前传参为空，则给出错误日志提示
            for (String key: piControllerArgsMap.keySet()){
                if (ptfControllerArgsNotNullAsMap.containsKey(key) && ptfControllerArgsNotNullAsMap.get(key) && StringUtils.isEmpty(piControllerArgsMap.get(key))) {
                    String err = String.format("组件“%s”，参数“%s” 不能为空，但当前数值为空，请修改。", this.getName(), key, piControllerArgsMap.get(key));
                    log.error(err);
                }
            }
        }
    }


}
