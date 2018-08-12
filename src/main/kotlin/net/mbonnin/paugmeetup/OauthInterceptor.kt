package net.mbonnin.paugmeetup

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import net.mbonnin.mooauth.Mooauth
import okhttp3.*
import okio.Okio
import java.net.URLEncoder

class OauthInterceptor: Interceptor {
    var token: String? = null
    val httpPort = 9999
    val client_id = System.getenv("MEETUP_CLIENT_ID")
    val redirect_uri = "http://localhost:${httpPort}"
    val authorizeUrl = "https://secure.meetup.com/oauth2/authorize?client_id=${client_id}&response_type=code&redirect_uri=${URLEncoder.encode(redirect_uri)}"

    override fun intercept(chain: Interceptor.Chain): Response {
        if (client_id == null) {
            System.err.println("You need to set MEETUP_CLIENT_ID and MEETUP_CLIENT_SECRET in your environment")
            System.exit(1)
        }
        if (token == null) {
            acquireNewToken()
            System.out.println("got token: $token")
        }

        val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${token}")
                .build()


        return chain.proceed(request)
    }

    private fun acquireNewToken() {
        Mooauth(authorizeUrl, ::exchangeCode, httpPort = httpPort).authorize()
    }

    fun exchangeCode(query: String): String {
        try {
            val params = mutableMapOf<String, String>()
            query.split("&").map { it.split("=") }.forEach {
                params.put(it[0], it[1])
            }

            val client = OkHttpClient()
            val code = params.get("code")!!

            val body = FormBody.Builder()
                    .add("client_id", System.getenv("MEETUP_CLIENT_ID"))
                    .add("client_secret", System.getenv("MEETUP_CLIENT_SECRET"))
                    .add("grant_type", "authorization_code")
                    .add("code", params.get("code")!!)
                    .add("redirect_uri", URLEncoder.encode(redirect_uri))
                    .build()
            val url = HttpUrl.parse("https://secure.meetup.com/oauth2/access")!!
                    .newBuilder()
                    .addQueryParameter("client_id", System.getenv("MEETUP_CLIENT_ID"))
                    .addQueryParameter("client_secret", System.getenv("MEETUP_CLIENT_SECRET"))
                    .addQueryParameter("grant_type", "authorization_code")
                    .addQueryParameter("code", code)
                    .addQueryParameter("redirect_uri", redirect_uri)
                    .build()

            val request = Request.Builder().url(url)
                    .post(FormBody.Builder().build())
                    .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return "Bad code: ${response.body()?.string()}"
            }

            val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = Moshi.Builder().build().adapter<Map<String, Any>>(type)

            token = response.body()?.byteStream()?.let {
                it.use {
                    val map = adapter.fromJson(Okio.buffer(Okio.source(it)))
                    map?.get("access_token") as String
                }
            }

            return "You've been authorized"
        } catch (e: Exception) {
            e.printStackTrace()
            return "Oops, something wrong happened"
        }
    }
}