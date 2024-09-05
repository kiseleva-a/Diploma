package ru.netology.nework

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var appAuth: AppAuth
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authViewModel.state.observe(this) {
            invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_auth, menu)

        menu.let {
            it.setGroupVisible(R.id.authorized, authViewModel.authorized)
            it.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logOut -> {
                if (findNavController(R.id.fragment_container).currentDestination?.id == R.id.newPostFragment) {
                    false
                } else {
                    appAuth.removeAuth()
                    true
                }
            }
            R.id.signIn -> {
                findNavController(R.id.fragment_container).navigate(R.id.action_global_fragment_sing_in)
                true
            }
            R.id.signUp -> {
                findNavController(R.id.fragment_container).navigate(R.id.action_global_signUpFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}