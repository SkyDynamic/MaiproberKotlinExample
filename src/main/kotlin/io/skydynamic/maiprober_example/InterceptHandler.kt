package io.skydynamic.maiprober_example

import java.net.URI

suspend fun onAuthHook(authUrl: URI) {
    val urlString = authUrl.toString()
    // 修改这里的账号密码成你自己的
    val username = ""
    val password = ""

    // 将拦截的authUrl scheme改为 https
    val target = urlString.replace("http", "https")
    // 验证水鱼查分器账号密码
    if (verifyProberAccount(username, password)) {
        // 判断游戏类型
        if (target.contains("maimai-dx")) {
            // Maimai处理
            uploadMaimaiProberData(username, password, target)
        } else if (target.contains("chunithm")) {
            // 自行实现
        }
    } else {
        println("Prober账号密码错误")
    }
}