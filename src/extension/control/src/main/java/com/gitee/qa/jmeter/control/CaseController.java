package com.gitee.qa.jmeter.control;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.testelement.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class CaseController extends GenericController implements Serializable {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggerFactory.getLogger(CaseController.class);

    private static final String CASE_NAME = "CaseController.case_name"; // $NON-NLS-1$

    public void setCaseName(String name) {
        setProperty(new StringProperty(CASE_NAME, name));
    }

    public String getCaseName() {
        return getPropertyAsString(CASE_NAME);
    }

}
