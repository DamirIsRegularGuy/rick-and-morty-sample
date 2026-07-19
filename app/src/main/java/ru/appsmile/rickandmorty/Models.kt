package ru.appsmile.rickandmorty.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Обёртка над ответом API /character — содержит пагинацию (info) и список персонажей.
 */
data class CharacterResponse(
    @SerializedName("info")
    val info: Info,
    @SerializedName("results")
    val results: List<ResultItem>
)

/**
 * Информация о пагинации. next == null значит, что дальше страниц нет.
 */
data class Info(
    @SerializedName("count")
    val count: Int,
    @SerializedName("pages")
    val pages: Int,
    @SerializedName("next")
    val next: String?,
    @SerializedName("prev")
    val prev: String?
)

data class ResultItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("species")
    val species: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("origin")
    val origin: LocationItem,
    @SerializedName("location")
    val location: LocationItem,
    @SerializedName("episode")
    val episode: List<String>,
    @SerializedName("created")
    val created: String
) : Serializable

data class LocationItem(
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String
) : Serializable
