/*
 * Copyright (C) 2005-2018 YY Inc. All rights reserved.
 *  Description :ExampleInstrumentedTest.kt
 *
 *  Creation    : 2018-08-23
 *  Author      : wuzhonglianl@yy.com
 */

package vanda.wzl.vandadownloader

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("vanda.wzl.vandadownloader", appContext.packageName)
    }
}
