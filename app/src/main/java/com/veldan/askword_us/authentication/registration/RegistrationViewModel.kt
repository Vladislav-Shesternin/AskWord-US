package com.veldan.askword_us.authentication.registration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.veldan.askword_us.authentication.User
import com.veldan.askword_us.global.objects.Verification
import com.veldan.askword_us.global.toast
import kotlinx.coroutines.*

private const val TAG = "RegistrationViewModel"

class RegistrationViewModel(
    private val fragment: RegistrationFragment,
) : ViewModel() {

    // Coroutine
    private val scope = viewModelScope

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val fireDb = FirebaseDatabase.getInstance()
    private val users = fireDb.getReference("Users")
    private var fireUser: FirebaseUser? = null

    // Properties
    private val context = fragment.requireContext()


    //==============================
    //          Registration
    //==============================
    fun registration(user: User) {
        val name = user.name
        val surname = user.surname
        val email = user.email
        val password = user.password

        if (Verification.verifyNameSurname(context, name, surname) &&
            Verification.verifyEmailPassword(context, email, password)
        ) {
            "Проверка пройдена".toast(context)
            scope.launch(Dispatchers.Default) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        "Пользователя зарегистрировано".toast(context)

                        fireUser = auth.currentUser
                        fireUser!!.sendEmailVerification()
                            .addOnSuccessListener {
                                "Подтвердите адрес: $email".toast(context)

                                // Checking email confirmation every one second
                                scope.launch(Dispatchers.Default) {
                                    verificationEmail()
                                    val userWithoutPassword = User(name, surname, email)
                                    addUserFireDb(userWithoutPassword)
                                    transitionToStart()
                                }
                            }
                            .addOnFailureListener {
                                "Не удалось отправить: $email".toast(context)
                            }
                    }
                    .addOnFailureListener {
                        "Пользователя не зарегистрировано".toast(context)
                    }
            }
        }
    }

    //==============================
    //          VerificationEmail
    //==============================
    private suspend fun verificationEmail() {
        val verifyJob = scope.launch {
            var a = 0
            fireUser?.let { user ->
                while (!(user.isEmailVerified)) {
                    delay(1000)
                    user.reload()
                    Log.i(TAG, "Подтвердите...${++a}")
                }
            }
        }
        verifyJob.join()
    }

    //==============================
    //          AddUserFireDb
    //==============================
    private suspend fun addUserFireDb(user: User) {
        val addUserJob = scope.launch {
            users.child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(user)
                .addOnSuccessListener {
                    "Пользователя добавлено в БД".toast(context)
                }
        }
        addUserJob.join()
    }

    //==============================
    //          DeleteUser
    //==============================
    private fun deleteUser() {
        fireUser?.let {
            CoroutineScope(Dispatchers.Default).launch {
                it.delete()
                    .addOnSuccessListener {
                        Log.i(TAG, "Пользователя удалено")
                    }
            }
        }
    }

    //==============================
    //          TransitionToStart
    //==============================
    private fun transitionToStart() {
        val action = RegistrationFragmentDirections.actionRegistrationFragmentToStartFragment()
        fragment.findNavController().navigate(action)
    }

    //==============================
    //          OnCleared
    //==============================
    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onDetach:")
        deleteUser()
    }

}