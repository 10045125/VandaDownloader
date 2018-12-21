package vanda.wzl.vandadownloader

interface ExeProgressCalc {
    fun exeProgressCalc(): Long
    fun allComplete(): Boolean
    fun speedIncrement(): String
}