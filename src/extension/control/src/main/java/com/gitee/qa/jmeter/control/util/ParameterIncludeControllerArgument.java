package com.gitee.qa.jmeter.control.util;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ParameterIncludeControllerArgument extends Argument implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ParameterIncludeControllerArgument.class);

    private static final long serialVersionUID = 241L;

    private static final String REQUIRED = "ParameterIncludeControllerArgument.required";
    private static final String NOT_NULL = "ParameterIncludeControllerArgument.notNull";

    /**
     * Constructor for the Argument object
     */
    public ParameterIncludeControllerArgument() {
        this("", "", "", false, false);
    }

    public ParameterIncludeControllerArgument(Argument arg) {
        this(arg.getName(), arg.getValue(), arg.getDescription());
    }

    public ParameterIncludeControllerArgument(String name, String value, String desc) {
        this(name, value, desc, false, false);
    }

    /**
     * Construct a new HTTPArgument instance; alwaysEncoded is set to true.
     */
    public ParameterIncludeControllerArgument(String name, String value, String desc, boolean notNull, boolean required) {
        setRequired(required);
        setNotNull(notNull);
        setName(name);
        setValue(value);
        setDescription(desc);
        setMetaData("=");
    }

    /**
     * Converts all {@link Argument} entries to {@link ParameterIncludeControllerArgument} entries.
     *
     * @param args collection of {@link Argument} entries
     */
    public static void convertArgumentsToParameterIncludeControllerArguments(Arguments args) {
        List<Argument> newArguments = new LinkedList<>();
        for (JMeterProperty jMeterProperty : args.getArguments()) {
            Argument arg = (Argument) jMeterProperty.getObjectValue();
            if (!(arg instanceof ParameterIncludeControllerArgument)) {
                newArguments.add(new ParameterIncludeControllerArgument(arg));
            } else {
                newArguments.add(arg);
            }
        }
        args.removeAllArguments();
        args.setArguments(newArguments);
    }

    /**
     * Sets the Name attribute of the Argument object.
     *
     * @param newName
     *            the new Name value
     */
    @Override
    public void setName(String newName) {
        if (newName == null || !newName.equals(getName())) {
            super.setName(newName);
        }
    }

    public void setNotNull(boolean r) {
        setProperty(new BooleanProperty(NOT_NULL, r));
    }

    public boolean isNotNull() {
        return getPropertyAsBoolean(NOT_NULL);
    }


    public void setRequired(boolean r) {
        setProperty(new BooleanProperty(REQUIRED, r));
    }

    public boolean isRequired() {
        return getPropertyAsBoolean(REQUIRED);
    }


}
