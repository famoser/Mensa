package com.example.mensa.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mensa.R
import com.example.mensa.data.Mensa
import com.example.mensa.dummy.DummyContent
import kotlinx.android.synthetic.main.activity_mensa.*
import kotlinx.android.synthetic.main.fragment_mensa_detail.view.*
import java.util.*

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class MensaDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: Mensa? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(MENSA_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = DummyContent.MENSA_MAP[UUID.fromString(it.getString(MENSA_ID))]
                activity?.toolbar_layout?.title = item?.title
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_mensa_detail, container, false)

        item?.let {
            rootView.title.text = it.title
        }

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
