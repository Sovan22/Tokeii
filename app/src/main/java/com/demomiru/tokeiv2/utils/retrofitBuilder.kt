package com.demomiru.tokeiv2.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun retrofitBuilder (): Retrofit
{
   return Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}