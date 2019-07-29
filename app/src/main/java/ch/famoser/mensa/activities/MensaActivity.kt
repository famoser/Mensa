package ch.famoser.mensa.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import ch.famoser.mensa.R
import ch.famoser.mensa.fragments.MensaDetailFragment
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.repositories.LocationRepository
import kotlinx.android.synthetic.main.activity_mensa.*
import java.io.FileNotFoundException
import java.util.*
import android.net.Uri


/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [MainActivity].
 */
class MensaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensa)
        setSupportActionBar(toolbar)


        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mensa = loadMensaFromIntent() ?: return
        initializeContent(mensa, savedInstanceState)

        details_action.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, mensa.url)
            startActivity(browserIntent)
        }
    }

    private fun initializeContent(mensa: Mensa, savedInstanceState: Bundle?) {
        if (mensa.imagePath != null) {
            loadMensaImage(mensa.imagePath)
        }

        // load fragment if if first time (e.g. savedInstanceState == null
        if (savedInstanceState == null) {
            loadMensaDetailFragment(mensa.id)
        }
    }

    private fun loadMensaImage(imagePath: String) {
        try {
            val mensaImageFile = assets.open(imagePath)
            val mensaImage = Drawable.createFromStream(mensaImageFile, null);
            image.setImageDrawable(mensaImage);
        } catch (exception: FileNotFoundException) {
            // no image is OK
        }
    }

    private fun loadMensaFromIntent(): Mensa? {
        val mensaId = UUID.fromString(intent.getStringExtra(MensaDetailFragment.MENSA_ID));

        val locationRepository = LocationRepository.getInstance(this);
        val mensa = locationRepository.getMensa(mensaId);

        return mensa
    }

    private fun loadMensaDetailFragment(mensaId: UUID) {
        val fragment = MensaDetailFragment().apply {
            arguments = Bundle().apply {
                putString(
                    MensaDetailFragment.MENSA_ID,
                    mensaId.toString()
                )
            }
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.item_detail_container, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                navigateUpTo(Intent(this, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
