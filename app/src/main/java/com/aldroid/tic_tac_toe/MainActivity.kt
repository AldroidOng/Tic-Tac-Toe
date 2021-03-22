package com.aldroid.tic_tac_toe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart.setOnClickListener(this)
        btnJoin.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            btnStart.id -> {
                val intent: Intent = Intent(applicationContext, StartActivity::class.java)
                startActivity(intent)
            }

            btnJoin.id -> {
                val intent: Intent = Intent(applicationContext, JoinActivity::class.java)
                startActivity(intent)
            }
        }
    }
}