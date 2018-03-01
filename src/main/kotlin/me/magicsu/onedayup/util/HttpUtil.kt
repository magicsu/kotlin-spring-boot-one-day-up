package me.magicsu.onedayup.util

import org.apache.commons.lang3.StringUtils
import org.apache.http.*
import org.apache.http.client.HttpRequestRetryHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HttpContext
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import java.io.*
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.util.ArrayList

object HttpUtil {
    private val httpClient: CloseableHttpClient
    val CHARSET = "UTF-8"
    val log = LoggerFactory.getLogger("HttpUtil")

    init {
        val config = RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .setSocketTimeout(10 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .build()

        val cm = PoolingHttpClientConnectionManager()
        cm.maxTotal = 200            // 将最大连接数增加到200
        cm.defaultMaxPerRoute = 20   // 将每个路由基础的连接增加到20

        //请求重试处理
        val httpRequestRetryHandler = HttpRequestRetryHandler { exception, executionCount, context ->
            if (executionCount >= 5) {// 如果已经重试了5次，就放弃
                return@HttpRequestRetryHandler false
            }
            if (exception is NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                return@HttpRequestRetryHandler true
            }
            if (exception is SSLHandshakeException) {// 不要重试SSL握手异常
                return@HttpRequestRetryHandler false
            }
            if (exception is InterruptedIOException) {// 超时
                return@HttpRequestRetryHandler false
            }
            if (exception is UnknownHostException) {// 目标服务器不可达
                return@HttpRequestRetryHandler false
            }
            if (exception is ConnectTimeoutException) {// 连接被拒绝
                return@HttpRequestRetryHandler false
            }
            if (exception is SSLException) {// ssl握手异常
                return@HttpRequestRetryHandler false
            }

            val clientContext = HttpClientContext.adapt(context)
            val request = clientContext.request
            // 如果请求是幂等的，就再次尝试
            request !is HttpEntityEnclosingRequest
        }

        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config)
                .setConnectionManager(cm)
                .setRetryHandler(httpRequestRetryHandler)
                .build()
    }

    fun <T : Any> doGet(url: String, params: Map<String, T>): String? {
        return doGet(url, params, CHARSET, null)
    }

    fun <T : Any> doGet(url: String, params: Map<String, T>, urlSuffix: String): String? {
        return doGet(url, params, CHARSET, urlSuffix)
    }

