package com.example.nahad_sliding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.LayoutDirection
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.nahad_sliding.progressLayout.ProgressLayout
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder
import android.widget.RelativeLayout
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.layout_button.view.*

class MainActivity : AppCompatActivity() {

    var isLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {

        //initialize Swipe Login
        sbLogin.isRtl = true
        sbLogin.setGravity(true)
        sbLogin.configureTouch(true)

        //initialize Swipe Logout
        sbLogout.isRtl = false
        sbLogout.setGravity(false)
        sbLogout.configureTouch(false)

        //Listener Swipe
        listenerLogin()
        listenerLogout()

        //listener Progress
        listenerProgress()
        listenerImgClose()

    }

    private fun listenerProgress() {
        prLayout.setProgressLayoutListener(object : ProgressLayout.ProgressLayoutListener {
            override fun onProgressCompleted() {

                if (isLogin) {
                    sbLogin.visibility = View.GONE
                    sbLogout.visibility = View.VISIBLE
                    rlProgress.visibility = View.GONE
                    //todo send login to server
                } else {
                    sbLogout.visibility = View.GONE
                    sbLogin.visibility = View.GONE
                    rlProgress.visibility = View.GONE
                    //todo send logout to server
                }

            }

            override fun onProgressChanged(seconds: Int) {
                calculateSongDuration(seconds)
            }
        })
    }

    private fun listenerImgClose() {
        ivProgress.setOnClickListener {
            if (prLayout.isPlaying) {
                prLayout.stop()
                prLayout.cancel()

                if (isLogin) {
                    Toast.makeText(this, "show login", Toast.LENGTH_SHORT).show()
                    sbLogin.visibility = View.VISIBLE
                    sbLogout.visibility = View.GONE
                    rlProgress.visibility = View.GONE
                    listenerLogin()
                } else {
                    Toast.makeText(this, "show logout", Toast.LENGTH_SHORT).show()
                    sbLogout.animatedToStart()
                    sbLogout.visibility = View.VISIBLE
                    sbLogin.visibility = View.GONE
                    rlProgress.visibility = View.GONE
                    listenerLogout()
                }

            }
        }
    }

    private fun listenerLogin() {
        sbLogin.setOnStateChangeListener {

            rlProgress.visibility = View.VISIBLE
            sbLogin.visibility = View.GONE
            sbLogout.visibility = View.GONE

            val params = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val emptyColor = ContextCompat.getColor(this, R.color.color_empty)
            val greenColor = ContextCompat.getColor(this, R.color.color_green)
            prLayout.init(this, null, greenColor , emptyColor)
            prLayout.setMaxProgress(5) // 5 second
            prLayout.start()
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            params.setMargins(0, 0, 20, 0)
            ivProgress.layoutParams = params

            isLogin = true
        }
    }

    private fun listenerLogout() {
        sbLogout.setOnStateChangeListener {

            rlProgress.visibility = View.VISIBLE
            sbLogout.visibility = View.GONE
            sbLogin.visibility = View.GONE

            val params = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val emptyColor = ContextCompat.getColor(this, R.color.color_empty)
            val greenColor = ContextCompat.getColor(this, R.color.color_red)
            prLayout.init(this, null, greenColor, emptyColor)
            prLayout.setMaxProgress(5) // 5 second
            prLayout.start()
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
            params.setMargins(20,0,0,0)
            ivProgress.layoutParams = params

            isLogin = false
        }
    }

    private fun calculateSongDuration(seconds: Int): String {
        return StringBuilder((seconds / 60).toString())
            .append(":")
            .append((seconds % 60).toString())
            .toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        prLayout.stop()
        prLayout.cancel()
    }

}