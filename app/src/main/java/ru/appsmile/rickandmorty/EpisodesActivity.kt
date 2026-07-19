package ru.appsmile.rickandmorty.ui.episodes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.appsmile.rickandmorty.R
import ru.appsmile.rickandmorty.databinding.ActivityEpisodesBinding

class EpisodesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEpisodesBinding
    private lateinit var adapter: EpisodesAdapter

    private val viewModel: EpisodesViewModel by viewModels {
        EpisodesViewModel.Factory(
            intent.getStringArrayListExtra(EXTRA_EPISODE_URLS)?.toList() ?: emptyList()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(
            R.string.episodes_title_format,
            intent.getStringExtra(EXTRA_CHARACTER_NAME).orEmpty()
        )

        adapter = EpisodesAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        observeUiState()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.rows)

                    binding.progressBar.isVisible = state.isLoading
                    binding.recyclerView.isVisible = state.rows.isNotEmpty()

                    val showError = state.errorMessage != null && state.rows.isEmpty()
                    binding.textViewError.isVisible = showError
                    binding.textViewError.text = state.errorMessage
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        private const val EXTRA_CHARACTER_NAME = "extra_character_name"
        private const val EXTRA_EPISODE_URLS = "extra_episode_urls"

        fun newIntent(context: Context, characterName: String, episodeUrls: List<String>): Intent {
            return Intent(context, EpisodesActivity::class.java).apply {
                putExtra(EXTRA_CHARACTER_NAME, characterName)
                putStringArrayListExtra(EXTRA_EPISODE_URLS, ArrayList(episodeUrls))
            }
        }
    }
}