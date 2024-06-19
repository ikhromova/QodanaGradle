/*
 * Copyright 2024 the original author or authors.
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

plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    // enable all default rules
    buildUponDefaultConfig = true

    // customize some of the rules, until we can fix the offending cases
    config.setFrom(project.isolated.rootProject.projectDirectory.file("gradle/detekt.yml"))

    // also check .gradle.kts build files
    val buildFiles = project
        .fileTree(project.projectDir)
        .filter { it.name.endsWith("gradle.kts") }
    source.from(buildFiles)
}

pluginManager.withPlugin("gradlebuild.code-quality") {
    tasks {
        named("codeQuality") {
            dependsOn(detekt)
        }
    }
}

