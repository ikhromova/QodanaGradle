/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.kotlin.dsl.support

import org.gradle.util.TextUtil.normaliseFileSeparators

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


fun zipTo(zipFile: File, baseDir: File) {
    val files = baseDir.walkTopDown().filter { it.isFile }
    zipTo(zipFile, baseDir, files)
}


fun zipTo(zipFile: File, baseDir: File, files: Sequence<File>) {
    val entries = files.map { file ->
        val path = file.relativeTo(baseDir).path
        val bytes = file.readBytes()
        normaliseFileSeparators(path) to bytes
    }
    zipTo(zipFile, entries)
}


fun zipTo(zipFile: File, entries: Sequence<Pair<String, ByteArray>>) {
    zipTo(zipFile.outputStream(), entries)
}


fun zipTo(outputStream: OutputStream, entries: Sequence<Pair<String, ByteArray>>) {
    ZipOutputStream(outputStream).use { zos ->
        entries.forEach { entry ->
            val (path, bytes) = entry
            zos.putNextEntry(ZipEntry(path).apply { size = bytes.size.toLong() })
            zos.write(bytes)
            zos.closeEntry()
        }
    }
}


fun unzipTo(outputDirectory: File, zipFile: File) {
    ZipFile(zipFile).use { zip ->
        for (entry in zip.entries()) {
            unzipEntryTo(outputDirectory, zip, entry)
        }
    }
}


private
fun unzipEntryTo(outputDirectory: File, zip: ZipFile, entry: ZipEntry) {
    val output = File(outputDirectory, entry.name)
    if (!output.canonicalPath.startsWith(outputDirectory.canonicalPath)) {
        throw IOException("Zip entry path outside of output directory")
    }
    if (entry.isDirectory) {
        output.mkdirs()
    } else {
        output.parentFile.mkdirs()
        zip.getInputStream(entry).use { it.copyTo(output) }
    }
}


private
fun InputStream.copyTo(file: File): Long =
    file.outputStream().use { copyTo(it) }
