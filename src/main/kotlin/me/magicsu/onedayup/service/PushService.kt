package me.magicsu.onedayup.service

import me.magicsu.onedayup.vendor.MiPushVendor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Author: sush
 * Date: 2018/02/26.
 * Function:
 */
@Service
class PushService {

    @Autowired
    private lateinit var miPushVendor: MiPushVendor

    fun pushDailyVideos() {

    }
}