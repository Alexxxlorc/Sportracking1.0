package com.example.sportracking.core

import android.util.Log
import com.example.sportracking.core.ResponseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(): Authentication {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    override suspend fun requestLogin(
        email: String,
        password: String
    ): ResponseService<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                return@withContext ResponseService.Success(it)
            } ?: return@withContext ResponseService.Error("Usuario no encontrado")

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return@withContext ResponseService.Error("Correo o Contraseña incorrectos")

        } catch (e: FirebaseAuthException) {
            return@withContext ResponseService.Error(e.localizedMessage ?: "Error de autenticación")

        } catch (e: Exception) {
            return@withContext ResponseService.Error("Error inesperado: ${e.message}")
        }
    }
    override suspend fun requestSignUp(
        email: String,
        password: String
    ): ResponseService<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                ResponseService.Success(it)
            } ?: ResponseService.Error("No se pudo crear el usuario")

        } catch (e: FirebaseAuthUserCollisionException) {
            ResponseService.Error("Este correo ya está registrado, intenta con otro")

        } catch (e: FirebaseAuthWeakPasswordException) {
            ResponseService.Error("La contraseña es muy débil")

        } catch (e: Exception) {
            ResponseService.Error("Error inesperado: ${e.localizedMessage}")
        }
      }
    }