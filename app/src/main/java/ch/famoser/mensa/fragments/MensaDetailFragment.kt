package ch.famoser.mensa.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.famoser.mensa.R
import ch.famoser.mensa.adapters.MenuDetailsAdapter
import ch.famoser.mensa.models.Mensa
import ch.famoser.mensa.repositories.LocationRepository
import kotlinx.android.synthetic.main.activity_mensa.*
import kotlinx.android.synthetic.main.fragment_mensa_detail.view.*
import java.util.*

class MensaDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: Mensa? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(MENSA_ID)) {
                val mensaId = UUID.fromString(it.getString(MENSA_ID))

                val locationRepository = LocationRepository.getInstance(context!!)
                item = locationRepository.getMensa(mensaId)

                activity?.toolbar_layout?.title = item?.title
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_mensa_detail, container, false)

        val mensa = item ?: return rootView
        val activity = activity ?: return rootView

        val menuDetailsAdapter = MenuDetailsAdapter(mensa.menus, activity)
        rootView.menu_details_list.adapter = menuDetailsAdapter

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val MENSA_ID = "item_id"
    }
}
