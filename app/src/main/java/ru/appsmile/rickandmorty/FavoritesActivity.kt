package ru.appsmile.rickandmorty.ui.favorites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.appsmile.rickandmorty.adapter.CharacterListItem
import ru.appsmile.rickandmorty.adapter.RickAndMortyAdapter
import ru.appsmile.rickandmorty.data.FavoritesRepository
import ru.appsmile.rickandmorty.databinding.ActivityFavoritesBinding
import ru.appsmile.rickandmorty.ui.detail.DetailActivity

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: RickAndMortyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = RickAndMortyAdapter(
            onItemClick = { character ->
                startActivity(DetailActivity.newIntent(this, character))
            },
            onFavoriteClick = { character ->
                FavoritesRepository.toggleFavorite(character)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        observeFavorites()
    }

    private fun observeFavorites() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                FavoritesRepository.favorites.collect { favorites ->
                    adapter.submitList(favorites.map { CharacterListItem(it, isFavorite = true) })
                    binding.recyclerView.isVisible = favorites.isNotEmpty()
                    binding.textViewEmpty.isVisible = favorites.isEmpty()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
