/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.component.alibaba.mns.deployment;

import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.BytecodeTransformerBuildItem;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

// The MNS SDK uses javax.xml.bind (JAXB 2.x) but the project enforces jakarta.xml.bind (JAXB 4.x)
public class JakartaEnablement {

    private static final String JAVAX_PREFIX = "javax/xml/bind/";
    private static final String JAKARTA_PREFIX = "jakarta/xml/bind/";

    private static final String[] CLASSES_TO_TRANSFORM = {
            "com.aliyun.mns.common.parser.JAXBResultParser",
            "com.aliyun.mns.common.parser.JaxbDateSerializer",
            "com.aliyun.mns.model.ErrorMessage",
    };

    @BuildStep
    void transformToJakarta(BuildProducer<BytecodeTransformerBuildItem> transformers) {
        if (QuarkusClassLoader.isClassPresentAtRuntime("jakarta.xml.bind.JAXBContext")) {
            Remapper remapper = new Remapper(Opcodes.ASM9) {
                @Override
                public String map(String internalName) {
                    if (internalName.startsWith(JAVAX_PREFIX)) {
                        return JAKARTA_PREFIX + internalName.substring(JAVAX_PREFIX.length());
                    }
                    return internalName;
                }
            };
            for (String className : CLASSES_TO_TRANSFORM) {
                transformers.produce(new BytecodeTransformerBuildItem.Builder()
                        .setCacheable(true)
                        .setContinueOnFailure(false)
                        .setClassToTransform(className)
                        .setVisitorFunction((name, classVisitor) -> new ClassRemapper(classVisitor, remapper))
                        .build());
            }
        }
    }
}
