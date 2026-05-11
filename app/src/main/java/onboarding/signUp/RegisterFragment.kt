package onboarding.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.sportracking.R
import com.example.sportracking.core.FragmentCommunicator
import com.example.sportracking.core.ResponseService
import com.example.sportracking.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<RegisterViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator

        setupValidation()
        setupClickListeners()
        observeState()

        return binding.root
    }

    private fun setupValidation() {
        binding.btnRegister.isEnabled = false

        binding.etCorreo.addTextChangedListener { validateAndEnable() }
        binding.etPassword.addTextChangedListener { validateAndEnable() }
        binding.confirmPasswordTiet.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val email = binding.etCorreo.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm = binding.confirmPasswordTiet.text.toString().trim()

        binding.etCorreo.error = viewModel.validateEmail(email)
        binding.etPassword.error = viewModel.validatePassword(password)
        binding.confirmPasswordTiet.error =
            viewModel.validateConfirmPassword(password, confirm)

        binding.btnRegister.isEnabled =
            viewModel.isRegisterFormValid(
                email,
                password,
                confirm
            )
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etCorreo.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            viewModel.requestSignUp(email, password)
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnRegister.isEnabled = false
                        }

                        is ResponseService.Success -> {
                            communicator.manageLoader(false)

                            findNavController().navigate(
                                R.id.action_registerFragment_to_infoPersonalFragment
                            )
                        }

                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnRegister.isEnabled = true

                            Snackbar.make(
                                binding.root,
                                state.data,
                                Snackbar.LENGTH_LONG
                            ).show()
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