package me.magicsu.onedayup.service

import me.magicsu.onedayup.model.Page
import me.magicsu.onedayup.repository.VideoResourceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Author: sush
 * Date: 2018/02/25.
 * Function:
 */
@Service
class BusinessService {

    @Autowired
    private lateinit var videoResourceRepository: VideoResourceRepository

    fun dailyVideos(userId: Long?, pageNum: Int, pageSize: Int): Page {
        return videoResourceRepository.pageVideos(userId, pageNum, pageSize)
    }

}