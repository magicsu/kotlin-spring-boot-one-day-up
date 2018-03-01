package me.magicsu.onedayup.model

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
data class Page(var list: List<Any>?,
                var pageNum: Int,
                var pageSize: Int,
                var totalPage: Int,
                var totalRow: Int)