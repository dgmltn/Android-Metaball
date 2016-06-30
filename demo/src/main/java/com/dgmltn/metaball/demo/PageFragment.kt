package com.dgmltn.metaball.demo

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class PageFragment : Fragment() {

    private var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        page = arguments.getInt(ARG_PAGE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_page, container, false) as TextView
        view.text = "Fragment #$page"
        return view
    }

    companion object {
        val ARG_PAGE = "ARG_PAGE"

        fun newInstance(page: Int): PageFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = PageFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
