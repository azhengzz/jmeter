package com.gitee.qa.jmeter.control;

import com.gitee.qa.jmeter.control.util.ParameterIncludeControllerArgument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParameterTestFragmentController extends TestFragmentController {

    public static final String PARAMETER_TEST_FRAGMENT_CONTROLLER_ARGUMENTS = "ParameterTestFragmentController.arguments"; // $NON-NLS-1$
    public static final String PARAMETER_TEST_FRAGMENT_CONTROLLER_RETURN_VALUE_ARGUMENTS = "ParameterTestFragmentController.ReturnValueArguments"; // $NON-NLS-1$

    public Arguments getArguments() {
        return (Arguments) getProperty(PARAMETER_TEST_FRAGMENT_CONTROLLER_ARGUMENTS).getObjectValue();
    }

    public void setArguments(Arguments args) {
        setProperty(new TestElementProperty(PARAMETER_TEST_FRAGMENT_CONTROLLER_ARGUMENTS, args));
    }

    public Arguments getReturnValueArguments() {
        return (Arguments) getProperty(PARAMETER_TEST_FRAGMENT_CONTROLLER_RETURN_VALUE_ARGUMENTS).getObjectValue();
    }

    public Map<String, String> getArgumentsAsMap() {
        Arguments arguments = (Arguments) getProperty(PARAMETER_TEST_FRAGMENT_CONTROLLER_ARGUMENTS).getObjectValue();
        return arguments.getArgumentsAsMap();
    }

    public Map<String, String> getReturnValueArgumentsAsMap() {
        Arguments arguments = (Arguments) getProperty(PARAMETER_TEST_FRAGMENT_CONTROLLER_RETURN_VALUE_ARGUMENTS).getObjectValue();
        return arguments.getArgumentsAsMap();
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

}
