package me.magicsu.onedayup.model

import me.magicsu.onedayup.IdentifiableDTO
import me.magicsu.onedayup.jooq.tables.pojos.User

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
class UserDTO : User(), IdentifiableDTO<Long>