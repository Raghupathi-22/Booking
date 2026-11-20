package com.hyderabad.booking.common.dto

data class UserDto(
    val id: Long,
    val phone: String,
    val name: String,
    val role: String,
    val fcmToken: String? = null,
    val createdAt: String? = null
)
