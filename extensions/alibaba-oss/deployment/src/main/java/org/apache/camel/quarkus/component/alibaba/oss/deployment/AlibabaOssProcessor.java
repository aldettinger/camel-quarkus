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
package org.apache.camel.quarkus.component.alibaba.oss.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;

class AlibabaOssProcessor {

    private static final String FEATURE = "camel-alibaba-oss";
    private static final DotName JACKSON_XML_ROOT_ELEMENT = DotName.createSimple(
            "com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement");

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    NativeImageResourceBuildItem nativeImageResource() {
        // Used by VersionInfoUtils to populate the User-Agent header on every OSS request
        return new NativeImageResourceBuildItem("versioninfo.properties");
    }

    @BuildStep
    IndexDependencyBuildItem indexDependency() {
        return new IndexDependencyBuildItem("com.aliyun", "alibabacloud-oss-v2");
    }

    @BuildStep
    void reflectiveClasses(CombinedIndexBuildItem combinedIndex, BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {
        final String[] modelClasses = combinedIndex
                .getIndex()
                .getAnnotations(JACKSON_XML_ROOT_ELEMENT)
                .stream()
                .filter(ann -> ann.target().kind() == AnnotationTarget.Kind.CLASS)
                .map(ann -> ann.target().asClass().name().toString())
                .filter(n -> n.startsWith("com.aliyun.sdk.service.oss2."))
                .sorted()
                .toArray(String[]::new);
        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(modelClasses).fields().build());
    }

}
