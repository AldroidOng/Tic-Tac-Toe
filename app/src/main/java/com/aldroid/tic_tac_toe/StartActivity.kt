package com.aldroid.tic_tac_toe

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_start.*
import java.util.*

class StartActivity : AppCompatActivity() {
    // get database Firebase instance
    var database = FirebaseDatabase.getInstance()
    // get the database object to the path "sessions"
    var myRef = database.getReference("sessions")

    var isCodeGenerated = false
    var sessionCode = 0
    var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        pd = ProgressDialog(this)
        pd!!.setCancelable(false)
        pd!!.setCanceledOnTouchOutside(false)
        pd!!.setMessage("Generating Code")
        pd!!.show()
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                while (!isCodeGenerated) {
                    val random = Random()
                    var n: Int
                    //generate random number between 1000 - 9999 (btw 0 to 8999 + 1000)
                    n = random.nextInt(9000) + 1000
                    if (dataSnapshot.child(n.toString()).exists()) {
                        if (dataSnapshot.child(n.toString()).child("p1").exists()) {
                            var calendar = Calendar.getInstance()
                            // If the existing token generated is more than 24 hours, then can reuse token to start game
                            if (dataSnapshot.child(n.toString()).child("start_time")
                                    .getValue(Long::class.java)!! - calendar.getTimeInMillis() >= 86400000
                            ) {
                                startSession(n)
                            }
                        } else {
                            startSession(n)
                        }
                    } else {
                        startSession(n)
                    }
                }
                if (isCodeGenerated) {
                    if (dataSnapshot.child(sessionCode.toString()).child("p2").exists()) {
                        Toast.makeText(baseContext, "Game Started!", Toast.LENGTH_SHORT).show()
                        startActivity(
                            Intent(baseContext, GameActivity::class.java)
                                .putExtra("session_code", sessionCode.toString())
                                .putExtra("my_player", "X")
                        )
                        finish()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Cancelled", "Failed to read value.", error.toException())
            }
        })
    }

    private fun startSession(n: Int) {
        val android_id = Secure.getString(contentResolver, Secure.ANDROID_ID)
        var calendar = Calendar.getInstance()
        isCodeGenerated = true
        sessionCode = n
        myRef.child(n.toString()).child("p1").setValue(android_id)
        calendar = Calendar.getInstance()
        myRef.child(n.toString()).child("start_time").setValue(calendar.getTimeInMillis())
        txtCode.text = n.toString()
        pd!!.dismiss()
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}