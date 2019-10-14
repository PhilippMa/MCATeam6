package com.example.mcateam6


import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mcateam6.database.RemoteDatabase
import com.example.mcateam6.datatypes.Product
import com.google.android.gms.tasks.Tasks
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Make sure, that the application is connected to the firebase project in Android Studio
 *
 */
@RunWith(AndroidJUnit4::class)
class DatabaseInstrumentedTest {

    private lateinit var db: RemoteDatabase



    @Before
    fun initialize() {
        db = RemoteDatabase()
    }

    //Timeout for upload tests
    private val time: Long = 10
    private val timeUnit = TimeUnit.SECONDS

    /**
     * Waits uploads data to Firestore database and waits for the completion of the upload procedure
     *
     * Please check manually if the product has been uploaded correctly
     * @throws java.util.concurrent.TimeoutException If upload takes longer then the timeout set in the variables time and timeUnit
     * @see time, timeUnit
     */
    @Test
    fun test_basicUpload() {
        val task = db.upload("TestProduct", "표본", "0123456789", "This is a description", emptyList(), emptyMap())
        val res = Tasks.await(task, time, timeUnit)
        Log.i("FirebaseDatabase", "ID of document is: " + res.id)
        assertNotNull("Upload failed", res)
    }

    /**
     * Waits uploads data to Firestore database and waits for the completion of the upload procedure
     *
     * Please check manually if the product has been uploaded correctly
     * @throws java.util.concurrent.TimeoutException If upload takes longer then the timeout set in the variables time and timeUnit
     * @see time, timeUnit
     */
    @Test
    fun test_productUpload() {
        val prod = Product("TestProduct", "표본", "0123456789", "This is a description", emptyList(), emptyMap())
        val task = db.upload(prod)
        val res = Tasks.await(task, time, timeUnit)
        Log.i("FirebaseDatabase", "ID of document is: " + res.id)
        assertNotNull("Upload failed", res)
    }
}
