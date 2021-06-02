package com.bruhascended.fitapp.ui.capturefood

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.fitapp.ui.addfood.FoodSearchActivity

class PredictionPresenter(
    private val context: Context,
    private val recyclerView: RecyclerView
) {
    companion object {
        const val KEY_FOOD_LABEL = "FOOD_LABEL"
    }

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun populate (predictions: Array<String>) {
        recyclerView.post {
            recyclerView.adapter = PredictionRecyclerViewAdaptor(predictions) {
                context.startActivity(
                    Intent(context, FoodSearchActivity::class.java).apply {
                        putExtra(KEY_FOOD_LABEL, it)
                    }
                )
                (context as AppCompatActivity).finish()
            }
        }
    }
}