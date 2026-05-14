package home.account

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sportracking.databinding.FragmentAccountBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import onboarding.MainActivity
import onboarding.personal.model.UserProfile
import java.net.URL

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Photo Picker — selector de imagen del sistema (Android 13+ con backport)
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) uploadProfilePhoto(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.btnEditPhoto.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnLogout.setOnClickListener {
            confirmLogout()
        }
    }



    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        binding.tvEmail.text = currentUser.email ?: "—"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val profile = snapshot.toObject(UserProfile::class.java)

                if (profile != null) {
                    fillProfileFields(profile)
                    if (profile.photoUrl.isNotBlank()) {
                        loadProfilePhoto(profile.photoUrl)
                    }
                } else {
                    showEmptyProfile()
                }
            } catch (e: Exception) {
                showEmptyProfile()
            }
        }
    }

    private fun fillProfileFields(profile: UserProfile) {
        val fullName = listOf(
            profile.firstName,
            profile.middleName,
            profile.lastNamePaternal,
            profile.lastNameMaternal
        ).filter { it.isNotBlank() }.joinToString(" ")

        binding.tvFullName.text = if (fullName.isNotBlank()) fullName else "Usuario"
        binding.tvInitials.text = buildInitials(profile.firstName, profile.lastNamePaternal)
        binding.tvUserHandle.text =
            if (profile.userName.isNotBlank()) "@${profile.userName}" else ""

        binding.tvUserName.text =
            if (profile.userName.isNotBlank()) profile.userName else "—"
        binding.tvPhone.text =
            if (profile.phone.isNotBlank()) profile.phone else "—"
        binding.tvBirthDate.text =
            if (profile.birthDate.isNotBlank()) profile.birthDate else "—"
    }

    private fun showEmptyProfile() {
        binding.tvFullName.text = "Usuario"
        binding.tvInitials.text = "?"
        binding.tvUserHandle.text = ""
        binding.tvUserName.text = "—"
        binding.tvPhone.text = "—"
        binding.tvBirthDate.text = "—"
    }

    private fun buildInitials(firstName: String, lastName: String): String {
        val first = firstName.firstOrNull()?.uppercaseChar()
        val last = lastName.firstOrNull()?.uppercaseChar()
        return when {
            first != null && last != null -> "$first$last"
            first != null -> first.toString()
            else -> "?"
        }
    }



    private fun loadProfilePhoto(url: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    URL(url).openStream().use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }
            if (bitmap != null && _binding != null) {
                binding.ivProfilePhoto.setImageBitmap(bitmap)
                binding.ivProfilePhoto.isVisible = true
                binding.tvInitials.isVisible = false
            }
        }
    }

    private fun uploadProfilePhoto(uri: Uri) {
        val currentUser = auth.currentUser ?: return

        // Mostrar la foto seleccionada inmediatamente (preview optimista)
        binding.ivProfilePhoto.setImageURI(uri)
        binding.ivProfilePhoto.isVisible = true
        binding.tvInitials.isVisible = false
        binding.photoProgress.isVisible = true
        binding.btnEditPhoto.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1) Subir bytes a Firebase Storage
                val ref = storage.reference
                    .child("users")
                    .child(currentUser.uid)
                    .child("profile.jpg")

                ref.putFile(uri).await()

                // 2) Obtener URL pública
                val downloadUrl = ref.downloadUrl.await().toString()

                // 3) Guardar URL en Firestore
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update("photoUrl", downloadUrl)
                    .await()

                if (_binding != null) {
                    Toast.makeText(
                        requireContext(),
                        "Foto actualizada",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo subir la foto: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                if (_binding != null) {
                    binding.photoProgress.isVisible = false
                    binding.btnEditPhoto.isEnabled = true
                }
            }
        }
    }


    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres cerrar sesión?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Cerrar sesión") { _, _ -> logout() }
            .show()
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
