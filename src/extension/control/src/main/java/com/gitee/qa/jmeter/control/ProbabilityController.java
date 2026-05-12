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

package com.gitee.qa.jmeter.control;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.NextIsNullException;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Controller that rans randomly one of it's children on each iteration
 */
public class ProbabilityController extends GenericController implements Serializable {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggerFactory.getLogger(ProbabilityController.class);

    private static final String weight = "ProbabilityController.weight"; // $NON-NLS-1$

    // 控制器内权重值之和
    private double totalWeight = 0d;
    // 随机数
    private double random = 0d;
    // 标识是否已经命中
    private ProbabilityController beHitProbabilityController = null;


    public ProbabilityController() {
    }

    @Override
    public void initialize() {
        super.initialize();
        totalWeight = getTotalWeight();
        random = getRandom(totalWeight);
//        System.out.println("initialize");
//        System.out.println("totalWeight: " + totalWeight);
//        System.out.println("random: " + random);
    }

    // 重写GenericController.next()
    @Override
    public Sampler next() {
        fireIterEvents();
        log.debug("Calling next on: {}", ProbabilityController.class);
        if (isDone()) {
            return null;
        }
        Sampler returnValue = null;
        try {
            TestElement currentElement = getCurrentElement();
            setCurrentElement(currentElement);
            if (currentElement == null) {
                returnValue = nextIsNull();
            } else {
                if (currentElement instanceof Sampler) {
                    returnValue = nextIsASampler((Sampler) currentElement);  // 如果当前currentElement是采样器
                } else { // must be a controller
                    if ((currentElement instanceof ProbabilityController) && (beHitProbabilityController != currentElement)){
                        float weight = Float.parseFloat(((ProbabilityController) currentElement).getProbailityValue());
                        if ((random - weight) > 0 || (beHitProbabilityController != null)){  // 表示当前的概率控制器不执行
                            random -= weight;
                            incrementCurrent();  // 跳过控制器
                            return this.next();  // 跳过控制器
                        } else {
                            beHitProbabilityController = (ProbabilityController) currentElement;
                        }
                    }
                    returnValue = nextIsAController((Controller) currentElement);  // 如果当前currentElement是控制器，则继续next迭代该控制器内组件
                }
            }
        } catch (NextIsNullException e) {
            // NOOP
        }
        return returnValue;
    }

    public void setProbailityValue(String value){
        setProperty(weight, value);
    }

    public String getProbailityValue() {
        return getPropertyAsString(weight);
    }

    @Override
    protected void reInitialize() {
        super.reInitialize();
        random = getRandom(totalWeight);
        beHitProbabilityController = null;
//        System.out.println("reInitialize");
//        System.out.println("totalWeight: " + totalWeight);
//        System.out.println("random: " + random);
    }

    // 获取当前控制器内所有概率控制器权重值之合
    private double getTotalWeight(){
        List<TestElement> subControllersAndSamplers = getSubControllers();
        for (TestElement te : subControllersAndSamplers){
            if (te instanceof ProbabilityController){
                String probailityValue = ((ProbabilityController) te).getProbailityValue();
                totalWeight += Float.parseFloat(probailityValue);
            }
        }
        return totalWeight;
    }

    // 获取一个在指定范围内的随机数
    private double getRandom(double max){
        Random r = new Random();
        return max * r.nextDouble();
    }

}
