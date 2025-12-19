import funnyblockdoormod.funnyblockdoormod.serialize.Serializable
import funnyblockdoormod.funnyblockdoormod.serialize.TestSerializable
import funnyblockdoormod.funnyblockdoormod.serialize.Serializable.Companion.RootRef
import funnyblockdoormod.funnyblockdoormod.serialize.TestSerializable.Companion.fromNBT
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

object SerializableTester {

    private val logger = LoggerFactory.getLogger("SerializableTester")

    data class TestResult(
        val passed: Int,
        val failed: Int,
        val total: Int
    ) {
        val success: Boolean get() = failed == 0
    }

    fun runAllTests(): TestResult {
        logger.info("=== Starting Serializable System Tests ===")

        var passed = 0
        var failed = 0

        val tests = listOf(
            "Basic Serialization" to ::testBasicSerialization,
            "Hard Reference Caching" to ::testHardReferenceCaching,
            "Weak Reference Caching" to ::testWeakReferenceCaching,
            "Delete Functionality" to ::testDeleteFunctionality,
            "Multiple Objects Same Region" to ::testMultipleObjectsSameRegion,
            "GetOrCreate Behavior" to ::testGetOrCreateBehavior,
            "Concurrent Access" to ::testConcurrentAccess,
            "Multiple Regions Isolation" to ::testMultipleRegionsIsolation,
            "Nonexistent Key" to ::testNonexistentKey,
            "Empty Save State" to ::testEmptySaveState
        )

        for ((name, test) in tests) {
            try {
                logger.info("Running test: $name")
                test()
                logger.info("✓ PASSED: $name")
                passed++
            } catch (e: AssertionError) {
                logger.error("✗ FAILED: $name - ${e.message}")
                failed++
            } catch (e: Exception) {
                logger.error("✗ ERROR: $name - ${e.message}", e)
                failed++
            }
        }

        val total = tests.size
        logger.info("=== Test Results: $passed/$total passed, $failed failed ===")

        return TestResult(passed, failed, total)
    }

    private fun testBasicSerialization() = runBlocking {
        val region = "test_basic"
        val key = "key1"
        val original = TestSerializable(region, key, "test data")
        val stored = Serializable.getOrCreate(
            RootRef.HARD,
            region,
            key,
            TestSerializable::fromNBT
        ) { original } as TestSerializable

        assert(stored.data == "test data") { "Data mismatch: expected 'test data', got '${stored.data}'" }
    }

    private fun testHardReferenceCaching() = runBlocking {
        val region = "test_hard_ref"
        val key = "key1"
        val obj = TestSerializable(region, key, "cached data")

        val first = Serializable.getOrCreate(
            RootRef.HARD,
            region,
            key,
            TestSerializable::fromNBT
        ) { obj }

        val second = Serializable.getCached(region, key)

        assert(second != null) { "Cached object should not be null" }
        assert(first === second) { "Hard references should return same instance" }
    }

    private fun testWeakReferenceCaching() = runBlocking {
        val region = "test_weak_ref"
        val key = "key1"
        var obj: TestSerializable? = TestSerializable(region, key, "weak data")

        Serializable.getOrCreate(
            RootRef.WEAK,
            region,
            key,
            TestSerializable::fromNBT
        ) { obj!! }

        // Should be cached initially
        val cached = Serializable.getCached(region, key)
        assert(cached != null) { "Weak reference should be cached initially" }

        // Clear reference and suggest GC
        obj = null
        System.gc()
        delay(100)

        // Just verify no crash (GC is non-deterministic)
        Serializable.getCached(region, key)
    }

    private fun testDeleteFunctionality() = runBlocking {
        val region = "test_delete"
        val key = "key1"
        val obj = TestSerializable(region, key, "to be deleted")

        Serializable.getOrCreate(
            RootRef.HARD,
            region,
            key,
            TestSerializable::fromNBT
        ) { obj }

        obj.deleteSelf()
        Serializable.saveState()

        val cached = Serializable.getCached(region, key)
        assert(cached == null) { "Deleted object should not be in cache" }
    }

