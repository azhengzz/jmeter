package com.gitee.qa.jmeter.assertions;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TextAreaEditor;

import java.beans.PropertyDescriptor;

public class JsonAutoAssertionBeanInfo extends BeanInfoSupport {

    public JsonAutoAssertionBeanInfo() {
        super(JsonAutoAssertion.class);

        PropertyDescriptor p;

        p = property("extensible");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);

        createPropertyGroup("extensibleGroup", new String[] { "extensible" });

        p = property("strictOrdering");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);

        createPropertyGroup("strictOrderingGroup", new String[] { "strictOrdering" });

        p = property("jsonStr");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setPropertyEditorClass(TextAreaEditor.class);

        createPropertyGroup("jsonStrGroup", new String[] { "jsonStr" });
    }
}
