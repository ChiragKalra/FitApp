package com.bruhascended.fitapp.main

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import com.bruhascended.fitapp.addfood.AddFoodActivity
import com.bruhascended.fitapp.addworkout.AddWorkoutActivity
import com.bruhascended.fitapp.capturefood.CaptureFoodActivity
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.logweight.LogWeightActivity
import com.bruhascended.fitapp.util.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabPresenter (
    private val mActivity: Activity,
    private val binding: ActivityMainBinding
) {

    // onClick destinations
    private val actionDestinations = arrayOf (
        CaptureFoodActivity::class.java,
        AddFoodActivity::class.java,
        AddWorkoutActivity::class.java,
        LogWeightActivity::class.java
    )

    // all FABs, their text descriptions
    private var actionButtons: Array<FloatingActionButton>
    private var actionDescriptions: Array<TextView>

    init {
        binding.fabsLayout.apply {
            actionButtons = arrayOf (
                captureFoodButton, addFoodButton, addWorkoutButton, addWeightButton
            )
            actionDescriptions = arrayOf (
                textCapture, textFood, textWorkout, textWeight
            )
        }
    }

    fun setupFABs() {
        setupIntents()
        setupEntryAndExit()
    }

    private fun setupIntents () {
        // attach destination intents to FABs
        for (i in 0..3) {
            actionButtons[i].setOnClickListener {
                mActivity.startActivity(Intent(mActivity, actionDestinations[i]))
                binding.fabsLayout.cancelActionButton.callOnClick()
            }
        }
    }

    private fun setupEntryAndExit () {
        binding.fabsLayout.apply {
            // on show FAB buttons
            binding.addActionButton.setOnClickListener {
                binding.addActionButton.animateRotation(135f).animateFadeOut()
                cancelActionButton.animateRotation(135f).animateFadeIn(1f)
                backgroundView.animateFadeIn(0.95f)
                for (actionButton in actionButtons) {
                    actionButton.animateFadeUpIn(mActivity.toPx(12))
                }
                for (textView in actionDescriptions) {
                    textView.animateFadeUpIn(mActivity.toPx(12))
                }
            }
            // on hide fab buttons
            cancelActionButton.setOnClickListener {
                binding.addActionButton.animateRotation(0f).animateFadeIn(1f)
                cancelActionButton.animateRotation(0f).animateFadeOut()
                backgroundView.animateFadeOut()
                for (actionButton in actionButtons) {
                    actionButton.animateFadeDownOut(mActivity.toPx(12))
                }
                for (textView in actionDescriptions) {
                    textView.animateFadeDownOut(mActivity.toPx(12))
                }
            }
            // hide FAB buttons on tap outside
            backgroundView.setOnClickListener {
                cancelActionButton.callOnClick()
            }
        }
    }
}