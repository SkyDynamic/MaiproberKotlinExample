package io.skydynamic.maiprober_example

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class LoginResponse(val errcode: Int? = null, val message: String)

private const val loginUrl = "https://www.diving-fish.com/api/maimaidxprober/login"

// 随机延迟
suspend fun delayRandomTime(diff: Int) {
    val duration = 1000L * (diff + 1) + 1000L * 5 * Random.nextDouble()
    withContext(Dispatchers.IO) {
        delay(duration.toLong())
    }
}

// 验证水鱼查分器账号
suspend fun verifyProberAccount(username: String, password: String) : Boolean {
    val resp: HttpResponse = client.post(loginUrl) {
        headers {
            append(HttpHeaders.ContentType, "application/json;charset=UTF-8")
            append(HttpHeaders.Referrer, "https://www.diving-fish.com/maimaidx/prober/")
            append(HttpHeaders.Origin, "https://www.diving-fish.com")
        }
        contentType(ContentType.Application.Json)
        setBody("""{"username":"$username","password":"$password"}""")
    }
    val body: LoginResponse = resp.body()
    return body.errcode == null
}

suspend fun uploadMaimaiProberData(
    username: String,
    password: String,
    authUrl: String
) {
    println("开始更新Maimai成绩")

    // 登录MaimaiDX主页并保存cookie
    println("登录MaimaiDX主页...")
    client.get(authUrl) {
        headers {
            append(HttpHeaders.Connection, "keep-alive")
            append("Upgrade-Insecure-Requests", "1")
            append(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/81.0.4044.138 Safari/537.36 NetType/WIFI " +
                    "MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x6307001e)")
            append(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9," +
                    "image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            append("Sec-Fetch-Site", "none")
            append("Sec-Fetch-Mode", "navigate")
            append("Sec-Fetch-User", "?1")
            append("Sec-Fetch-Dest", "document")
            append(HttpHeaders.AcceptEncoding, "gzip, deflate, br")
            append(HttpHeaders.AcceptLanguage, "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
        }
    }

    val result = client.get("https://maimai.wahlap.com/maimai-mobile/home/")

    if (result.bodyAsText().contains("错误")) {
        throw RuntimeException("登录公众号失败")
    }

    // 难度列表
    val diffNameList = listOf(
        "Basic",     // diff = 0
        "Advanced",  // diff = 1
        "Expert",    // diff = 2
        "Master",    // diff = 3
        "Re:Master"  // diff = 4
    )

    var diff = 0
    for (diffName in diffNameList) {
        println("获取 Maimai-DX $diffName 难度成绩数据")
        delayRandomTime(diff)

        with(client) {
            // 获取成绩HTML数据
            val scoreResp: HttpResponse = get(
                "https://maimai.wahlap.com/maimai-mobile/record/musicGenre/search/?genre=99&diff=$diff"
            )
            val body = scoreResp.bodyAsText()

            val data = Regex("<html.*>([\\s\\S]*)</html>")
                .find(body)?.groupValues?.get(1)?.replace("\\s+/g", " ")

            println("上传 Maimai-DX $diffName 难度成绩到 Diving-Fish 查分器数据库")

            // 上传HTML数据到Diving-Fish
            val resp: HttpResponse = post("https://www.diving-fish.com/api/pageparser/page") {
                headers {
                    append(HttpHeaders.ContentType, "text/plain")
                }
                contentType(ContentType.Text.Plain)
                setBody("""<login><u>$username</u><p>$password</p></login>$data""")
            }
            val respData: String = resp.bodyAsText()

            println("Diving-Fish 上传 Maimai-DX $diffName 分数接口返回信息: $respData")
        }
        diff += 1
    }
    println("Maimai 成绩上传到 Diving-Fish 查分器数据库完毕")
}