/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.groovy

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class GroovyBasePluginIntegrationTest extends AbstractIntegrationSpec {
    def "defaults groovyClasspath to 'groovy' configuration if the latter is non-empty"() {
        executer.withDeprecationChecksDisabled()
        file("build.gradle") << """
apply plugin: "groovy-base"

sourceSets {
    custom
}

repositories {
    mavenCentral()
}

dependencies {
    groovy "org.codehaus.groovy:groovy-all:2.1.2"
}

task groovydoc(type: Groovydoc)

task verify << {
    assert compileCustomGroovy.groovyClasspath.is(configurations.groovy)
    assert groovydoc.groovyClasspath.is(configurations.groovy)
}
"""

        expect:
        succeeds("verify")
    }

    def "defaults Groovy class path to inferred Groovy dependency if Groovy configuration is empty"() {
        file("build.gradle") << """
apply plugin: "groovy-base"

sourceSets {
    custom
}

repositories {
    mavenCentral()
}

dependencies {
    customCompile "$dependency"
}

task groovydoc(type: Groovydoc) {
    classpath = sourceSets.custom.runtimeClasspath
}

task verify << {
    assert compileCustomGroovy.groovyClasspath.files.any { it.name == "$jarFile" }
    assert groovydoc.groovyClasspath.files.any { it.name == "$jarFile" }
}
"""

        expect:
        succeeds("verify")

        where:
        dependency                                  | jarFile
        "org.codehaus.groovy:groovy-all:2.1.2"      | "groovy-all-2.1.2.jar"
        "org.codehaus.groovy:groovy:2.1.2"          | "groovy-2.1.2.jar"
        "org.codehaus.groovy:groovy-all:2.1.2:indy" | "groovy-all-2.1.2-indy.jar"
    }

    def "only resolves source class path feeding into inferred Groovy class path if/when the latter is actually used (but not during autowiring)"() {
        file("build.gradle") << """
apply plugin: "groovy-base"

sourceSets {
    custom
}

repositories {
    mavenCentral()
}

dependencies {
    customCompile "org.codehaus.groovy:groovy-all:2.1.2"
}

task groovydoc(type: Groovydoc) {
    classpath = sourceSets.custom.runtimeClasspath
}

task verify << {
    assert configurations.customCompile.state.toString() == "UNRESOLVED"
    assert configurations.customRuntime.state.toString() == "UNRESOLVED"
}
        """

        expect:
        succeeds("verify")
    }
}
