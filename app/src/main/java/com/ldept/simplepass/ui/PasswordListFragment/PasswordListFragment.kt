package com.ldept.simplepass.ui.PasswordListFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ldept.simplepass.R
import com.ldept.simplepass.data.Entities.PasswordEntry
import com.ldept.simplepass.databinding.FragmentPasswordListBinding

class PasswordListFragment : Fragment(), PasswordListAdapter.OnItemClickListener {

    private lateinit var navController : NavController

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
        val viewModel : PasswordListViewModel by viewModels()

        val adapter = PasswordListAdapter(this)
        val repository = 

    }

    override fun onItemClick(password: PasswordEntry) {
        // action
        // TODO: First add another fragment
    }


}