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

package com.gitee.qa.jmeter.functions;

import com.google.auto.service.AutoService;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * FileToBase64 Function to read a complete file into a Base64 String.
 * <p>
 * Parameters:
 * <ul>
 *  <li>file name</li>
 *  <li>variable name (optional)</li>
 * </ul>
 *
 * Returns:
 * <ul>
 *  <li>the Base64 encoded content of a file</li>
 *  <li>or {@code **ERR**} if an error occurs</li>
 *  <li>value is also optionally saved in the variable for later re-use.</li>
 * </ul>
 */
@AutoService(Function.class)
public class FileToBase64 extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(FileToBase64.class);

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__FileToBase64";//$NON-NLS-1$

    static final String ERR_IND = "**ERR**";//$NON-NLS-1$

    static {
        desc.add("输入文件的全路径");//$NON-NLS-1$
        desc.add("存储结果的变量名（可选）");//$NON-NLS-1$
    }

    private static final int MIN_PARAM_COUNT = 1;

    private static final int MAX_PARAM_COUNT = 2;

    private static final int PARAM_NAME = 2;

    private Object[] values;

    public FileToBase64() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        String fileName = ((CompoundVariable) values[0]).execute();

        String myName = "";//$NON-NLS-1$
        if (values.length >= PARAM_NAME) {
            myName = ((CompoundVariable) values[PARAM_NAME - 1]).execute().trim();
        }

        String myValue = ERR_IND;

        try {
            File file = new File(fileName);
            if(file.exists() && file.canRead()) {
                byte[] bytes;
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    bytes = new byte[(int) file.length()];
                    fileInputStream.read(bytes);
                }
                myValue = Base64.getEncoder().encodeToString(bytes);
            } else {
                log.warn("Could not read open: {} ", fileName);
            }
        } catch (IOException e) {
            log.warn("Could not read file: {} {}", fileName, e.getMessage(), e);
        }

        if (myName.length() > 0) {
            JMeterVariables vars = getVariables();
            if (vars != null) {// Can be null if called from Config item testEnded() method
                vars.put(myName, myValue);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("{} name: {} value: {}", Thread.currentThread().getName(), myName, myValue); //$NON-NLS-1$
        }

        return myValue;
    }


    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAM_COUNT, MAX_PARAM_COUNT);
        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }
}
