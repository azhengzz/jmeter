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


import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class VariableAssertion extends AbstractTestElement implements Assertion, TestBean, Serializable{

	private static final Logger log = LoggerFactory.getLogger(VariableAssertion.class); // used by check()

    private static final long serialVersionUID = 240L;

    private Collection<VariableExpectElement> variablesCheckTable;


	public VariableAssertion() {
        super();
    }

    @Override
    public AssertionResult getResult(SampleResult response) {
        AssertionResult result = new AssertionResult(getName());
        
        if (variablesCheckTable == null || variablesCheckTable.isEmpty()) {
            return doNothing(result);
        }else {
        	compare(result);
        }
        return result;
    }

    public AssertionResult compare(AssertionResult result) {
    	String varName = "";
    	String varValue = "";
		String propertyName = "";
		String propertyValue = "";
    	String expValue = "";
    	boolean failure = false;
    	boolean error = false;
    	String failureMsg = "\n";
		try {
	    	JMeterContext context = getThreadContext();
	        JMeterVariables vars = context.getVariables();  // 上下文中变量
			Properties properties = context.getProperties();  // 上下文中的属性
			for (VariableExpectElement row: variablesCheckTable) {
				varName = row.getVariable().trim();
				varValue = vars.get(varName);
				propertyName = row.getProperty().trim();
				propertyValue = properties.getProperty(propertyName);
				expValue = row.getExpect();
				if (! "".equals(varName) ){
					if (varValue == null) {
						failure = true;
						failureMsg += String.format("变量校验失败，变量名:%s在当前线程组中未找到，请检查，期望值:%s。\n", varName, expValue);
					} else if (! expValue.equals(varValue)) {
						failure = true;
						failureMsg += String.format("变量校验失败，变量名:%s，实际值:%s，期望值:%s。\n", varName, varValue, expValue);
					}
				}
				if (! "".equals(propertyName) ){
					if (propertyValue == null) {
						failure = true;
						failureMsg += String.format("属性校验失败，属性名:%s在当前实例中未找到，请检查，期望值:%s。\n", propertyName, expValue);
					} else if (! expValue.equals(propertyValue)) {
						failure = true;
						failureMsg += String.format("属性校验失败，属性名:%s，实际值:%s，期望值:%s。\n", propertyName, propertyValue, expValue);
					}
				}
		 	}
		} catch (Exception e){
			error = true;
			failureMsg = e.getMessage();
		}
     	result.setFailure(failure);
     	result.setError(error);
     	result.setFailureMessage(failureMsg);
    	return result;
    }
    
    public AssertionResult doNothing(AssertionResult result) {
    	result.setError(false);
    	result.setFailure(false);
    	return result;
    }
    
    public Collection<VariableExpectElement> getVariablesCheckTable() {
    	return variablesCheckTable;
    }

    public void setVariablesCheckTable(Collection<VariableExpectElement> variablesCheckTable) {
    	this.variablesCheckTable = variablesCheckTable;
    }

	public String getVariablesCheckTableAsString() {
		String resultString = "";
		ArrayList<TestElementProperty> arrayList = (ArrayList<TestElementProperty>) this.getProperty("variablesCheckTable").getObjectValue();
		for (TestElementProperty prop: arrayList) {
			VariableExpectElement row = (VariableExpectElement) prop.getObjectValue();
			resultString += String.format("[variable: %s, property: %s , expect: %s]", row.getVariable(), row.getProperty(), row.getExpect());
		}
		return resultString;
	}

	@Override
	public List<String> getSearchableTokens() {
		List<String> result = null;
		result = super.getSearchableTokens();
		result.add("variablesCheckTable");
		result.add(getVariablesCheckTableAsString());
		return result;
	}

}
