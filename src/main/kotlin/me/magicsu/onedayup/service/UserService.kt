package me.magicsu.onedayup.service

import me.magicsu.onedayup.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Author: sush
 * Date: 2018/02/24.
 * Function:
 */
@Service
class UserService {

    @Autowired
    private lateinit var userRosoritory: UserRepository


}