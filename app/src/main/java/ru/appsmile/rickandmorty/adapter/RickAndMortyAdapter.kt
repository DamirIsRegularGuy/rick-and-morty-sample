package ru.appsmile.rickandmorty.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.appsmile.rickandmorty.R
import ru.appsmile.rickandmorty.databinding.ItemRickAndMortyBinding
import ru.appsmile.rickandmorty.model.ResultItem

class RickAndMortyAdapter(
    private val onItemClick: (ResultItem) -> Unit,
    private val onFavoriteClick: (ResultItem) -> Unit
) : ListAdapter<CharacterListItem, RickAndMortyAdapter.ItemViewHolder>(DiffCallback()) {

    class ItemViewHolder(val binding: ItemRickAndMortyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemRickAndMortyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val (currentItem, isFavorite) = getItem(position)
        with(holder.binding) {
            textViewName.text = currentItem.name

            Glide.with(root)
                .load(currentItem.image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .into(imageView)

            textViewAliveStatus.text =
                root.context.getString(R.string.status_species_format, currentItem.status, currentItem.species)

            val statusColor = when (currentItem.status) {
                "Dead" -> R.color.dead
                "Alive" -> R.color.live
                else -> R.color.unknown
            }

            textViewAliveStatus.compoundDrawableTintList =
                ColorStateList.valueOf(ContextCompat.getColor(root.context, statusColor))

            // origin = место, откуда персонаж родом ("впервые увиден"),
            // location = последнее известное местоположение. В исходном коде они были перепутаны.
            textViewFirstKnownLocation.text = currentItem.origin.name
            textViewLastKnownLocation.text = currentItem.location.name

            imageViewFavorite.setImageResource(
                if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
            )
            imageViewFavorite.setColorFilter(
                ContextCompat.getColor(root.context, if (isFavorite) R.color.favorite else R.color.white)
            )
            imageViewFavorite.setOnClickListener { onFavoriteClick(currentItem) }

            root.setOnClickListener { onItemClick(currentItem) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CharacterListItem>() {
        override fun areItemsTheSame(oldItem: CharacterListItem, newItem: CharacterListItem): Boolean =
            oldItem.character.id == newItem.character.id

        override fun areContentsTheSame(oldItem: CharacterListItem, newItem: CharacterListItem): Boolean =
            oldItem == newItem
    }
}
