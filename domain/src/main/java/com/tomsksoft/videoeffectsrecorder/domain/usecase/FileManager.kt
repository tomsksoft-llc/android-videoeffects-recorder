package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.FileStore
import java.io.File
import java.util.Objects

class FileManager(private val fileStore: FileStore) {
    /**
     * Create new file with unique name. It's guaranteed that it doesn't exist yet.
     * Filename matches pattern `<base name>\[_<index>\].<extension>`,
     * where index starts from zero and increases itself if it is needed.
     */
    fun create(baseName: String, extension: String, mimeType: String): File {
        if (!fileStore.directory.exists())
            fileStore.directory.mkdirs()

        val list = fileStore.getList()

        if (list.none { it.name == "$baseName.$extension" })
            return fileStore.create("$baseName.$extension", mimeType)

        val sortedTakenIndices: IntArray = list.stream()
            .filter { it.extension == extension }
            .map(File::nameWithoutExtension)
            .filter { it.startsWith("${baseName}_") }
            .map { it.substring(baseName.length + 1).toIntOrNull() }
            .filter(Objects::nonNull)
            .mapToInt { it!! }
            .filter { it >= 0 }
            .sorted()
            .toArray()

        var minFreeIndex = 0

        while (
            minFreeIndex < sortedTakenIndices.size
            && sortedTakenIndices[minFreeIndex] == minFreeIndex
        ) minFreeIndex++

        return fileStore.create("${baseName}_$minFreeIndex.$extension", mimeType)
    }
}