package com.odd.ostrinov2.dialogFragments

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import com.odd.ostrinov2.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ultra_timer.view.*
import java.util.*
import java.util.concurrent.TimeUnit


class CountDown : DialogFragment() {

    lateinit var builder: AlertDialog.Builder
    lateinit var inflater: LayoutInflater
    lateinit var dialogView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        builder = AlertDialog.Builder(activity)

        inflater = activity.layoutInflater
        dialogView = inflater.inflate(R.layout.ultra_timer, null)

        launchTimer()
        builder.setView(dialogView)

        return builder.create()
    }

    fun launchTimer(){

        val url = "https://cdn.gamerant.com/wp-content/uploads/pokemon-ultra-sun-ultra-moon-alternate-story-3ds.jpg.webp"
        Picasso.with(activity.applicationContext).load(Uri.parse(url)).into(dialogView.ivPokemon)

        val start_calendar = Calendar.getInstance()
        val start_millis = start_calendar.timeInMillis //get the start time in milliseconds
        val end_calendar = Calendar.getInstance()
        end_calendar.set(2017, 10, 17, 0, 0) // 10 = November, month start at 0 = January
        val end_millis = end_calendar.timeInMillis //get the end time in milliseconds
        val total_millis = end_millis - start_millis //total time in milliseconds

        //1000 = 1 second interval
        val cdt = object : CountDownTimer(total_millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var millisUntilFinished = millisUntilFinished
                val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                millisUntilFinished -= TimeUnit.DAYS.toMillis(days)

                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)

                val minutes= TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

                val seconds: Int = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()

                val timerString: String = days.toString() + ":" + hours.toInt() + ":" + minutes.toInt() + ":" + seconds //You can compute the millisUntilFinished on hours/minutes/seconds
                dialogView.tvTimer.text = timerString
            }

            override fun onFinish() {
                //TODO Add memes
                dialogView.tvTimer.text = "Finished"
            }
        }
        cdt.start()
    }
}
