package com.example.composetutorial.data

import com.example.composetutorial.data.dao.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    val userData: Flow<User?> = userDao.getUser()

    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }
    suspend fun updateUser(user: User) {
        userDao.update(user)
    }
}