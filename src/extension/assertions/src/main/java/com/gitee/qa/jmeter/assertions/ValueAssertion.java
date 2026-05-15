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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ValueAssertion extends AbstractTestElement implements Assertion, TestBean, Serializable {

	private static final long serialVersionUID = 240L;

	private Collection<ValueAssertionTableElement> valuesCheckTable;


	public ValueAssertion() {
		super();
	}

	@Override
	public AssertionResult getResult(SampleResult response) {
		AssertionResult result = new AssertionResult(getName());

		if (valuesCheckTable == null || valuesCheckTable.isEmpty()) {
			return doNothing(result);
		} else {
			compare(result);
		}
		return result;
	}

	public AssertionResult compare(AssertionResult result) {
		String actValue = "";
		String expValue = "";
		String desc = "";
		boolean failure = false;
		boolean error = false;
		String failureMsg = "\n";
		try {
			for (ValueAssertionTableElement row : valuesCheckTable) {
				actValue = row.getActual();
				expValue = row.getExpect();
				if (!row.getEnable()) continue;  // 不启用则不校验
				desc = row.getDesc();
				if (!Objects.equals(actValue, expValue)){
					failure = true;
					failureMsg += String.format("%s 校验失败，请检查：实际值:%s 期望值:%s。\n", desc, actValue, expValue);
				}
			}
		} catch (Exception e) {
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

	public Collection<ValueAssertionTableElement> getValuesCheckTable() {
		return valuesCheckTable;
	}

	public void setValuesCheckTable(Collection<ValueAssertionTableElement> valuesCheckTable) {
		this.valuesCheckTable = valuesCheckTable;
	}

	public String getValuesCheckTableAsString() {
		String resultString = "";
		ArrayList<TestElementProperty> arrayList = (ArrayList<TestElementProperty>) this.getProperty("valuesCheckTable").getObjectValue();
		for (TestElementProperty prop: arrayList) {
			ValueAssertionTableElement row = (ValueAssertionTableElement) prop.getObjectValue();
			resultString += String.format("[actual: %s, expect: %s, desc: %s]", row.getActual(), row.getExpect(), row.getDesc());
		}
		return resultString;
	}

	@Override
	public List<String> getSearchableTokens() {
		List<String> result = null;
		result = super.getSearchableTokens();
		result.add("valuesCheckTable");
		result.add(getValuesCheckTableAsString());
		return result;
	}
}
