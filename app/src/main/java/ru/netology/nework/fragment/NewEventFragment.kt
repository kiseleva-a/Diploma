package ru.netology.nework.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.EventType
import ru.netology.nework.dto.MediaModel
import ru.netology.nework.fragment.secondary.MapFragment.Companion.editingArg
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.utils.load
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.UsersAndMapViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class NewEventFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: EventViewModel by activityViewModels()
    private val usersAndMapViewModel: UsersAndMapViewModel by activityViewModels()

    private val binding: FragmentNewEventBinding by viewBinding(createMethod = CreateMethod.INFLATE)

    //For choosing Audio file. There is an standard Image-Video picker but no Audio picker so it on its own
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                    viewModel.changeMedia(
                        uri,
                        context?.let { AndroidUtils.fileFromContentUri(it, uri) },
                        AttachmentType.AUDIO
                    )
                } else {
                    println("No media selected")
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind()
        binding.edit.requestFocus()

        subscribe()

        return binding.root
    }

    private fun bind() {
        if(usersAndMapViewModel.coords!=null){
            //we check if we have saved coordinates from map fragment
            //if we have - we save them to our edited/draft and clear
            viewModel.changeCoords(usersAndMapViewModel.coords!!)
            usersAndMapViewModel.coords = null
        }
        //edited is used as container for event info if we editing and as a draft saver for new event
        val event = viewModel.edited.value
        event?.speakerIds?.let { usersAndMapViewModel.getUserList(it) }
        binding.apply {
            if (event?.datetime?.isEmpty() == true) {
                val formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd")
                dateTimePicker.text = LocalDateTime.now().format(formatter)
            } else {
                val eventTime = OffsetDateTime.parse(event?.datetime).toLocalDateTime()
                val formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd")
                dateTimePicker.text = eventTime.format(formatter)
            }
            dateTimePicker.setOnClickListener {
                val dateDialog = DatePickerDialog(requireContext())
                dateDialog.setOnDateSetListener { _, y, m, d ->
                    val currentTime = LocalDateTime.now()
                    TimePickerDialog(requireContext(), object : TimePickerDialog.OnTimeSetListener {
                        override fun onTimeSet(p0: TimePicker?, h: Int, min: Int) {
                            viewModel.changeEventDateTime(setDate(y, m, d, h, min, dateTimePicker))
                        }
                    }, currentTime.hour, currentTime.minute, true).show()
                }
                dateDialog.show()
            }

            bindEventTypeButton(eventType, event?.type == EventType.ONLINE)
            eventType.setOnClickListener {
                viewModel.changeEventType()
                bindEventTypeButton(eventType, viewModel.edited.value?.type == EventType.ONLINE)
            }

            edit.setText(event?.content)
            link.setText(event?.link)
            if (event?.coords != null) {
                coordinatesButton.text = "${event.coords.lat.take(9)} | ${event.coords.long.take(9)}"
                deleteCoordsButton.isVisible = true
                deleteCoordsButton.setOnClickListener {
                    deleteCoordsButton.isVisible = false
                    viewModel.changeCoords(null)
                    coordinatesButton.text = getText(R.string.coordinates_hint)
                }
            }
            if (event?.id != 0) { //If event ID is not 0 - we editing so there can be some attachment
                if (event?.attachment != null) {
                    when (event.attachment.type) {
                        AttachmentType.IMAGE -> photo.load(event.attachment.url)
                        AttachmentType.VIDEO -> photo.setImageResource(R.drawable.baseline_video_48)
                        AttachmentType.AUDIO -> photo.setImageResource(R.drawable.baseline_audio_file_48)
                        else -> {}
                    }
                    viewModel.changeMedia(null, null, event.attachment.type, event.attachment.url)
                    photoContainer.isVisible = true
                } else {
                    viewModel.deleteMedia()
                    photoContainer.isVisible = false
                }
            } else { //If event ID is 0 - it's a new event but some attachment can be as a draft
                viewModel.attachment.value?.let { showAttachment(it) }
            }
        }
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
        val pickVideoContract =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    viewModel.changeMedia(
                        uri,
                        context?.let { AndroidUtils.fileFromContentUri(it, uri) },
                        AttachmentType.VIDEO
                    )
                } else {
                    println("No media selected")
                }
            }

        binding.apply {
            sendButton.setOnClickListener {
                val text = binding.edit.text.toString()
                val link = binding.link.text.toString()
                if (text.isNotBlank()) {
                    viewModel.changeContent(
                        text,
                        link
                    )
                    viewModel.save()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.no_context),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                AndroidUtils.hideKeyboard(requireView())
            }

            coordinatesButton.setOnClickListener {
                usersAndMapViewModel.coords = viewModel.edited.value?.coords
                findNavController().navigate(R.id.action_global_mapFragment,
                    Bundle().apply
                    {
                        editingArg = true
                    })
            }

            pickPhoto.setOnClickListener {
                pickImageContract.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            pickVideo.setOnClickListener {
                pickVideoContract.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            }
            pickAudio.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "audio/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                resultLauncher.launch(intent)
            }
            removeAttachment.setOnClickListener {
                viewModel.deleteMedia()
            }

            addSpeaker.setOnClickListener {
                findNavController().navigate(R.id.action_newEventFragment_to_usersFragment)
            }
        }

        viewModel.attachment.observe(viewLifecycleOwner) {
            showAttachment(it)
        }

        usersAndMapViewModel.userIdList.observe(viewLifecycleOwner) {
            binding.addSpeaker.text = if (it.isEmpty()) "" else it.size.toString()
            viewModel.changeSpeakerList(it)
        }

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.logOut -> {
                        context?.let { showLogOutDialog(it) }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            //ID = 0 if new event (even with draft), non-zero will be edited events
            if (viewModel.edited.value?.id == 0) {
                val text = binding.edit.text.toString()
                val link = binding.link.text.toString()
                viewModel.changeContent(
                    text,
                    link,
                )
            } else {
                viewModel.empty()
                viewModel.deleteMedia()
            }

            findNavController().navigateUp()
        }
    }

    private fun setDate(y: Int, m: Int, d: Int, h: Int, min: Int, button: MaterialButton): String {
        //"yyyy-mm-ddThh:mn:11.996Z" making this type of string
        val yString = y.toString().padStart(4, '0')
        val mString = (m + 1).toString().padStart(2, '0')
        val dString = d.toString().padStart(2, '0')
        val hString = h.toString().padStart(2, '0')
        val minString = min.toString().padStart(2, '0')
        val date = "$hString:$minString $dString-$mString-$yString"
        button.text = date
        return "$yString-$mString-${dString}T$hString:$minString:01.000Z"
    }

    private fun bindEventTypeButton(button: MaterialButton, state: Boolean) {
        button.isChecked = state
        button.text = if (state) getString(R.string.online) else getString(R.string.offline)
    }

    private fun showLogOutDialog(context: Context) {
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(context)
            builder.apply {
                setTitle(R.string.logging_out)
                setMessage(getString(R.string.dialog_log_out))
                setPositiveButton(
                    getString(R.string.log_out)
                ) { _, _ ->
                    appAuth.removeAuth()
                    findNavController().navigateUp()
                }
                setNegativeButton(
                    getString(R.string.back)
                ) { _, _ ->
                }
            }
            builder.create()
        }
        alertDialog.show()
    }

    private fun showAttachment(mediaModel: MediaModel) {
        binding.apply {
            when (mediaModel.attachmentType) {
                AttachmentType.IMAGE -> {
                    if (mediaModel.url == null) { //if we have url - then the image is already loaded
                        photo.setImageURI(mediaModel.uri)
                    }
                }
                AttachmentType.VIDEO -> photo.setImageResource(R.drawable.baseline_video_48)
                AttachmentType.AUDIO -> photo.setImageResource(R.drawable.baseline_audio_file_48)
                else -> {}
            }
            photoContainer.isVisible = mediaModel.attachmentType != AttachmentType.NONE
        }
    }

}