    fun <T : Any> doPost(url: String, params: Map<String, T>): String? {
        return doPost(url, params, CHARSET)
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    private fun <T : Any> doGet(url: String, params: Map<String, T>?, charset: String, urlSuffix: String?): String? {
        var url = url
        if (StringUtils.isBlank(url)) {
            return null
        }

        log.info("http:get:request:url: {}, params:{}", url, params)
        var response: CloseableHttpResponse? = null
        var result: String? = null

        var httpGet: HttpGet? = null
        try {
            if (params != null && !params.isEmpty()) {
                val pairs = ArrayList<NameValuePair>(params.size)
                for ((key, value) in params) {
                    if (value != null) {
                        pairs.add(BasicNameValuePair(key, value.toString()))
                    }
                }
                url += "?" + EntityUtils.toString(UrlEncodedFormEntity(pairs, charset))
            }

            if (StringUtils.isNotEmpty(urlSuffix)) {
                url += urlSuffix
            }

            log.info("url: {}", url)
            httpGet = HttpGet(url)
            response = httpClient.execute(httpGet)

            val statusCode = response!!.statusLine.statusCode
            if (statusCode != 200) {
                httpGet.abort()
                throw RuntimeException("HttpClient,error status code :" + statusCode)
            }

        } catch (e: Exception) {
            if (httpGet != null) {
                httpGet.abort()
            }

            log.error("<<<< exception: " + e.message)
            e.printStackTrace()
        } finally {
            if (response != null) {
                val entity = response.entity

                if (entity != null) {
                    try {
                        result = EntityUtils.toString(entity, "utf-8")
                        EntityUtils.consume(entity)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }

                try {
                    response.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else {
                log.info("<<<< response == null")
            }
        }
        log.info("http:get:result: {}", result)
        return result
    }

    /**
     * HTTP Post 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    private fun <T : Any> doPost(url: String, params: Map<String, T>?, charset: String): String? {
        if (StringUtils.isBlank(url)) {
            return null
        }
        log.info("\nhttp:post:request:url: {}, params:{}", url, params!!.toString())
        var response: CloseableHttpResponse? = null
        var result: String? = null
        try {
            var pairs: MutableList<NameValuePair>? = null
            if (params != null && !params.isEmpty()) {
                pairs = ArrayList(params.size)
                for ((key, value) in params) {
                    if (value != null) {
                        pairs.add(BasicNameValuePair(key, value.toString()))
                    }
                }
            }
            val httpPost = HttpPost(url)
            if (pairs != null && pairs.size > 0) {
                httpPost.entity = UrlEncodedFormEntity(pairs, CHARSET)
            }
            response = httpClient.execute(httpPost)
            val statusCode = response!!.statusLine.statusCode
            if (statusCode != 200) {
                httpPost.abort()
                throw RuntimeException("HttpClient,error status code :" + statusCode)
            }
            val entity = response.entity
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8")
            }
            EntityUtils.consume(entity)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (response != null) {
                try {
                    response.close()
                } catch (e: IOException) {
                }

            }
        }
        log.info("http:post:result: {}", result)
        return result
    }

    fun doPostJsonAndDownload(url: String, json: String, localPath: String): String? {
        if (StringUtils.isBlank(url)) {
            return null
        }

        log.info("\nhttp:post:request:url: {}, json:{}", url, json)
        var response: CloseableHttpResponse? = null
        val result: String? = null
        try {
            val httpPost = HttpPost(url)

            httpPost.addHeader("Content-type", "application/json; charset=utf-8")
            httpPost.setHeader("Accept", "application/json")
            httpPost.entity = StringEntity(json, Charset.forName("UTF-8"))

            response = httpClient.execute(httpPost)
            val statusCode = response!!.statusLine.statusCode
            if (statusCode != 200) {
                httpPost.abort()
                throw RuntimeException("HttpClient,error status code :" + statusCode)
            }

            val entity = response.entity
            //            if (entity != null) {
            //                result = EntityUtils.toString(entity, "utf-8");
            //            }
            //            EntityUtils.consume(entity);

            if (entity != null) {
                val `in` = entity.content
                var bufferedOut: BufferedOutputStream? = null
                try {
                    // do something useful with the response
                    val buffer = ByteArray(1024)
                    val bufferedIn = BufferedInputStream(`in`)
                    var len = 0

                    val fileOutStream = FileOutputStream(File(localPath))
                    bufferedOut = BufferedOutputStream(fileOutStream)
                    while (len != -1) {
                        len = bufferedIn.read(buffer, 0, 1024)
                        bufferedOut.write(buffer, 0, len)
                    }
                    bufferedOut.flush()
                } catch (ex: IOException) {
                    // In case of an IOException the connection will be released
                    // back to the connection manager automatically
                    throw ex
                } finally {
                    // Closing the input stream will trigger connection release
                    `in`.close()
                    if (bufferedOut != null) {
                        bufferedOut.close()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (response != null) {
                try {
                    response.close()
                } catch (e: IOException) {
                }

            }
        }
        log.info("http:post:result: {}", result)
        return result
    }

    fun download(url: String, json: String): ByteArray? {
        if (StringUtils.isBlank(url)) {
            return null
        }

        log.info("\nhttp:post:request:url: {}, json:{}", url, json)
        var response: CloseableHttpResponse? = null
        var result: ByteArray? = null
        try {
            val httpPost = HttpPost(url)

            httpPost.addHeader("Content-type", "application/json; charset=utf-8")
            httpPost.setHeader("Accept", "application/json")
            httpPost.entity = StringEntity(json, Charset.forName("UTF-8"))

            response = httpClient.execute(httpPost)
            val statusCode = response!!.statusLine.statusCode
            if (statusCode != 200) {
                httpPost.abort()
                throw RuntimeException("HttpClient,error status code :" + statusCode)
            }

            val entity = response.entity
            var inputStream: InputStream? = null
            try {
                if (entity != null) {
                    inputStream = entity.content
                    result = readInputStream(inputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inputStream!!.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (response != null) {
                try {
                    response.close()
                } catch (e: IOException) {
                }

            }
        }
        log.info("http:post:result: {}", result)
        return result
    }

    fun download(url: String): ByteArray? {
        if (StringUtils.isBlank(url)) {
            return null
        }

        log.info("\nhttp:post:request:url: {} ", url)
        var response: CloseableHttpResponse? = null
        var result: ByteArray? = null
        try {
            val httpGet = HttpGet(url)

            response = httpClient.execute(httpGet)
            val statusCode = response!!.statusLine.statusCode
            if (statusCode != 200) {
                httpGet.abort()
                throw RuntimeException("HttpClient,error status code :" + statusCode)
            }

            val entity = response.entity
            var inputStream: InputStream? = null
            try {
                if (entity != null) {
                    inputStream = entity.content
                    result = readInputStream(inputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inputStream!!.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (response != null) {
                try {
                    response.close()
                } catch (e: IOException) {
                }

            }
        }
        log.info("http:post:result: {}", result)
        return result
    }

    @Throws(IOException::class)
    fun readInputStream(inputStream: InputStream?): ByteArray {
        val buffer = ByteArray(1024)
        var len = 0
        val bos = ByteArrayOutputStream()
        while (len != -1) {
            len = inputStream!!.read(buffer)
            bos.write(buffer, 0, len)
        }
        bos.close()
        return bos.toByteArray()
    }

}
