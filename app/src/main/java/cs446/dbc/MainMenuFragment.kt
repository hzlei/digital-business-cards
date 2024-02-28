package cs446.dbc
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cs446.dbc.databinding.DbcMainMenuBinding

class MainMenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DbcMainMenuBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.viewCardButton.setOnClickListener {
            (activity as NavigationHost).navigateTo(CardWalletFragment(), true)
        }

        binding.shareCardButton.setOnClickListener {
            (activity as NavigationHost).navigateTo(ShareMenuFragment(), true)
        }


        return view
    }
}