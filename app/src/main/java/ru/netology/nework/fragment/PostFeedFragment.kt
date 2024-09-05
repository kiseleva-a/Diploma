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
import ru.netology.nework.adapter.PostsAdapter
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.FeedModelState
import ru.netology.nework.dto.Post
import ru.netology.nework.fragment.JobFragment.Companion.myJobsArg
import ru.netology.nework.fragment.UserWallFragment.Companion.userIdArg
import ru.netology.nework.fragment.UserWallFragment.Companion.userJobArg
import ru.netology.nework.fragment.secondary.MapFragment.Companion.editingArg
import ru.netology.nework.fragment.secondary.PictureFragment.Companion.urlArg
import ru.netology.nework.utils.listeners.MapInteractionListener
import ru.netology.nework.utils.listeners.MediaInteractionListener
import ru.netology.nework.utils.listeners.PostInteractionListener
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.UserWallViewModel
import ru.netology.nework.viewmodel.UsersAndMapViewModel

@AndroidEntryPoint
open class PostFeedFragment : Fragment() {
    protected open val viewModel: PostViewModel by activityViewModels()
    protected val usersAndMapViewModel: UsersAndMapViewModel by activityViewModels()
    protected val authViewModel: AuthViewModel by activityViewModels()
    protected val binding: FragmentPostsBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    protected lateinit var postData: Flow<PagingData<Post>>

    protected val onInteractionListener = object : PostInteractionListener {
        override fun onLike(post: Post) {
            val token = authViewModel.state.value?.token
            if (token == null) {
                context?.let { showSignInDialog(it) }
            } else {
                viewModel.likeById(post.id, post.likedByMe)
            }
        }

        override fun onEdit(post: Post) {
            viewModel.edit(post)
            usersAndMapViewModel.coords = null
            findNavController().navigate(R.id.action_global_newPostFragment)
        }

        override fun onRemove(post: Post) {
            viewModel.removeById(post.id)
        }

        override fun onAvatarClick(post: Post) {
            if (viewModel is UserWallViewModel) {
                //in user wall avatar click does nothing
            } else {
                findNavController().navigate(R.id.action_postFeedFragment_to_userWallFragment,
                    Bundle().apply
                    {
                        userIdArg = post.authorId.toString()
                        userJobArg = post.authorJob
                    })
            }
        }
    }

    protected val mediaInteractionListener = object : MediaInteractionListener {
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

    protected val adapter =
        PostsAdapter(onInteractionListener, mediaInteractionListener, mapInteractionListener)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.listPosts.adapter = adapter
        postData = viewModel.data

        subscribe()
        subscribeForFeedWall()

        return binding.root
    }


    protected fun subscribe() {
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

            eventWallButton.setOnClickListener {
                findNavController().navigate(R.id.action_global_eventFeedFragment)
            }

            jobsButton.setOnClickListener {
                val token = authViewModel.state.value?.token
                if (token == null) {
                    context?.let { context -> showSignInDialog(context) }
                } else {
                    findNavController().navigate(
                        R.id.action_global_jobFragment,
                        Bundle().apply { myJobsArg = true })
                }
            }

            addPostButton.setOnClickListener {
                val token = authViewModel.state.value?.token
                if (token == null) {
                    context?.let { context -> showSignInDialog(context) }
                } else {
                    usersAndMapViewModel.coords = null
                    findNavController().navigate(R.id.action_global_newPostFragment)
                }
            }
        }

        viewModel.apply {
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
                checkLoading()
            }

            postCreatedError.observe(viewLifecycleOwner) {
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

            postsRemoveError.observe(viewLifecycleOwner) {
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

            postsLikeError.observe(viewLifecycleOwner) {
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
        }

        usersAndMapViewModel.apply {
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
            dataState.observe(viewLifecycleOwner) {
                checkLoading()
            }
        }
    }

    private fun subscribeForFeedWall() {
        usersAndMapViewModel.loadUsers()

        binding.apply {
            myWallButton.setOnClickListener {
                val token = authViewModel.state.value?.token
                if (token == null) {
                    context?.let { context -> showSignInDialog(context) }
                } else {
                    val id = authViewModel.state.value!!.id
                    //viewModel.changeUserId(id)
                    findNavController().navigate(R.id.action_postFeedFragment_to_userWallFragment,
                        Bundle().apply
                        {
                            userIdArg = id.toString()
                            userJobArg = ""
                        })
                }
            }
        }
    }

    protected fun checkLoading() {
        val postsLoading = viewModel.dataState.value == FeedModelState.Loading
        val usersLoading = usersAndMapViewModel.dataState.value == FeedModelState.Loading
        binding.apply {
            loading.isVisible = postsLoading || usersLoading
            buttonPanel.isVisible = !postsLoading && !usersLoading
            addPostButton.isVisible = !postsLoading && !usersLoading
        }
    }

    protected fun showSignInDialog(context: Context) {
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