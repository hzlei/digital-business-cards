package cs446.dbc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cs446.dbc.databinding.DbcCardWalletBinding

class CardWalletFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DbcCardWalletBinding.inflate(inflater, container, false)

        return binding.root
    }
}