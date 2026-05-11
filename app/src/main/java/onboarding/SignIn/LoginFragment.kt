package onboarding.SignIn

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sportracking.R
import com.example.sportracking.core.FragmentCommunicator
import com.example.sportracking.databinding.FragmentLoginBinding
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import home.HomeActivity
import kotlinx.coroutines.launch
import com.example.sportracking.core.ResponseService

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SignInViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupValidation()
        observeState()
        binding.btnLogin.setOnClickListener {

            val email = binding.etCorreo.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            viewModel.requestLogin(email, password)
        }
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        return binding.root
    }

    private fun setupValidation() {
        binding.btnLogin.isEnabled = false

        binding.etCorreo.addTextChangedListener {
            validateFields()
        }
        binding.etPassword.addTextChangedListener {
            validateFields()
        }
    }

    private fun validateFields() {
        val email = binding.etCorreo.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        val isEmailValid = isValidEmail(email)
        val isPasswordValid = password.length >= 8

        binding.etCorreo.error = if (email.isNotEmpty() && isEmailValid) null else "Correo invalido"
        binding.etPassword.error = if (password.isNotEmpty() && isPasswordValid) null else "Minimo 8 caracteres"

        binding.btnLogin.isEnabled =
            email.isNotEmpty() && password.isNotEmpty() && isEmailValid && isPasswordValid
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                        }

                        is ResponseService.Success<*> -> {
                            communicator.manageLoader(false)

                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                        }

                        is ResponseService.Error -> {
                            communicator.manageLoader(false)

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

}

