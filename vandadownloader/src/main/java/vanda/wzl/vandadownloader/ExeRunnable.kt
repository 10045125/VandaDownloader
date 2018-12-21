package vanda.wzl.vandadownloader

interface ExeRunnable : Runnable {
    fun sofar(): Long
    fun complete(): Boolean
}