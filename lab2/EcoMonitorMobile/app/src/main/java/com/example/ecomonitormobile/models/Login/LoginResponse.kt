package com.example.ecomonitormobile.models.Login

data class LoginResponse(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val isActive: Boolean,
    val lastLogin: String
)
