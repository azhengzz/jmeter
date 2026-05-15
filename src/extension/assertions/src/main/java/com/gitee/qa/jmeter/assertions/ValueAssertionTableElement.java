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

package com.gitee.qa.jmeter.assertions;

import org.apache.jmeter.testelement.AbstractTestElement;

public class ValueAssertionTableElement extends AbstractTestElement {
    private static final long serialVersionUID = 1;

    // These constants are used both for the JMX file and for the setters/getters
    public static final String ACTUAL = "actual"; // $NON-NLS-1$

    public static final String EXPECT = "expect"; // $NON-NLS-1$

    public static final String DESC = "desc"; // $NON-NLS-1$

    public static final String ENABLE = "enable"; // $NON-NLS-1$

    public ValueAssertionTableElement() {
        super();
        // 设置enable默认值为true
        setEnable(true);
    }

    public String getActual()
    {
        return getProperty(ACTUAL).getStringValue();
    }

    public void setActual(String property)
    {
        setProperty(ACTUAL, property);
    }

    public String getExpect()
    {
        return getProperty(EXPECT).getStringValue();
    }

    public void setExpect(String expect)
    {
        setProperty(EXPECT, expect);
    }

    public String getDesc()
    {
        return getProperty(DESC).getStringValue();
    }

    public void setDesc(String desc)
    {
        setProperty(DESC, desc);
    }

    public Boolean getEnable()  // 必须是Boolean类型，如果是boolean类型则显示字符串true or false
    {
        return getProperty(ENABLE).getBooleanValue();
    }

    public void setEnable(Boolean enable)  // 必须是Boolean类型，如果是boolean类型则显示字符串true or false
    {
        setProperty(ENABLE, enable);
    }

}
