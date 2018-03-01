package me.magicsu.onedayup.controller

import com.google.common.collect.Maps
import me.magicsu.onedayup.BusinessResult
import me.magicsu.onedayup.model.Page
import me.magicsu.onedayup.service.BusinessService
import me.magicsu.onedayup.util.HttpUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
@RestController
@RequestMapping(value = "/api/v1")
class OneController {

    @Autowired
    private lateinit var businessService: BusinessService

    @RequestMapping("hello")
    fun hello(): String {
        return "Hello Spring-Boot for Kotlin."
    }

    @GetMapping("/dailyVideos")
    fun dailyVideos(
            @RequestParam(value = "userId", required = false) userId: Long,
            @RequestParam(value = "pageNum", defaultValue = "1") pageNum: Int,
            @RequestParam(value = "pageSize", defaultValue = "10") pageSize: Int): BusinessResult<Page> {
        return BusinessResult(businessService.dailyVideos(userId, pageNum, pageSize))
    }
}