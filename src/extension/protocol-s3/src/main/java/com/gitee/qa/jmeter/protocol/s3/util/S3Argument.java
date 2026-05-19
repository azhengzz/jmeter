package com.gitee.qa.jmeter.protocol.s3.util;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class S3Argument extends Argument implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(S3Argument.class);

    private static final String REQUIRED = "S3Argument.required";
    private static final String NOT_NULL = "S3Argument.notNull";

    /**
     * Constructor for the Argument object
     */
    public S3Argument() {
        this("", "", "", false, false);
    }

    public S3Argument(Argument arg) {
        this(arg.getName(), arg.getValue(), arg.getDescription());
    }

    public S3Argument(String name, String value, String desc) {
        this(name, value, desc, false, false);
    }

    /**
     * Construct a new S3Argument instance; alwaysEncoded is set to true.
     */
    public S3Argument(String name, String value, String desc, boolean notNull, boolean required) {
        setRequired(required);
        setNotNull(notNull);
        setName(name);
        setValue(value);
        setDescription(desc);
        setMetaData("=");
    }

    /**
     * Converts all {@link Argument} entries in the collection to {@link S3Argument} entries.
     *
     * @param args collection of {@link Argument} and/or {@link S3Argument} entries
     */
    public static void convertArgumentsToS3Arguments(Arguments args) {
        List<Argument> newArguments = new LinkedList<>();
        for (JMeterProperty jMeterProperty : args.getArguments()) {
            Argument arg = (Argument) jMeterProperty.getObjectValue();
            if (!(arg instanceof S3Argument)) {
                newArguments.add(new S3Argument(arg));
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
