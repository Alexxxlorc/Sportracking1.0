package onboarding.personal

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sportracking.core.FragmentCommunicator
import com.example.sportracking.core.ResponseService
import com.example.sportracking.databinding.FragmentInfoPersonalBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar
import home.HomeActivity


class InfoPersonalFragment : Fragment() {

    private var _binding: FragmentInfoPersonalBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<PersonalInfoViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoPersonalBinding.inflate(inflater, container, false)

        communicator = requireActivity() as FragmentCommunicator

        setupValidation()
        setupDatePicker()
        setupClickListeners()
        observeState()

        return binding.root
    }

    private fun setupValidation() {
        binding.btnSavePersonalInfo.isEnabled = false

        binding.etFirstName.addTextChangedListener { validateAndEnable() }
        binding.etMiddleName.addTextChangedListener { validateAndEnable() }
        binding.etLastNamePaternal.addTextChangedListener { validateAndEnable() }
        binding.etLastNameMaternal.addTextChangedListener { validateAndEnable() }
        binding.etUsername.addTextChangedListener { validateAndEnable() }
        binding.etPhone.addTextChangedListener { validateAndEnable() }
        binding.etBirthDate.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val firstName = binding.etFirstName.text.toString().trim()
        val middleName = binding.etMiddleName.text.toString().trim()
        val lastNamePaternal = binding.etLastNamePaternal.text.toString().trim()
        val lastNameMaternal = binding.etLastNameMaternal.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val birthDate = binding.etBirthDate.text.toString().trim()

        binding.tilFirstName.error = viewModel.validateFirstName(firstName)
        binding.tilLastNamePaternal.error = viewModel.validateLastNamePaternal(lastNamePaternal)
        binding.tilUsername.error = viewModel.validateUsername(username)
        binding.tilPhone.error = viewModel.validatePhone(phone)
        binding.tilBirthDate.error = viewModel.validateBirthDate(birthDate)

        binding.btnSavePersonalInfo.isEnabled =
            viewModel.isFormValid(
                firstName,
                middleName,
                lastNamePaternal,
                lastNameMaternal,
                username,
                phone,
                birthDate
            )
    }

    private fun setupDatePicker() {
        binding.etBirthDate.setOnClickListener {
            val cal = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val formatted = "%04d-%02d-%02d".format(year, month + 1, day)
                    binding.etBirthDate.setText(formatted)
                },
                cal.get(Calendar.YEAR) - 18,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }

    private fun setupClickListeners() {
        binding.btnSavePersonalInfo.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (uid == null) {
                Snackbar.make(binding.root, "Sesión inválida", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            viewModel.saveProfile(
                uid = uid,
                firstName = binding.etFirstName.text.toString().trim(),
                middleName = binding.etMiddleName.text.toString().trim(),
                lastNamePaternal = binding.etLastNamePaternal.text.toString().trim(),
                lastNameMaternal = binding.etLastNameMaternal.text.toString().trim(),
                username = binding.etUsername.text.toString().trim(),
                phone = binding.etPhone.text.toString().trim(),
                birthDate = binding.etBirthDate.text.toString().trim()
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnSavePersonalInfo.isEnabled = false
                        }

                        is ResponseService.Success -> {
                            communicator.manageLoader(false)

                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnSavePersonalInfo.isEnabled = true
                            Snackbar.make(binding.root, state.data, Snackbar.LENGTH_LONG).show()
                        }

                        null -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}