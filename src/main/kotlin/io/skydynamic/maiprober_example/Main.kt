package io.skydynamic.maiprober_example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
}

// 获取授权URL
suspend fun getAuthUrl(type: String) : String {
    val resp: HttpResponse = client.get("https://tgk-wcaime.wahlap.com/wc_auth/oauth/authorize/$type")
    val url = resp.request.url.toString().replace("redirect_uri=https", "redirect_uri=http")
    return url
}

fun main() = runBlocking {
    println("oauthURL: ${getAuthUrl("maimai-dx")}")
    println("请尽快复制到微信中打开")
    startProxy()
}