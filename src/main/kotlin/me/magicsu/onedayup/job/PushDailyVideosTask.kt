package me.magicsu.onedayup.job

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Author: sush
 * Date: 2018/02/26.
 * Function:
 */
@Component
class PushDailyVideosTask {

    @Scheduled(cron = "0 0 9 * * ?")
    fun PushDailyVideos() {

    }
}