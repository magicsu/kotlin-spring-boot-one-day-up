package me.magicsu.onedayup

import java.io.Serializable

class BusinessResult<T> : Serializable {

    var code: Int = 0
    var message: String? = null
    var data: T? = null

    constructor()

    constructor(data: T) {
        this.code = 0
        this.message = null
        this.data = data
    }

    constructor(code: Int, message: String) {
        this.code = code
        this.message = message
        this.data = null
    }

    constructor(code: Int, message: String, data: T) {
        this.code = code
        this.message = message
        this.data = data
    }

    constructor(resultCodeEnum: ResultCodeEnum) {
        this.code = resultCodeEnum.code
        this.message = resultCodeEnum.getName()
        this.data = null
    }

    override fun toString(): String {
        return "BusinessResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}'
    }

    companion object {
        private const val serialVersionUID = -3700423587200262408L

        fun <T> newInstance(): BusinessResult<T> {
            return BusinessResult()
        }
    }
}
