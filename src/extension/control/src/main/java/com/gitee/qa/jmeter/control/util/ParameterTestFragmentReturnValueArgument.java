package com.gitee.qa.jmeter.control.util;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ParameterTestFragmentReturnValueArgument extends Argument implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ParameterTestFragmentReturnValueArgument.class);

    private static final long serialVersionUID = 241L;

    /**
     * Constructor for the Argument object
     */
    public ParameterTestFragmentReturnValueArgument() {
        this("", "");
    }

    public ParameterTestFragmentReturnValueArgument(Argument arg) {
        this(arg.getName(), arg.getDescription());
    }

    /**
     * Construct a new HTTPArgument instance; alwaysEncoded is set to true.
     */
    public ParameterTestFragmentReturnValueArgument(String name, String desc) {
        setName(name);
        setDescription(desc);
        setMetaData("=");
    }

    /**
     * Converts all {@link Argument} entries to {@link ParameterTestFragmentReturnValueArgument} entries.
     *
     * @param args collection of {@link Argument} entries
     */
    public static void convertArgumentsToParameterTestFragmentReturnValueArguments(Arguments args) {
        List<Argument> newArguments = new LinkedList<>();
        for (JMeterProperty jMeterProperty : args.getArguments()) {
            Argument arg = (Argument) jMeterProperty.getObjectValue();
            if (!(arg instanceof ParameterTestFragmentReturnValueArgument)) {
                newArguments.add(new ParameterTestFragmentReturnValueArgument(arg));
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

}
