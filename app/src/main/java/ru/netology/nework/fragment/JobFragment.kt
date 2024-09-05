package ru.netology.nework.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import ru.netology.nework.R
import ru.netology.nework.adapter.JobAdapter
import ru.netology.nework.databinding.FragmentJobsBinding
import ru.netology.nework.dto.FeedModelState
import ru.netology.nework.dto.Job
import ru.netology.nework.utils.AndroidUtils.getJobDate
import ru.netology.nework.utils.BooleanArg
import ru.netology.nework.utils.StringArg
import ru.netology.nework.utils.listeners.JobInteractionListener
import ru.netology.nework.viewmodel.JobViewModel

class JobFragment : Fragment() {
    private val viewModel: JobViewModel by activityViewModels()
    var isEditing = false //For saving draft for new job

    private val onInteractionListener = object : JobInteractionListener {
        override fun onEdit(job: Job) {
            viewModel.edit(job) //Instead of a draft for new post we show what was in job already
            binding.apply {
                bindAddingJobDialog()
                addJobContainer.isVisible = true
                addJobButton.isVisible = false
            }
            isEditing = true
        }

        override fun onRemove(job: Job) {
            viewModel.removeById(job.id)
        }
    }

    private val adapter = JobAdapter(onInteractionListener)
    private val binding: FragmentJobsBinding by viewBinding(createMethod = CreateMethod.INFLATE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val userId = arguments?.idArg

        viewModel.load(userId)

        subscribe(userId)

        return binding.root
    }

    private fun subscribe(userId: String?) {
        var startString = ""
        var finishString: String? = null

        binding.apply {
            jobList.adapter = adapter
            feedButton.setOnClickListener {
                findNavController().navigate(R.id.action_global_postFeedFragment)
            }
            eventWallButton.setOnClickListener {
                findNavController().navigate(R.id.action_global_eventFeedFragment)
            }
            addJobButton.isVisible = arguments?.myJobsArg == true
            addJobButton.setOnClickListener {
                bindAddingJobDialog()
                addJobContainer.isVisible = true
                addJobButton.isVisible = false
            }
            jobStartDate.setOnClickListener {
                val dateDialog = DatePickerDialog(requireContext())
                dateDialog.setOnDateSetListener { datePicker, y, m, d ->
                    startString = setDate(y, m, d, jobStartDate)
                }
                dateDialog.show()
            }
            jobFinishDate.setOnClickListener {
                val dateDialog = DatePickerDialog(requireContext())
                dateDialog.apply {
                    setOnDateSetListener { datePicker, y, m, d ->
                        finishString = setDate(y, m, d, jobFinishDate)
                    }
                    setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.remove)) { _, _ ->
                        jobFinishDate.setText(R.string.job_finish_date)
                        finishString = null
                    }
                }
                dateDialog.show()
            }

            sendJobButton.setOnClickListener {
                if (jobName.text?.isBlank() == true || jobPosition.text?.isBlank() == true || startString.isBlank()) {
                    Toast.makeText(
                        context,
                        getString(R.string.job_not_filled),
                        Toast.LENGTH_LONG
                    )
                        .show()
                } else {
                    viewModel.changeContent(
                        jobName.text.toString(),
                        jobPosition.text.toString(),
                        startString,
                        finishString,
                        jobLink.text.toString()
                    )
                    viewModel.saveNewJob()
                    viewModel.empty()
                    addJobButton.isVisible = true
                    addJobContainer.isVisible = false
                }
            }
        }

        viewModel.apply {
            jobsData.observe(viewLifecycleOwner) {
                adapter.submitList(it)
                binding.empty.isVisible = it.isEmpty()
            }

            dataState.observe(viewLifecycleOwner) {
                when (it) {
                    FeedModelState.Error -> {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.load_jobs_error),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("Retry") {
                                viewModel.saveNewJob()
                            }
                            .show()
                    }
                    else -> {}
                }
                binding.loading.isVisible = it == FeedModelState.Loading
                binding.empty.isVisible = it == FeedModelState.Error
            }
            newJobLoadError.observe(viewLifecycleOwner) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.new_job_error),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Retry") {
                        load(userId)
                    }
                    .show()
            }
            jobRemoveError.observe(viewLifecycleOwner) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.job_delete_error),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Retry") {
                        load(userId)
                    }
                    .show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            binding.apply {
                if (addJobContainer.isVisible == true) {
                    addJobContainer.isVisible = false
                    addJobButton.isVisible = true
                    if (isEditing) {
                        viewModel.empty() //Going back to draft, though it's empty now
                        isEditing = false
                    } else {
                        viewModel.changeContent(
                            jobName.text.toString(),
                            jobPosition.text.toString(),
                            startString,
                            finishString,
                            jobLink.text.toString()
                        )
                    }
                } else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun bindAddingJobDialog() {
        val job = viewModel.edited.value
        job?.let {
            binding.apply {
                jobName.setText(it.name)
                jobPosition.setText(it.position)
                jobLink.setText(it.link)
                if (it.start != "") {
                    jobStartDate.setText(getJobDate(it.start))
                } else {
                    jobStartDate.setText(R.string.job_start_date)
                }
                if (it.finish != null) {
                    jobFinishDate.setText(getJobDate(it.finish))
                } else {
                    jobFinishDate.setText(R.string.job_finish_date)
                }
            }
        }
    }

    private fun setDate(y: Int, m: Int, d: Int, button: MaterialButton): String {
        //"yyyy-mm-ddT15:40:11.996Z" making this type of string
        val yString = y.toString().padStart(4, '0')
        val mString = (m + 1).toString().padStart(2, '0')
        val dString = d.toString().padStart(2, '0')
        val date = "$dString $mString $yString"
        button.text = date
        return "$yString-$mString-${dString}T00:00:01.000Z"
    }

    companion object {
        var Bundle.idArg by StringArg
        var Bundle.myJobsArg by BooleanArg
    }
}