package me.magicsu.onedayup.repository

import me.magicsu.onedayup.AbstractCrudRepository
import me.magicsu.onedayup.jooq.tables.User.USER
import me.magicsu.onedayup.jooq.tables.records.UserRecord
import me.magicsu.onedayup.model.UserDTO
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
class UserRepository(dslContext: DSLContext)
    : AbstractCrudRepository<UserRecord, Long, UserDTO>(dslContext, USER, USER.ID, UserDTO::class.java) {

    override fun filter(filterQuery: Map<String, Any>): Condition? {
        return DSL.trueCondition()
    }

    fun addUser(userDTO: UserDTO): UserDTO {
        userDTO.createdAt = Timestamp(System.currentTimeMillis())
        return create(userDTO)
    }

    fun updateUser(userDTO: UserDTO): UserDTO {
        userDTO.updatedAt = Timestamp(System.currentTimeMillis())
        return update(userDTO.id, userDTO)
    }



}