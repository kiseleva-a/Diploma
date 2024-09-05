package ru.netology.nework.fragment.secondary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentSignInBinding
import ru.netology.nework.viewmodel.SignInViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val binding: FragmentSignInBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private val viewModel: SignInViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        subscribe()

        return binding.root
    }

    private fun subscribe() {
        binding.apply {
            signUpButton.setOnClickListener {
                findNavController().navigate(R.id.action_fragment_sing_in_to_signUpFragment)
            }

            signInButton.setOnClickListener {
                if (binding.loginInput.text.isNullOrEmpty() || binding.passwordInput.text.isNullOrEmpty()) {
                    Toast.makeText(context, "Both fields need to be filled!", Toast.LENGTH_LONG)
                        .show()
                } else {
                    viewModel.signIn(
                        binding.loginInput.text.toString(),
                        binding.passwordInput.text.toString()
                    )
                }
            }
        }

        viewModel.apply {
            signInRight.observe(viewLifecycleOwner) {
                appAuth.setAuth(it.id, it.token)
                goBack()
            }

            signInError.observe(viewLifecycleOwner) {
                Toast.makeText(context, getString(R.string.login_error, it), Toast.LENGTH_LONG)
                    .show()
            }

            signInWrong.observe(viewLifecycleOwner) {
                Toast.makeText(context, getString(R.string.login_wrong), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun goBack() {
        findNavController().navigateUp()
    }

}