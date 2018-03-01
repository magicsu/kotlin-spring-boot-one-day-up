package me.magicsu.onedayup.job

import com.google.gson.Gson
import me.magicsu.onedayup.model.VideoResourceDTO
import me.magicsu.onedayup.model.VideoResourceEye
import me.magicsu.onedayup.repository.VideoResourceRepository
import me.magicsu.onedayup.util.HttpUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.sql.Date

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
@Component
class ResourceCollectTask {

    @Autowired
    private lateinit var videoResourceRepository: VideoResourceRepository

    companion object {
        val log: Logger = LoggerFactory.getLogger("ResourceCollectTask")

        const val CHANNEL_EYEPETIZER = "Eyepetizer"
        const val URL_EYEPETIZER_FEED_DAILY = "http://baobab.kaiyanapp.com/api/v2/feed"

        const val WEIGHT_SHARE = 3
        const val WEIGHT_COLLECT = 2
        const val WEIGHT_REPLY = 2
    }

    /**
     * 每日零点五分抓取素材资源
     */
    @Scheduled(cron = "0 5 0 * * ?")
    fun videoResourceCollect() {
        collectFromEyepetizer()
    }

    /**
     * 开眼视频日报资源抓取
     * 每日多条视频资源，根据视频用户的评论、分享、收藏次数加权计算打分排序，只挑选分数最高视频录入
     */
    private fun collectFromEyepetizer() {
        var date = getZeroTimeStamp(System.currentTimeMillis()) - 60 * 1000 * 60 * 24
        var paramsMap = mapOf(Pair("date", date), Pair("num", 1))

        var result = Gson().fromJson(HttpUtil.doGet(URL_EYEPETIZER_FEED_DAILY, paramsMap), VideoResourceEye::class.java)

        result?.let {
            log.info(result.toString())

            var issueList = result.issueList
            var videoItemList = issueList?.get(0)?.itemList?.filter { it -> it.type.equals("video") } // 过滤非视频资源数据

            // 根据视频分享、收藏、评论数量打分并排序
            videoItemList?.forEach { it ->
                var score: BigDecimal = BigDecimal.ZERO
                it.data?.consumption?.let {
                    score = BigDecimal(it.shareCount * WEIGHT_SHARE + it.collectionCount * WEIGHT_COLLECT
                            + it.replyCount * WEIGHT_REPLY)
                }
                it.data?.score = score
            }

            var sortedVideoItemList = videoItemList?.sortedByDescending { it -> it.data?.score }  // 排序
            var firstItem = sortedVideoItemList?.first()
            firstItem?.data?.let {
                var videoResourceDto = VideoResourceDTO()
                videoResourceDto.channel = ResourceCollectTask.CHANNEL_EYEPETIZER

                videoResourceDto.title = it.title
                videoResourceDto.description = it.description
                videoResourceDto.playUrl = it.playUrl
                videoResourceDto.category = it.category
                videoResourceDto.authorAvatar = it.author?.icon
                videoResourceDto.authorName = it.author?.name
                videoResourceDto.coverFeed = it.cover?.feed
                videoResourceDto.coverBlurred = it.cover?.blurred
                videoResourceDto.coverDetail = it.cover?.detail
                videoResourceDto.thumbPlayUrl = it.thumbPlayUrl
                videoResourceDto.duration = it.duration
                videoResourceDto.score = it.score
                videoResourceDto.publishTime = Date(it.releaseTime!!)
                videoResourceDto.collectDate = Date(System.currentTimeMillis())
                videoResourceDto.planPublishDate = Date(System.currentTimeMillis())

                if (videoResourceRepository.getOneByTitle(it.title!!) == null) {
                    ResourceCollectTask.log.info(" add video resource: {}", it.title)
                    videoResourceRepository.addVideoResource(videoResourceDto)
                }
            }
        }
    }

    private fun getZeroTimeStamp(time: Long): Long {
        return time - time % (24 * 60 * 60 * 1000) - (8 * 60 * 60 * 1000).toLong()
    }
}