package ru.appsmile.rickandmorty.ui.episodes

import ru.appsmile.rickandmorty.model.EpisodeItem

sealed class EpisodeListRow {
    data class SeasonHeader(val season: Int) : EpisodeListRow()
    data class Row(val episode: EpisodeItem, val numberInSeason: Int) : EpisodeListRow()
}

private val EPISODE_CODE_REGEX = Regex("S(\\d+)E(\\d+)")

/**
 * Превращает плоский список эпизодов (уже отсортированный API по id/хронологии)
 * в список строк с заголовками сезонов, парся код вида "S01E01".
 */
fun buildEpisodeRows(episodes: List<EpisodeItem>): List<EpisodeListRow> {
    val rows = mutableListOf<EpisodeListRow>()
    var lastSeason: Int? = null

    episodes.forEach { episode ->
        val match = EPISODE_CODE_REGEX.find(episode.episodeCode)
        val season = match?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val numberInSeason = match?.groupValues?.get(2)?.toIntOrNull() ?: 0

        if (season != lastSeason) {
            rows += EpisodeListRow.SeasonHeader(season)
            lastSeason = season
        }
        rows += EpisodeListRow.Row(episode, numberInSeason)
    }

    return rows
}