    private fun testMultipleObjectsSameRegion() = runBlocking {
        val region = "test_multiple"

        val obj1 = TestSerializable(region, "key1", "data1")
        val obj2 = TestSerializable(region, "key2", "data2")
        val obj3 = TestSerializable(region, "key3", "data3")

        Serializable.getOrCreate(RootRef.HARD, region, "key1", TestSerializable::fromNBT) { obj1 }
        Serializable.getOrCreate(RootRef.HARD, region, "key2", TestSerializable::fromNBT) { obj2 }
        Serializable.getOrCreate(RootRef.HARD, region, "key3", TestSerializable::fromNBT) { obj3 }

        val cached1 = Serializable.getCached(region, "key1") as? TestSerializable
        val cached2 = Serializable.getCached(region, "key2") as? TestSerializable
        val cached3 = Serializable.getCached(region, "key3") as? TestSerializable

        assert(cached1?.data == "data1") { "Object 1 data mismatch" }
        assert(cached2?.data == "data2") { "Object 2 data mismatch" }
        assert(cached3?.data == "data3") { "Object 3 data mismatch" }
    }

    private fun testGetOrCreateBehavior() = runBlocking {
        val region = "test_get_or_create"
        val key = "key1"

        val original = TestSerializable(region, key, "original data")

        val first = Serializable.getOrCreate(
            RootRef.HARD,
            region,
            key,
            TestSerializable::fromNBT
        ) { original } as TestSerializable

        assert(first.data == "original data") { "First creation data mismatch" }

        val second = Serializable.getOrCreate(
            RootRef.HARD,
            region,
            key,
            TestSerializable::fromNBT
        ) { TestSerializable(region, key, "new data") } as TestSerializable

        assert(first === second) { "Should return cached instance" }
        assert(second.data == "original data") { "Should not use supplier for cached object" }
    }

    private fun testConcurrentAccess() = runBlocking {
        val region = "test_concurrent"

        val jobs = List(10) { index ->
            async {
                val obj = TestSerializable(region, "key_$index", "data_$index")
                Serializable.getOrCreate(
                    RootRef.HARD,
                    region,
                    "key_$index",
                    TestSerializable::fromNBT
                ) { obj }
            }
        }

        jobs.forEach { it.await() }

        for (i in 0 until 10) {
            val cached = Serializable.getCached(region, "key_$i") as? TestSerializable
            assert(cached != null) { "key_$i should be cached" }
            assert(cached?.data == "data_$i") { "key_$i data mismatch: expected 'data_$i', got '${cached?.data}'" }
        }
    }

    private fun testMultipleRegionsIsolation() = runBlocking {
        val region1 = "region_1"
        val region2 = "region_2"
        val sharedKey = "shared_key"

        val obj1 = TestSerializable(region1, sharedKey, "data1")
        val obj2 = TestSerializable(region2, sharedKey, "data2")

        Serializable.getOrCreate(RootRef.HARD, region1, sharedKey, TestSerializable::fromNBT) { obj1 }
        Serializable.getOrCreate(RootRef.HARD, region2, sharedKey, TestSerializable::fromNBT) { obj2 }

        val cached1 = Serializable.getCached(region1, sharedKey) as? TestSerializable
        val cached2 = Serializable.getCached(region2, sharedKey) as? TestSerializable

        assert(cached1 != null && cached2 != null) { "Both objects should be cached" }
        assert(cached1 !== cached2) { "Different regions should have different object instances" }
        assert(cached1?.data == "data1") { "Region 1 data mismatch" }
        assert(cached2?.data == "data2") { "Region 2 data mismatch" }
    }

    private fun testNonexistentKey() = runBlocking {
        val result = Serializable.get(
            RootRef.HARD,
            "nonexistent_region",
            "nonexistent_key",
            TestSerializable::fromNBT
        )

        assert(result == null) { "Should return null for nonexistent keys" }
    }

    private fun testEmptySaveState() {
        // Should not throw any exceptions
        Serializable.saveState()
    }
}