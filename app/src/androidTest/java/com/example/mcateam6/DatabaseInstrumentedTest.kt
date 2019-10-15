package com.example.mcateam6


import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mcateam6.database.RemoteDatabase
import com.example.mcateam6.datatypes.Attribute
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
        basicUpload()
    }

    private fun basicUpload() : String {
        val task = db.upload(
            "TestProduct",
            "표본",
            "0123456789",
            "This is a description",
            emptyList(),
            emptyMap()
        )
        val res = Tasks.await(task, time, timeUnit)
        Log.i("FirebaseDatabase", "ID of document is: " + res.id)
        assertNotNull("Upload failed", res)
        return res.id
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

    @Test
    fun test_uploadDownload_basic() {
        val id = basicUpload()
        val res = Tasks.await(db.getByID_Raw(id))
        assertEquals("id of the downloaded document is different from the uploaded documents", id, res.id)
    }

    @Test
    fun test_uploadProductDetailed() {
        val ingre1 = Product("Ingredient1", "name1", "123123123", "This is a ingredient", emptyList(), mapOf(Pair(Attribute.VEGAN, false), Pair(Attribute.VEGETARIAN, true)))
        val ingre2 = Product("Ingredient2", "name2", "321321321", "This is a ingredient", emptyList(), mapOf(Pair(Attribute.VEGAN, false), Pair(Attribute.VEGETARIAN, false)))
        val prod = Product("Product", "name3", "456654456", "This is a product", listOf(ingre1, ingre2), mapOf(Pair(Attribute.VEGAN, false), Pair(Attribute.VEGETARIAN, false)))
        val task = db.upload(ingre1, ingre2, prod)
        val res = Tasks.await(task, time, timeUnit)
        assertNotNull("Upload failed", res)
    }

    @Test
    fun test_downloadBasic() {
        val id = "ocIj7Vfsvd3TeceqZjLR"
        val res = Tasks.await(db.getByID_Raw(id))
        assertEquals("id of the downloaded document is different from the uploaded documents", id, res.id)
    }

    @Test
    fun test_downloadProduct() {
        val id = "ocIj7Vfsvd3TeceqZjLR"
        val res = Tasks.await(db.getProductById(id))
        assertNotNull("Downloaded product is null", res)
    }
}
