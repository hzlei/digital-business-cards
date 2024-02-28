package cs446.dbc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cs446.dbc.databinding.DbcShareMenuBinding

class ShareMenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DbcShareMenuBinding.inflate(inflater, container, false)

        return binding.root
    }
}