package com.tomsksoft.videoeffectsrecorder.domain

/**
 * @param T file
 */
interface FileStore<T> {
    fun create(filename: String, extension: String, mimeType: String): T
}