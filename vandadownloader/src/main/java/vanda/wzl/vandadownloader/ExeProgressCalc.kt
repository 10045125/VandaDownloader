package vanda.wzl.vandadownloader

import vanda.wzl.vandadownloader.database.RemarkPointSql

interface ExeProgressCalc : RemarkPointSql {
    fun exeProgressCalc(): Long
    fun allComplete(): Boolean
    fun speedIncrement(): String
    fun sofar(curThreadId: Int): Long
    fun pauseComplete(curThreadId: Int)
    fun allPauseComplete(): Boolean
}