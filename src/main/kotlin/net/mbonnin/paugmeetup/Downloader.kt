package net.mbonnin.paugmeetup

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import okio.Okio
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class Downloader {
    val ioContext = newFixedThreadPoolContext(nThreads = 1, name = "meetup io thread")
    val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(OauthInterceptor())
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.meetup.com/")
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .build()

    val service = retrofit.create(MeetupService::class.java)

    fun run() {
        System.out.println("paugmeetup v0")

        val allEvents = mutableListOf<PaugEvent>()
        runBlocking {
            val meventList = service.getEvents().await()

            meventList.forEachIndexed { index, mevent ->

                System.out.println("$index/${meventList.size}: ${mevent.name}")
                val attendeeList = try {
                    async(ioContext) {
                        service.getAttendance(mevent.id).await()
                    }.await()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@forEachIndexed
                }

                // prevent overusing our meetup quota (we get a HTTP 429 else)
                delay(500)

                val ee = PaugEvent(mevent, attendeeList)

                allEvents.add(ee)
            }
        }

        val type = Types.newParameterizedType(List::class.java, PaugEvent::class.java)
        val adapter = Moshi.Builder().build().adapter<List<PaugEvent>>(type)

        val buffer = Okio.buffer(Okio.sink(File("paug.json")))
        adapter.toJson(buffer, allEvents)
        buffer.flush()
        buffer.close()

        System.out.println("done")
    }
}