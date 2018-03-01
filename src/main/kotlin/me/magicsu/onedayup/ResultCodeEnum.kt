package me.magicsu.onedayup

/**
 * Author: sush
 * Function:
 */
enum class ResultCodeEnum(var code: Int) {
    成功(0),

    请求失败(-10000),
    无效的请求(-10001),
    无效的内容(-10002);

    fun getName(): String {
        return this.name
    }
}
