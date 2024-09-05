package ru.netology.nework.fragment.secondary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentSignUpBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.viewmodel.SignUpViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val binding: FragmentSignUpBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private val viewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        subscribe()

        return binding.root
    }

    private fun subscribe() {
        val pickImageContract =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    viewModel.changeMedia(
                        uri,
                        context?.let { AndroidUtils.fileFromContentUri(it, uri) },
                        AttachmentType.IMAGE
                    )
                } else {
                    println("No media selected")
                }
            }

        binding.apply {
            signUpButton.setOnClickListener {
                if (binding.loginInput.text.isNullOrEmpty()
                    || binding.passwordInput.text.isNullOrEmpty()
                    || binding.repeatPasswordInput.text.isNullOrEmpty()
                    || binding.usernameInput.text.isNullOrEmpty()
                ) {
                    Toast.makeText(context, "All fields need to be filled!", Toast.LENGTH_LONG)
                        .show()
                } else {
                    if (binding.passwordInput.text.toString() != binding.repeatPasswordInput.text.toString()) {
                        Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        viewModel.signUp(
                            binding.loginInput.text.toString(),
                            binding.passwordInput.text.toString(),
                            binding.usernameInput.text.toString()
                        )
                    }
                }
            }
            chooseAvatarButton.setOnClickListener {
                pickImageContract.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            removePicture.setOnClickListener {
                viewModel.deleteMedia()
            }
        }

        viewModel.apply {
            signUpRight.observe(viewLifecycleOwner) {
                appAuth.setAuth(it.id, it.token)
                goBack()
            }

            signUpError.observe(viewLifecycleOwner) {
                Toast.makeText(context, getString(R.string.login_error, it), Toast.LENGTH_LONG)
                    .show()
            }

            attachment.observe(viewLifecycleOwner) {
                binding.chooseAvatarButton.apply {
                    it.uri?.let {
                        Glide.with(this)
                            .load(it)
                            .circleCrop()
                            .error(R.drawable.ic_baseline_error_outline_48)
                            .placeholder(R.drawable.ic_baseline_downloading_48)
                            .into(this)
                    }
                        ?: setImageResource(R.drawable.baseline_avatar_circle_filled_24)
                }

                binding.removePicture.isVisible = it.uri != null
            }
        }
    }

    private fun goBack() {
        findNavController().navigateUp()
    }


}