package ru.netology.nework.fragment.secondary

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentVideoBinding
import ru.netology.nework.utils.MediaLifecycleObserver
import ru.netology.nework.utils.StringArg


class VideoFragment : Fragment() {
    private val mediaObserver = MediaLifecycleObserver()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentVideoBinding.inflate(inflater, container, false)
        val player = mediaObserver.mediaPlayer
        val videoPlayer = binding.video

        arguments?.urlArg?.let { playerPlayVideo(videoPlayer, it, binding.infoIcon) }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            player?.stop()
            player?.reset()
            videoPlayer.stopPlayback()
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun playerPlayVideo(video: VideoView, url: String, infoIcon: ImageView) {

        video.apply {
            setVideoURI(Uri.parse(url))
            setOnPreparedListener {
                infoIcon.setImageResource(R.drawable.ic_baseline_play_circle_32)

                //A little hack to show a first frame of a video instead of a black nothing
                start()
                pause()
            }
            setOnCompletionListener {
                infoIcon.isVisible = true
                resume()
            }

            setOnClickListener {
                if (isPlaying) {
                    pause()
                } else {
                    start()
                }
                infoIcon.isVisible = !isPlaying
            }
        }
    }

    fun playerPlaySound(player: MediaPlayer?, url: String) {
        player?.apply {
            setDataSource(url)

            setOnPreparedListener {
                it.start()
            }

            setOnCompletionListener {
                player.stop()
            }
            prepareAsync()
        }
    }

    companion object {
        var Bundle.urlArg by StringArg
    }
}