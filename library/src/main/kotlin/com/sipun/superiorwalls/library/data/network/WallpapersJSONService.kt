package com.sipun.superiorwalls.library.data.network

import com.sipun.superiorwalls.library.data.models.Wallpaper
import retrofit2.http.GET
import retrofit2.http.Url

interface WallpapersJSONService {
    @GET
    suspend fun getJSON(@Url url: String): List<Wallpaper>
}