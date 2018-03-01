package me.magicsu.onedayup

import java.io.Serializable

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
interface IdentifiableDTO<T> : Serializable {

    fun getId() : T

}