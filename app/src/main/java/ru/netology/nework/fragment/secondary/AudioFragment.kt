package ru.netology.nework.fragment.secondary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentAudioBinding
import ru.netology.nework.utils.MediaLifecycleObserver
import ru.netology.nework.utils.StringArg


class AudioFragment : Fragment() {
    private val mediaObserver = MediaLifecycleObserver()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAudioBinding.inflate(inflater, container, false)
        val player = mediaObserver.mediaPlayer

        player?.apply {
            arguments?.urlArg?.let { setDataSource(it) }
            setOnPreparedListener {
                binding.infoIcon.setImageResource(R.drawable.ic_baseline_play_circle_32)

                binding.infoIcon.setOnClickListener {
                    if (player.isPlaying) {
                        binding.infoIcon.setImageResource(R.drawable.ic_baseline_play_circle_32)
                        pause()
                    } else {
                        binding.infoIcon.setImageResource(R.drawable.baseline_pause_32)
                        start()
                    }
                }

            }
            setOnCompletionListener {
                binding.infoIcon.setImageResource(R.drawable.ic_baseline_play_circle_32)
            }
            prepareAsync()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            player?.stop()
            player?.reset()
            findNavController().navigateUp()
        }

        return binding.root
    }

    companion object {
        var Bundle.urlArg by StringArg
    }
}