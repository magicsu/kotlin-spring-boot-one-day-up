package me.magicsu.onedayup

import com.google.gson.Gson
import me.magicsu.onedayup.job.ResourceCollectTask
import me.magicsu.onedayup.model.VideoResourceDTO
import me.magicsu.onedayup.model.VideoResourceEye
import me.magicsu.onedayup.repository.VideoResourceRepository
import me.magicsu.onedayup.util.HttpUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal
import java.sql.Date

@RunWith(SpringRunner::class)
@SpringBootTest
class OneDayUpApplicationTests {

	@Autowired
	private lateinit var videoResourceRepository: VideoResourceRepository

	@Test
	fun contextLoads() {
	}

	@Test
	fun collectVideoResource() {
		collectFromEyepetizer()
	}
	private fun collectFromEyepetizer() {
		var date = getZeroTimeStamp(System.currentTimeMillis())
		var paramsMap = mapOf(Pair("date", date), Pair("num", 1))

		var result = Gson().fromJson(HttpUtil.doGet(ResourceCollectTask.URL_EYEPETIZER_FEED_DAILY, paramsMap), VideoResourceEye::class.java)
		result?.let {
			ResourceCollectTask.log.info(result.toString())

			var issueList = result.issueList
			var videoItemList = issueList?.get(0)?.itemList?.filter { it -> it.type.equals("video") } // 过滤非视频资源数据

			videoItemList?.forEach { it ->
				var score: BigDecimal = BigDecimal.ZERO
				it.data?.consumption?.let {
					score = BigDecimal(it.shareCount!! * 3 + it.collectionCount!! * 2 + it.replyCount!! * 2)
				}
				it.data?.score = score
				ResourceCollectTask.log.info("title: {}, score: {}", it.data?.title, score)
			}

			var sortedVideoItemList = videoItemList?.sortedByDescending { it -> it.data?.score }
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
				videoResourceDto.coverBlurred = it.description
				videoResourceDto.coverDetail = it.description
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
