package me.magicsu.onedayup.repository

import me.magicsu.onedayup.AbstractCrudRepository
import me.magicsu.onedayup.jooq.Tables.VIDEO_RESOURCE
import me.magicsu.onedayup.jooq.tables.VideoResource
import me.magicsu.onedayup.jooq.tables.records.VideoResourceRecord
import me.magicsu.onedayup.model.Page
import me.magicsu.onedayup.model.VideoResourceDTO
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.sql.Timestamp

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
@Repository
class VideoResourceRepository(dslContext: DSLContext)
    : AbstractCrudRepository<VideoResourceRecord, Long, VideoResourceDTO>(dslContext,
        VideoResource.VIDEO_RESOURCE, VIDEO_RESOURCE.ID, VideoResourceDTO::class.java) {

    override fun filter(filterQuery: Map<String, Any>): Condition? {
        return DSL.trueCondition()
    }

    fun addVideoResource(videoResourceDTO: VideoResourceDTO): VideoResourceDTO {
        videoResourceDTO.createdAt = Timestamp(System.currentTimeMillis())
        return create(videoResourceDTO)
    }

    fun updateVideoResource(videoResourceDTO: VideoResourceDTO): VideoResourceDTO {
        videoResourceDTO.updatedAt = Timestamp(System.currentTimeMillis())
        return update(videoResourceDTO.id, videoResourceDTO)
    }

    fun getOneByTitle(title: String): VideoResourceDTO? {
        return getOne(VIDEO_RESOURCE.TITLE.eq(title))
    }

    fun pageVideos(userId: Long?, pageNum: Int, pageSize: Int): Page {
        var condition = DSL.trueCondition()
        var sortField = VIDEO_RESOURCE.PLAN_PUBLISH_DATE.desc()

        return page(pageNum, pageSize, condition, sortField)
    }
}