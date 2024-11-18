package com.example.instademo.model

data class User(var name: String? = "",
                var email: String? = "",
                var bio: String? = "",
                var imageurl: String? = "",
                var id: String? = "",
                var isPrivate: Boolean? = false)
