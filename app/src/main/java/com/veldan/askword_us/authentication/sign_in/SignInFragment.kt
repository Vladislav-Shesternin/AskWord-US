package com.veldan.askword_us.authentication.sign_in

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.veldan.askword_us.authentication.GoogleAccount
import com.veldan.askword_us.authentication.User
import com.veldan.askword_us.databinding.FragmentSignInBinding
import com.veldan.askword_us.global.defaultFocusAndKeyboard
import com.veldan.askword_us.global.emums.RequestCode

private const val TAG = "SignInFragment"

class SignInFragment : Fragment() {

    // Binding
    private lateinit var binding: FragmentSignInBinding

    // ViewModel and Factory
    private lateinit var viewModel: SignInViewModel
    private lateinit var viewModelFactory: SignInViewModelFactory

    // Properties
    private lateinit var googleAccount: GoogleAccount

    // Components UI
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        initViewModels()
        initBinding(inflater)
        initProperties()
        initComponentsUI()


        return binding.root
    }

    // ==============================
    //        Initializing
    // ==============================
    private fun initViewModels() {
        viewModelFactory = SignInViewModelFactory(this)
        viewModel = ViewModelProvider(this,
            viewModelFactory).get(SignInViewModel::class.java)
    }

    private fun initBinding(inflater: LayoutInflater) {
        binding = FragmentSignInBinding.inflate(inflater)
        binding.signInFragment = this
    }

    private fun initProperties() {
        googleAccount = GoogleAccount(this)
    }

    private fun initComponentsUI() {
        binding.also {
            editEmail = it.editEmail
            editPassword = it.editPassword
        }
    }

    // ==============================
    //          GetUser
    // ==============================
    private fun getUser() = User(
        email = binding.editEmail.text.toString(),
        password = binding.editPassword.text.toString())

    // ==============================
    //          SignIn
    // ==============================
    fun signIn() {
        viewModel.signIn(getUser())
    }

    // ==============================
    //          SignInWithGoogle
    // ==============================
    fun signInWithGoogle() {
        googleAccount.signInWithGoogle()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode.RC_GOOGLE.id) {
            googleAccount.signOutDefaultAccount()
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.i(TAG, "Account: ${account.email}")
                setAccount(account)
                editPassword.defaultFocusAndKeyboard(true)
            } catch (e: ApiException) {
                Log.i(TAG, "Exception.!.!.")
            }
        }
    }

    // ==============================
    //          SetAccount
    // ==============================
    private fun setAccount(account: GoogleSignInAccount) {
        account.also {
            editEmail.setText(it.email)
        }
    }

    // ==============================
    //          ForgetPassword
    // ==============================
    fun forgetPassword() {
        viewModel.forgetPassword(getUser())
    }
}