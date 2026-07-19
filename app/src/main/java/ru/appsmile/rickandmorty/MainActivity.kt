package ru.appsmile.rickandmorty.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.appsmile.rickandmorty.R
import ru.appsmile.rickandmorty.adapter.CharacterListItem
import ru.appsmile.rickandmorty.adapter.RickAndMortyAdapter
import ru.appsmile.rickandmorty.databinding.ActivityMainBinding
import ru.appsmile.rickandmorty.ui.detail.DetailActivity
import ru.appsmile.rickandmorty.ui.favorites.FavoritesActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: RickAndMortyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupSwipeRefresh()
        setupStatusFilter()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter = RickAndMortyAdapter(
            onItemClick = { character ->
                startActivity(DetailActivity.newIntent(this, character))
            },
            onFavoriteClick = { character ->
                viewModel.toggleFavorite(character)
            }
        )

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return

                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                if (lastVisible >= totalItemCount - 4) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupStatusFilter() = with(binding) {
        chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when (checkedIds.firstOrNull()) {
                chipAlive.id -> "alive"
                chipDead.id -> "dead"
                chipUnknown.id -> "unknown"
                else -> null
            }
            viewModel.onStatusFilterChanged(status)
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val listItems = state.items.map { character ->
                        CharacterListItem(character, isFavorite = character.id in state.favoriteIds)
                    }
                    adapter.submitList(listItems)

                    binding.swipeRefresh.isRefreshing = false
                    binding.progressBar.isVisible = state.isLoading
                    binding.recyclerView.isVisible = state.items.isNotEmpty()

                    val showFullScreenError = state.errorMessage != null && state.items.isEmpty()
                    binding.textViewError.isVisible = showFullScreenError
                    binding.textViewError.text = state.errorMessage

                    if (state.errorMessage != null && state.items.isNotEmpty()) {
                        Toast.makeText(this@MainActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.onSearchQueryChanged(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onSearchQueryChanged(newText.orEmpty())
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_favorites) {
            startActivity(Intent(this, FavoritesActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
