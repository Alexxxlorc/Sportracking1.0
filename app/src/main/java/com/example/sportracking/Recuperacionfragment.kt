package com.example.sportracking

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.sportracking.databinding.FragmentRecuperacionBinding

class Recuperacionfragment : Fragment() {

    private var _binding: FragmentRecuperacionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Usamos el nombre exacto de tu XML para inflar la vista
        _binding = FragmentRecuperacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del botón regresar
        binding.Regresar.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupValidation()

        // Acción al enviar el correo
        binding.botonEnviar.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            Toast.makeText(requireContext(), "Instrucciones enviadas a $email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupValidation() {
        binding.botonEnviar.isEnabled = false

        // Escuchamos los cambios en el campo de email
        binding.etEmail.addTextChangedListener {
            val email = it.toString().trim()
            val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

            // Mostramos el error en el Layout (tilEmail)
            binding.tilEmail.error = if (email.isEmpty() || isValid) null else "Correo inválido"
            binding.botonEnviar.isEnabled = isValid
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}