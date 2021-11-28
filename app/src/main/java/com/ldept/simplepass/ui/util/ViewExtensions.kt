package com.ldept.simplepass.ui.util

import androidx.appcompat.widget.SearchView

inline fun SearchView.onQueryTextChanged(
    crossinline submitListener: () -> Unit,
    crossinline changeListener: (String) -> Unit
){
    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            submitListener()
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            changeListener(newText.orEmpty())
            return true
        }

    })
}