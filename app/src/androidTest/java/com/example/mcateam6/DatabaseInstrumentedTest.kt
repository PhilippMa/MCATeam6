package com.example.mcateam6


import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
    private val ingre1 = Product("Ingredient1", "name1", "123123123", "This is a ingredient", emptyList(), mapOf(Pair(Attribute.VEGAN, false), Pair(Attribute.VEGETARIAN, true)))
    private val ingre2 = Product("Ingredient2", "name2", "321321321", "This is a ingredient", emptyList(), mapOf(Pair(Attribute.VEGAN, false), Pair(Attribute.VEGETARIAN, false)))
    private val prod = Product("Product", "name3", "456654456", "This is a product", listOf(ingre1, ingre2), mapOf(Pair(Attribute.VEGAN, false), Pair(Attribute.VEGETARIAN, false)))



    @Before
    fun initialize() {
        db = RemoteDatabase()
        db.signIn()
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
        val task = db.upload(ingre1, ingre2, prod)
        val res = Tasks.await(task, time, timeUnit)
        // TODO change assert
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
        val id = "xZQQxyHXBYf25i6I2UWT"
        val res = Tasks.await(db.getProductById(id)).toProduct()
        Log.i(this.javaClass.name, "Product: $res")
        assertNotNull("Downloaded product is null", res)
    }

    @Test
    fun test_signIn() {
        val task = db.signIn()
        val res = Tasks.await(task, time, timeUnit)
        assertNotNull("user is null", res)
    }

    @Test
    fun test_getProductByEnglishName() {
        val task = db.getProductByEnglishName("Product")
        val res = Tasks.await(task, time, timeUnit)
        Log.i(this.javaClass.name, "Product: $res")
        assertTrue("products not identical", Product.equals(prod, res))
    }

    @Test
    fun test_getProductByKoreanName() {
        val task = db.getProductByKoreanName("name3")
        val res = Tasks.await(task, time, timeUnit)
        Log.i(this.javaClass.name, "Product: $res")
        assertTrue("products not identical", Product.equals(prod, res))
    }

    @Test
    fun test_getProductByBarcode() {
        val task = db.getProductByBarcode("456654456")
        val res = Tasks.await(task, time, timeUnit)
        Log.i(this.javaClass.name, "Product: $res")
        assertTrue("products not identical", Product.equals(prod, res))
    }

    @Test
    fun test_uploadImage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val image = context.resources.openRawResource(R.raw.firework)
        val task = db.uploadImage("firework", image)
        val res = Tasks.await(task, time, timeUnit)
        assertTrue("error on upload", res.bytesTransferred > 0)
    }
}
