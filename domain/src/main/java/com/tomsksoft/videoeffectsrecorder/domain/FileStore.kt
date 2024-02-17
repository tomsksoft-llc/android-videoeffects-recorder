package com.tomsksoft.videoeffectsrecorder.domain

import java.io.File

interface FileStore {

    val directory: File

    fun getList(): List<File>

    fun create(filename: String, mimeType: String): File // TODO [tva] use scoped storage
}