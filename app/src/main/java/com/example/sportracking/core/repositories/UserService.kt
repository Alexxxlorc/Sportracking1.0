package com.example.sportracking.core.repositories

import com.example.sportracking.core.ResponseService
import onboarding.personal.model.UserProfile

interface UserService {
    suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit>
}