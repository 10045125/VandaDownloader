package vanda.wzl.vandadownloader.net

import java.io.InputStream

abstract class ProviderNetFileType {
    abstract fun isSupportMulti(): Boolean
    abstract fun fileSize(): Long
    abstract fun firstIntactInputStream(): InputStream
}