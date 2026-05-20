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

package com.gitee.qa.jmeter.protocol.httpud.util;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

//For unit tests, @see TestHTTPArgument

/*
 *
 * Represents an Argument for HTTP requests.
 */
public class HTTPUDArgument extends Argument implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(HTTPUDArgument.class);

    private static final long serialVersionUID = 241L;

    private static final String REQUIRED = "HTTPUDArgument.required";

    private static final EncoderCache cache = new EncoderCache(1000);

    public void setRequired(boolean r) {
        setProperty(new BooleanProperty(REQUIRED, r));
    }

    public boolean isRequired() {
        return getPropertyAsBoolean(REQUIRED);
    }

    public HTTPUDArgument(String name, String value, String desc) {
        this(name, value, desc, false);
    }

    /**
     * Construct a new HTTPArgument instance; alwaysEncoded is set to true.
     */
    public HTTPUDArgument(String name, String value, String desc, boolean required) {
        setRequired(required);
        setName(name);
        setValue(value);
        setDescription(desc);
        setMetaData("=");
    }

    public HTTPUDArgument(Argument arg) {
        this(arg.getName(), arg.getValue(), arg.getDescription());
    }

    /**
     * Constructor for the Argument object
     */
    public HTTPUDArgument() {
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

    /**
     * Converts all {@link Argument} entries in the collection to {@link HTTPUDArgument} entries.
     * 
     * @param args collection of {@link Argument} and/or {@link HTTPUDArgument} entries
     */
    public static void convertArgumentsToHTTPUD(Arguments args) {
        List<Argument> newArguments = new LinkedList<>();
        for (JMeterProperty jMeterProperty : args.getArguments()) {
            Argument arg = (Argument) jMeterProperty.getObjectValue();
            if (!(arg instanceof HTTPUDArgument)) {
                newArguments.add(new HTTPUDArgument(arg));
            } else {
                newArguments.add(arg);
            }
        }
        args.removeAllArguments();
        args.setArguments(newArguments);
    }
}
