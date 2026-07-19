package ru.appsmile.rickandmorty.ui.episodes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.appsmile.rickandmorty.R
import ru.appsmile.rickandmorty.databinding.ItemEpisodeBinding
import ru.appsmile.rickandmorty.databinding.ItemEpisodeHeaderBinding

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_EPISODE = 1

class EpisodesAdapter : ListAdapter<EpisodeListRow, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is EpisodeListRow.SeasonHeader -> VIEW_TYPE_HEADER
        is EpisodeListRow.Row -> VIEW_TYPE_EPISODE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(ItemEpisodeHeaderBinding.inflate(inflater, parent, false))
        } else {
            EpisodeViewHolder(ItemEpisodeBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is EpisodeListRow.SeasonHeader -> (holder as HeaderViewHolder).bind(item)
            is EpisodeListRow.Row -> (holder as EpisodeViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(private val binding: ItemEpisodeHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: EpisodeListRow.SeasonHeader) {
            binding.textViewSeasonTitle.text =
                binding.root.context.getString(R.string.season_format, header.season)
        }
    }

    class EpisodeViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(row: EpisodeListRow.Row) {
            binding.textViewEpisodeCode.text = row.episode.episodeCode
            binding.textViewEpisodeName.text = row.episode.name
            binding.textViewEpisodeDate.text = binding.root.context.getString(
                R.string.episode_air_date_format,
                row.numberInSeason,
                row.episode.airDate
            )
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EpisodeListRow>() {
        override fun areItemsTheSame(oldItem: EpisodeListRow, newItem: EpisodeListRow): Boolean =
            when {
                oldItem is EpisodeListRow.SeasonHeader && newItem is EpisodeListRow.SeasonHeader ->
                    oldItem.season == newItem.season
                oldItem is EpisodeListRow.Row && newItem is EpisodeListRow.Row ->
                    oldItem.episode.id == newItem.episode.id
                else -> false
            }

        override fun areContentsTheSame(oldItem: EpisodeListRow, newItem: EpisodeListRow): Boolean =
            oldItem == newItem
    }
}