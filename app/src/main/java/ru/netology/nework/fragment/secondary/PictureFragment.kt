package ru.netology.nework.fragment.secondary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentPictureBinding
import ru.netology.nework.utils.StringArg

class PictureFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding= FragmentPictureBinding.inflate(inflater,container,false)

        arguments?.urlArg?.let { binding.photo.load(it) }

        return binding.root
    }

    private fun ImageView.load(url: String, timeout: Int = 10_000) {
        Glide.with(this)
            .load(url)
            .error(R.drawable.ic_baseline_error_outline_48)
            .placeholder(R.drawable.ic_baseline_downloading_48)
            .timeout(timeout)
            .into(this)
    }

    companion object {
        var Bundle.urlArg by StringArg
    }
}