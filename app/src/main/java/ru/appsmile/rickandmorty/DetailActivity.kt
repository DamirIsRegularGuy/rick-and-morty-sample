package ru.appsmile.rickandmorty.ui.detail

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.appsmile.rickandmorty.R
import ru.appsmile.rickandmorty.data.FavoritesRepository
import ru.appsmile.rickandmorty.databinding.ActivityDetailBinding
import ru.appsmile.rickandmorty.model.ResultItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import ru.appsmile.rickandmorty.ui.episodes.EpisodesActivity

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var currentCharacter: ResultItem? = null
    private var favoriteMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val character = intent.getSerializableExtra(EXTRA_CHARACTER) as? ResultItem
        if (character == null) {
            finish()
            return
        }

        currentCharacter = character
        bindCharacter(character)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        favoriteMenuItem = menu.findItem(R.id.action_favorite)
        updateFavoriteIcon()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                currentCharacter?.let {
                    FavoritesRepository.toggleFavorite(it)
                    updateFavoriteIcon()
                }
                true
            }
            R.id.action_share -> {
                shareCharacter()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateFavoriteIcon() {
        val character = currentCharacter ?: return
        val isFavorite = FavoritesRepository.isFavorite(character.id)
        favoriteMenuItem?.setIcon(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
    }

    private fun shareCharacter() {
        val character = currentCharacter ?: return
        val shareText = getString(
            R.string.share_character_format,
            character.name, character.status, character.species, character.location.name
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun bindCharacter(character: ResultItem) = with(binding) {
        supportActionBar?.title = character.name

        Glide.with(imageView)
            .load(character.image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_placeholder)
            .into(imageView)

        textViewName.text = character.name
        textViewStatusSpecies.text =
            getString(R.string.status_species_format, character.status, character.species)

        val statusColor = when (character.status) {
            "Dead" -> R.color.dead
            "Alive" -> R.color.live
            else -> R.color.unknown
        }
        textViewStatusSpecies.compoundDrawableTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this@DetailActivity, statusColor))

        textViewGenderValue.text = character.gender.ifBlank { getString(R.string.unknown_value) }
        textViewTypeValue.text = character.type.ifBlank { getString(R.string.unknown_value) }
        textViewOriginValue.text = character.origin.name
        textViewLocationValue.text = character.location.name
        textViewEpisodeCountValue.text = resources.getQuantityString(
            R.plurals.episode_count,
            character.episode.size,
            character.episode.size
        )
        textViewCreatedValue.text = formatCreatedDate(character.created)
        buttonEpisodes.setOnClickListener {
            startActivity(
                EpisodesActivity.newIntent(
                    this@DetailActivity,
                    character.name,
                    character.episode
                )
            )
        }
    }

    private fun formatCreatedDate(rawDate: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(rawDate)
            val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: rawDate
        } catch (e: Exception) {
            rawDate
        }
    }

    companion object {
        private const val EXTRA_CHARACTER = "extra_character"

        fun newIntent(context: Context, character: ResultItem): Intent {
            return Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_CHARACTER, character)
            }
        }
    }
}
