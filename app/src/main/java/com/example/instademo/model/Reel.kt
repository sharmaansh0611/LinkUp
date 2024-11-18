package com.example.instademo.model

class Reel {
    var videoUrl: String? = null
    var caption: String? = null
    var profile: String? = null
    constructor()
    constructor(
        videoUrl: String?,
        caption: String?
    )
    {
        this.videoUrl = videoUrl
        this.caption = caption
    }
    constructor(
        videoUrl: String?,
        caption: String?,
        profile: String?
    ){
        this.videoUrl = videoUrl
        this.caption = caption
        this.profile = profile

    }

}