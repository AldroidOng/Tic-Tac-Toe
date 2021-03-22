package com.aldroid.tic_tac_toe

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_join.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class JoinActivity : AppCompatActivity(), View.OnClickListener  {
    var database = FirebaseDatabase.getInstance()
    var myRef = database.getReference("sessions")
    var dataSnapshot: DataSnapshot? = null
    var pd: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        pd = ProgressDialog(this)
        pd!!.setCancelable(false)
        pd!!.setCanceledOnTouchOutside(false)
        pd!!.setMessage("Please wait")
        pd!!.show()
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pd!!.dismiss()
                dataSnapshot = snapshot
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Cancelled", "Failed to read value.", error.toException())
            }
        })

        btnSubmit.setOnClickListener(this)
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            btnSubmit.id -> {
                val code = arrayOf("0000")
                if (etCode.text.length == 4) {
                    code[0] = etCode.text.toString()
                    if (dataSnapshot!!.child(code[0]).exists()) {
                        if (dataSnapshot!!.child(code[0]).child("p2").exists()) {
                            Toast.makeText(
                                baseContext,
                                "The Game has already started. Please generate a new code.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            var calendar = Calendar.getInstance()
                            if (dataSnapshot!!.child(code[0]).child("start_time")
                                    .getValue(Long::class.java)!! - calendar.getTimeInMillis() >= 86400000
                            ) {
                                Toast.makeText(
                                    baseContext,
                                    "The code has expired. Please generate a new code.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                val android_id = Secure.getString(
                                    contentResolver,
                                    Secure.ANDROID_ID
                                )
                                if (android_id == dataSnapshot!!.child(code[0]).child("p1")
                                        .getValue(String::class.java)
                                ) {
                                    Toast.makeText(
                                        baseContext,
                                        "You can't join a game started by you.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    myRef.child(code[0]).child("p2")
                                        .setValue(android_id)
                                    Toast.makeText(
                                        baseContext, "Game Started!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    startActivity(
                                        Intent(baseContext, GameActivity::class.java)
                                            .putExtra("session_code", code[0])
                                            .putExtra("my_player", "O")
                                    )
                                    finish()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(baseContext, "Invalid Code!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}