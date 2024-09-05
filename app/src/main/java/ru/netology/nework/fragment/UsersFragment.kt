package ru.netology.nework.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nework.adapter.UsersAdapter
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.dto.User
import ru.netology.nework.utils.listeners.UserListInteractionListener
import ru.netology.nework.viewmodel.UsersAndMapViewModel

class UsersFragment : Fragment() {
    private val viewModel: UsersAndMapViewModel by activityViewModels()

    private val userListInteractionListener = object : UserListInteractionListener {
        override fun onClick(user: User) {
            if (viewModel.userIdList.value?.contains(user.id) == true)
                viewModel.removeUser(user.id)
            else
                viewModel.addUser(user.id)
            viewModel.changeCheckedUsers(user.id)
        }
    }

    private val adapter = UsersAdapter(userListInteractionListener)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //viewModel.saveOldUsers()
        val binding = FragmentUsersBinding.inflate(inflater, container, false)
        //For users already in list we check them
        viewModel.userIdList.value?.let { viewModel.checkCheckedUser(it) }
        binding.usersList.adapter = adapter

        binding.addUsersButton.setOnClickListener {
            findNavController().navigateUp()
        }

        /*requireActivity().onBackPressedDispatcher.addCallback(this) {
            //viewModel.getBackOldUsers()
            findNavController().navigateUp()
        }*/

        viewModel.usersData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return binding.root
    }
}
