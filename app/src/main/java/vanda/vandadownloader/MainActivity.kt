package vanda.vandadownloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
//import kotlinx.coroutines.experimental.*
//import kotlinx.coroutines.experimental.android.Main
//import kotlinx.coroutines.experimental.channels.ReceiveChannel
//import kotlinx.coroutines.experimental.channels.produce
//import kotlinx.coroutines.experimental.selects.select
import vanda.wzl.vandadownloader.DownloadRunnable

//import kotlin.coroutines.experimental.CoroutineContext

class MainActivity : AppCompatActivity() {

    private val url: String = "http://dlied5.myapp.com/myapp/1104466820/sgame/2017_com.tencent.tmgp.sgame_h177_1.42.1.6_a6157f.apk"

    companion object {
        const val TAG = "vanda"
    }

    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this))
        testDownload()
    }

//    private fun testAysn() {
//        Log.e(TAG, "testAysn start thread = " + Thread.currentThread().name)
//
//        GlobalScope.launch(Dispatchers.Main) {
//            textView?.text = loadData()
//            selectChannelTest()
//        }
//
//        Log.e(TAG, "testAysn end thread = " + Thread.currentThread().name)
//    }
//
//    private suspend fun loadData(): String = withContext(Dispatchers.IO) {
//        Log.e(TAG, "loadData start thread = " + Thread.currentThread().name)
//
//        delay(2000)
//
//        Log.e(TAG, "loadData end thread = " + Thread.currentThread().name)
//
//        "Result"
//    }
//
//    fun fizz(context: CoroutineContext) = produce<String>(context) {
//        while (true) { // sends "Fizz" every 300 ms
//            delay(3000)
//            send("Fizz")
//        }
//    }
//
//    fun buzz(context: CoroutineContext) = produce<String>(context) {
//        while (true) { // sends "Buzz!" every 500 ms
//            delay(5000)
//            send("Buzz!")
//        }
//    }
//
//    suspend fun selectFizzBuzz(fizz: ReceiveChannel<String>, buzz: ReceiveChannel<String>) {
//        select<Unit> { // <Unit> means that this select expression does not produce any result
//            fizz.onReceive { value ->  // this is the first select clause
//                println("fizz -> '$value'")
//            }
//            buzz.onReceive { value ->  // this is the second select clause
//                println("buzz -> '$value'")
//            }
//        }
//    }
//
//    suspend fun selectChannelTest(): String = withContext(Dispatchers.IO) {
//        val fizz = fizz(coroutineContext)
//        val buzz = buzz(coroutineContext)
//        repeat(7) {
//            selectFizzBuzz(fizz, buzz)
//        }
//
//        ""
//    }
//            runBlocking<Unit> {
//        val fizz = fizz(coroutineContext)
//        val buzz = buzz(coroutineContext)
//        repeat(7) {
//            selectFizzBuzz(fizz, buzz)
//        }
//        coroutineContext.cancelChildren() // cancel fizz & buzz coroutines
//    }

    fun testDownload() {
//        Executors.newSingleThreadScheduledExecutor().execute { DownloadRunnable(url) }

        Thread(DownloadRunnable(url)).start()
    }
}
