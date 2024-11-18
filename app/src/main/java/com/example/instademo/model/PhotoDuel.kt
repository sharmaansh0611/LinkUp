package com.example.instademo.model


data class PhotoDuel(
    var id: String = "",
    var image1Url: String? = null,
    var image2Url: String? = null,
    var caption1: String = "",
    var caption2: String = "",
    var startTime: Long = 0,
    var endTime: Long = 0,
    var userId: String = "",
    var votesImage1: Int = 0,
    var votesImage2: Int = 0,
    var winner: String? = null // "image1", "image2", or "draw"
)

