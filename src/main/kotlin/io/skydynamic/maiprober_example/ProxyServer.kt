package io.skydynamic.maiprober_example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.URI

fun startProxy() {
    // 新建一个NettyApplicationServer, 端口为2560
    embeddedServer(Netty, host = "0.0.0.0", port = 2560) {
        // 添加拦截成功页面
        routing {
            get("/success") {
                call.respond(HttpStatusCode.OK, "查询完成，请返回查分器查看")
            }
        }

        // 添加拦截处理
        intercept(ApplicationCallPipeline.Call) {
            val requestUrl = call.request.uri
            try {
                val uri = URI(requestUrl)
                // 如果拦截到的请求URL为 http 协议
                if (uri.scheme.equals("http")) {
                    // 如果拦截到的是 tgk-wcaime.wahlap.com
                    if (uri.host.equals("tgk-wcaime.wahlap.com")) {
                        // 跳转到 success 界面
                        call.respondRedirect("http://127.0.0.1:2560/success")
                        // 处理请求
                        onAuthHook(uri)
                    }
                } else
                    call.respond(HttpStatusCode.BadRequest, "Invalid URL")
                return@intercept
            } catch (_: Exception) {
            }
        }
    }.start(wait = true)
}