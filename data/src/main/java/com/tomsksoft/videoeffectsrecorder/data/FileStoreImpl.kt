package com.tomsksoft.videoeffectsrecorder.data

import com.tomsksoft.videoeffectsrecorder.domain.FileStore
import java.io.File

class FileStoreImpl(override val directory: File): FileStore {

    override fun getList() = directory.listFiles()!!.toList()
}