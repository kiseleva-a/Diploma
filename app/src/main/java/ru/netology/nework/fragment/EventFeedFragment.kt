package ru.netology.nework.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.EventAdapter
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.FeedModelState
import ru.netology.nework.fragment.JobFragment.Companion.myJobsArg
import ru.netology.nework.fragment.UserWallFragment.Companion.userIdArg
import ru.netology.nework.fragment.UserWallFragment.Companion.userJobArg
import ru.netology.nework.fragment.secondary.MapFragment.Companion.editingArg
import ru.netology.nework.fragment.secondary.PictureFragment.Companion.urlArg
import ru.netology.nework.utils.listeners.EventInteractionListener
import ru.netology.nework.utils.listeners.MapInteractionListener
import ru.netology.nework.utils.listeners.MediaInteractionListener
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.UsersAndMapViewModel

@AndroidEntryPoint
open class EventFeedFragment : Fragment() {
    private val viewModel: EventViewModel by activityViewModels()
    protected val usersAndMapViewModel: UsersAndMapViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val binding: FragmentEventsBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var postData: Flow<PagingData<Event>>

    private val onInteractionListener = object : EventInteractionListener {
        override fun onLike(event: Event) {
            val token = authViewModel.state.value?.token
            if (token == null) {
                context?.let { showSignInDialog(it) }
            } else {
                viewModel.likeById(event.id, event.likedByMe)
            }
        }

        override fun onEdit(event: Event) {
            viewModel.edit(event)
            usersAndMapViewModel.coords = null
            findNavController().navigate(R.id.action_eventFeedFragment_to_newEventFragment)
        }

        override fun onRemove(event: Event) {
            viewModel.removeById(event.id)
        }

        override fun onAvatarClick(event: Event) {
            //viewModel.changeUserId(event.authorId)
            findNavController().navigate(R.id.action_global_userWallFragment,
                Bundle().apply
                {
                    userIdArg = event.authorId.toString()
                    userJobArg = event.authorJob
                })

        }

        override fun onParticipate(event: Event) {
            val token = authViewModel.state.value?.token
            if (token == null) {
                context?.let { showSignInDialog(it) }
            } else {
                viewModel.participateById(event.id, event.participatedByMe)
            }
        }
    }

    private val mediaInteractionListener = object : MediaInteractionListener {
        override fun onAudioClick(url: String) {
            findNavController().navigate(R.id.action_global_audioFragment,
                Bundle().apply
                { urlArg = url })
        }

        override fun onVideoClick(url: String) {
            findNavController().navigate(R.id.action_global_playFragment,
                Bundle().apply
                { urlArg = url })
        }

        override fun onPictureClick(url: String) {
            findNavController().navigate(R.id.action_global_pictureFragment,
                Bundle().apply
                { urlArg = url })
        }
    }

    protected val mapInteractionListener = object : MapInteractionListener {
        override fun onCoordsClick(coords: Coords) {
            usersAndMapViewModel.coords = coords
            findNavController().navigate(R.id.action_global_mapFragment,
                Bundle().apply
                {
                    editingArg = false
                })
        }
    }

    private val adapter =
        EventAdapter(onInteractionListener, mediaInteractionListener, mapInteractionListener)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.listEvents.adapter = adapter
        postData = viewModel.data

        subscribe()

        return binding.root
    }


    private fun subscribe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                postData.collectLatest {
                    adapter.submitData(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                adapter.loadStateFlow.collectLatest {
                    binding.swiper.isRefreshing =
                        it.refresh is LoadState.Loading
                }
            }
        }

        authViewModel.state.observe(viewLifecycleOwner) {
            viewModel.load()
            if (it?.id != -1) {
                adapter.refresh()
            }
        }

        binding.apply {
            swiper.setOnRefreshListener {
                adapter.refresh()
            }

            feedButton.setOnClickListener {
                findNavController().navigate(R.id.action_global_postFeedFragment)
            }

            myWallButton.setOnClickListener {
                val token = authViewModel.state.value?.token
                if (token == null) {
                    context?.let { context -> showSignInDialog(context) }
                } else {
                    val id = authViewModel.state.value!!.id
                    findNavController().navigate(R.id.action_global_userWallFragment,
                        Bundle().apply
                        {
                            userIdArg = id.toString()
                            userJobArg = ""
                        })
                }
            }

            jobsButton.setOnClickListener {
                val token = authViewModel.state.value?.token
                if (token == null) {
                    context?.let { context -> showSignInDialog(context) }
                } else {
                    findNavController().navigate(R.id.action_global_jobFragment,
                        Bundle().apply { myJobsArg = true })
                }
            }

            addEventButton.setOnClickListener {
                val token = authViewModel.state.value?.token
                if (token == null) {
                    context?.let { context -> showSignInDialog(context) }
                } else {
                    usersAndMapViewModel.coords = null
                    findNavController().navigate(R.id.action_eventFeedFragment_to_newEventFragment)
                }
            }
        }

        viewModel.apply {
            loadUsers()

            dataState.observe(viewLifecycleOwner) {
                when (it) {
                    FeedModelState.Error -> {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.load_feed_error),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("Retry") {
                                load()
                            }
                            .show()
                    }
                    else -> {}
                }
                binding.apply {
                    loading.isVisible = it == FeedModelState.Loading
                    buttonPanel.isVisible = it != FeedModelState.Loading
                    addEventButton.isVisible = it != FeedModelState.Loading
                }
            }

            eventCreatedError.observe(viewLifecycleOwner) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.specific_posting_error, it),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Retry post") {
                        viewModel.load()
                    }
                    .show()
            }

            eventsRemoveError.observe(viewLifecycleOwner) {
                val id = it.second
                Snackbar.make(
                    binding.root,
                    getString(R.string.specific_edit_error, it.first),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Retry") {
                        viewModel.removeById(id)
                    }
                    .show()
            }

            eventsLikeError.observe(viewLifecycleOwner) {
                val id = it.first
                val willLike = it.second
                Snackbar.make(
                    binding.root,
                    getString(R.string.like_error),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Retry") {
                        viewModel.likeById(id, willLike)
                    }
                    .show()
            }

            eventsParticipateError.observe(viewLifecycleOwner) {
                val id = it.first
                val willLike = it.second
                Snackbar.make(
                    binding.root,
                    getString(R.string.participation_error),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Retry") {
                        viewModel.participateById(id, willLike)
                    }
                    .show()
            }

            usersLoadError.observe(viewLifecycleOwner) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.load_users_error, it),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Retry") {
                        loadUsers()
                    }
                    .show()
            }
        }
    }

    private fun showSignInDialog(context: Context) {
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(context)
            builder.apply {
                setTitle(R.string.authorization_required)
                setMessage(getString(R.string.dialog_sign_in))
                setPositiveButton(
                    getString(R.string.sign_in)
                ) { _, _ ->
                    findNavController().navigate(R.id.action_global_fragment_sing_in)
                }
                setNeutralButton(
                    getString(R.string.back)
                ) { _, _ ->
                }
                setNegativeButton(
                    getString(R.string.sign_up)
                ) { _, _ ->
                    findNavController().navigate(R.id.action_global_signUpFragment)
                }
            }
            builder.create()
        }
        alertDialog.show()
    }
}