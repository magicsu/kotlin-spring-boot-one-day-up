package me.magicsu.onedayup.model

import me.magicsu.onedayup.IdentifiableDTO
import me.magicsu.onedayup.jooq.tables.pojos.UserFavorite

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
class UserFavoriteDTO : UserFavorite(), IdentifiableDTO<Long>