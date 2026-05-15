package com.gitee.qa.jmeter.assertions;
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


import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;

public class VariableAssertionBeanInfo extends BeanInfoSupport {

    public VariableAssertionBeanInfo() {
        super(VariableAssertion.class);
        createPropertyGroup("variablesCheck", new String[]{"variablesCheckTable"}); //$NON-NLS-1$ $NON-NLS-2$
        PropertyDescriptor p = null;
        p = property("variablesCheckTable"); //$NON-NLS-1$
        p.setPropertyEditorClass(TableEditor.class);
        p.setValue(TableEditor.CLASSNAME,VariableExpectElement.class.getName());
        p.setValue(TableEditor.HEADERS,new String[]{
                "变量名", //$NON-NLS-1$
                "属性名", //$NON-NLS-1$
                "期望值"}); //$NON-NLS-1$
        p.setValue(TableEditor.OBJECT_PROPERTIES, // These are the names of the get/set methods
                new String[]{VariableExpectElement.VARIABLE, VariableExpectElement.PROPERTY, VariableExpectElement.EXPECT});
        p.setValue(NOT_UNDEFINED,Boolean.TRUE);
        p.setValue(DEFAULT, new ArrayList<>());
        p.setValue(MULTILINE,Boolean.TRUE);

    }

}
