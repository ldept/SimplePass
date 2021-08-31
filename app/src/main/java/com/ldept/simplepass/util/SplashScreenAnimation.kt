package com.ldept.simplepass.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

object SplashScreenAnimation {

    private const val animationDuration = 300
    fun crossfade(viewFrom : View, viewTo : View){
        viewTo.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(animationDuration.toLong())
                .setListener(null)
        }
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        viewFrom.animate()
            .alpha(0f)
            .setDuration(animationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    viewFrom.visibility = View.GONE
                }
            })

    }
}