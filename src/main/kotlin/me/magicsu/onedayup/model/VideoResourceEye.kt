package me.magicsu.onedayup.model

import java.math.BigDecimal

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
data class VideoResourceEye(var nextPageUrl: String?,
                         var nextPublishTime: Long,
                         var newestIssueType: String?,
                         var issueList: List<IssueList>?) {

    data class IssueList(var releaseTime: Long,
                         var type: String?,
                         var date: Long,
                         var publishTime: Long,
                         var count: Int,
                         var itemList: List<ItemList>?) {

        data class ItemList(var type: String?,var data: DataBean?) {

            data class DataBean(var dataType: String?,
                                var id: Int,
                                var title: String?,
                                var description:String?,
                                var image: String?,
                                var actionUrl: String?,
                                var isShade: Boolean,
                                var category: String?,
                                var duration: Short?,
                                var playUrl: String,
                                var thumbPlayUrl: String?,
                                var cover: CoverBean?,
                                var author: Author?,
                                var releaseTime: Long?,
                                var consumption: ConsumptionBean?,
                                var score: BigDecimal) {

                data class CoverBean(var feed : String?,
                                     var detail : String?,
                                     var blurred : String?,
                                     var sharing : String?,
                                     var homepage:String?)

                data class ConsumptionBean(var collectionCount: Int,
                                           var shareCount: Int,
                                           var replyCount: Int)

                data class Author(var icon: String,
                                  var name: String)

                data class Comsumption(var collectionCount: Int,
                                       var shareCount: Int,
                                       var replyCount: Int)
            }
        }
    }
}