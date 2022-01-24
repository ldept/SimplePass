package com.ldept.simplepass.ui.passwordListFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.ldept.simplepass.R
import com.ldept.simplepass.SimplePassApp
import com.ldept.simplepass.data.entities.PasswordEntry
import com.ldept.simplepass.databinding.FragmentPasswordListBinding
import com.ldept.simplepass.ui.MainActivity
import com.ldept.simplepass.ui.util.CollapsingToolbarStateChangeListener
import com.ldept.simplepass.ui.util.onQueryTextChanged

class PasswordListFragment : Fragment(), PasswordListAdapter.OnItemClickListener {

    enum class DatabaseOperation {
        INSERT, UPDATE, DELETE
    }

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        val binding = FragmentPasswordListBinding.bind(view)
        val passwordListAdapter = PasswordListAdapter(this)

        val repository = (activity as MainActivity).repository

        val viewModelFactory = PasswordListViewModelFactory(repository, SimplePassApp.preferencesRepository)
        val viewModel: PasswordListViewModel = ViewModelProvider(this, viewModelFactory)
            .get(PasswordListViewModel::class.java)

        val dbFilePath = activity?.getDatabasePath("password_database_encrypted")?.path
        if(dbFilePath != null)
            viewModel.setDbFilePath(dbFilePath)


        binding.apply {
            recyclerview.apply {
                adapter = passwordListAdapter
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
            }
            fab.setOnClickListener {
                val action = PasswordListFragmentDirections
                    .actionPasswordListFragmentToPasswordDetailsFragment()
                navController.navigate(action)
            }

            settingsButton.setOnClickListener {
                val action =
                    PasswordListFragmentDirections.actionPasswordListFragmentToSettingsFragment()
                navController.navigate(action)
            }
            searchBarSearchView.onQueryTextChanged(
                { searchBarSearchView.clearFocus() },
                { text ->
                    val searchQuery = "%$text%"
                    viewModel.searchPasswords(searchQuery)
                        .observe(viewLifecycleOwner) { passwords ->
                            passwordListAdapter.submitList(passwords)
                        }
                }
            )
            // Change AppBar text visibility if scrollview is scrolled
            appBarLayout.addOnOffsetChangedListener(object :
                CollapsingToolbarStateChangeListener() {
                override fun onStateChanged(appBarLayout: AppBarLayout?, currentState: State) {
                    if (currentState == State.COLLAPSED) {
                        titleTextView.visibility = View.INVISIBLE
                        titleTextViewToolbar.visibility = View.VISIBLE
                    } else {
                        titleTextView.visibility = View.VISIBLE
                        titleTextViewToolbar.visibility = View.INVISIBLE
                    }

                }
            })

        }
        viewModel.passwords.observe(viewLifecycleOwner) {
            passwordListAdapter.submitList(it)
        }
    }

    override fun onItemClick(password: PasswordEntry) {
        val action = PasswordListFragmentDirections
            .actionPasswordListFragmentToPasswordDetailsFragment(password)
        navController.navigate(action)
    }


}