package com.aldroid.tic_tac_toe

import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {
    var board =
        Array(3) { arrayOfNulls<String>(3) }
    var turn: String? = null
    var firstTurn: String? = null
    var code: String? = null
    var player: String? = null
    var score_x = 0
    var score_y = 0
    var database = FirebaseDatabase.getInstance()
    var myGameRef: DatabaseReference? = null
    var gameEnd: Boolean? = null
    private var restartBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val intent = intent
        code = intent.getStringExtra("session_code")
        player = intent.getStringExtra("my_player")
        myGameRef = database.getReference("games").child(code!!)
        tvYourShape!!.text = "You are: $player"
        restartBtn = findViewById<View>(R.id.restart_btn) as Button
        startLocal()
        startFB()
        updateUI()
        myGameRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                updateLocal(dataSnapshot)
                var checkWinningValue = checkWinning()
                if (checkWinningValue == "-") {
                    if (check_draw()) {
                        tvTurnText.text = "DRAW!"
                        gameEnd = true
                    }
                } else {
                    score_x = dataSnapshot.child("scores").child("X").getValue(Int::class.java)!!
                    score_y = dataSnapshot.child("scores").child("O").getValue(Int::class.java)!!
                    tvXScore.text = "X - $score_x"
                    tvYScore.text = "O - $score_y"
                    tvTurnText.text = checkWinning() + " WON!"
                    gameEnd = true
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
                Log.w(
                    "Cancelled",
                    "Failed to read value.",
                    databaseError.toException()
                )
            }
        })
        ibOne!!.setOnClickListener { play_local(1) }
        ibTwo!!.setOnClickListener { play_local(2) }
        ibThree!!.setOnClickListener { play_local(3) }
        ibFour!!.setOnClickListener { play_local(4) }
        ibFive!!.setOnClickListener { play_local(5) }
        ibSix!!.setOnClickListener { play_local(6) }
        ibSeven!!.setOnClickListener { play_local(7) }
        ibEight!!.setOnClickListener { play_local(8) }
        ibNine!!.setOnClickListener { play_local(9) }
        restartBtn!!.setOnClickListener {
            myGameRef!!.child("restart").child(player!!).setValue(true)
        }
    }

    private fun play_local(i: Int) {
        if (!gameEnd!!) {
            if (turn == player) {
                if (is_valid_move(i)) {
                    board[(i - 1) / 3][(i - 1) % 3] = player
                    turn = if (player == "X") "O" else "X"
                    updateUI()
                    updateFB(i)
                    if (checkWinning() == "-") {
                        if (check_draw()) {
                            tvTurnText.text = "DRAW!"
                            gameEnd = true
                        }
                    } else {
                        tvTurnText.text = checkWinning() + " WON!"
                        gameEnd = true
                        if (checkWinning() == "X") {
                            score_x += 1
                            tvXScore.text = "X - $score_x"
                            myGameRef!!.child("scores").child(checkWinning()!!).setValue(score_x)
                        } else {
                            score_y += 1
                            tvYScore.text = "O - $score_y"
                            myGameRef!!.child("scores").child(checkWinning()!!).setValue(score_y)
                        }
                    }
                }
            }
        }
    }

    private fun updateFB(i: Int) {
        myGameRef!!.child("board").child(i.toString())
            .setValue(board[(i - 1) / 3][(i - 1) % 3])
        myGameRef!!.child("turn").setValue(turn)
    }

    private fun startFB() {
        for (i in 1..9) {
            myGameRef!!.child("board").child(i.toString()).setValue("-")
        }
        myGameRef!!.child("turn").setValue("X")
        myGameRef!!.child("scores").child("X").setValue(0)
        myGameRef!!.child("scores").child("O").setValue(0)
        myGameRef!!.child("restart").child("X").setValue(false)
        myGameRef!!.child("restart").child("O").setValue(false)
        myGameRef!!.child("first_turn").setValue("X")
    }

    private fun startLocal() {
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = "-"
            }
        }
        firstTurn = "X"
        turn = firstTurn
        score_y = 0
        score_x = score_y
        gameEnd = false
    }

    private fun updateLocal(dataSnapshot: DataSnapshot) {
        if (dataSnapshot.child("restart").child("X")
                .getValue(Boolean::class.java)!! &&
            dataSnapshot.child("restart").child("O")
                .getValue(Boolean::class.java)!!
        ) {
            // Clear Board
            for (i in 0..2) {
                for (j in 0..2) {
                    board[i][j] = "-"
                }
            }
            for (i in 1..9) {
                myGameRef!!.child("board").child(i.toString()).setValue("-")
            }
            // First Turn
            firstTurn = if (firstTurn == "X") "O" else "X"
            myGameRef!!.child("first_turn").setValue(firstTurn)
            // Turn
            turn = firstTurn
            myGameRef!!.child("turn").setValue(turn)
            // Restart
            gameEnd = false
            myGameRef!!.child("restart").child("X").setValue(false)
            myGameRef!!.child("restart").child("O").setValue(false)
            // Scores
            score_x = dataSnapshot.child("scores").child("X").getValue(Int::class.java)!!
            score_y = dataSnapshot.child("scores").child("O").getValue(Int::class.java)!!
            // Restart Button
            updateUI()
        }
        if (turn != player) {
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] != dataSnapshot.child("board")
                            .child((i * 3 + j + 1).toString())
                            .getValue(String::class.java)
                    ) {
                        turn = player
                        board[i][j] =
                            dataSnapshot.child("board").child((i * 3 + j + 1).toString())
                                .getValue(String::class.java)
                    }
                }
            }
        }
        updateUI()
    }

    private fun updateUI() {
        tvXScore!!.text = "X - $score_x"
        tvYScore!!.text = "O - $score_y"
        tvTurnText.text = turn
        when (board[0][0]) {
            "X" -> ibOne!!.setImageResource(R.drawable.cross)
            "O" -> ibOne!!.setImageResource(R.drawable.knot)
            else -> ibOne!!.setImageDrawable(null)
        }
        when (board[0][1]) {
            "X" -> ibTwo!!.setImageResource(R.drawable.cross)
            "O" -> ibTwo!!.setImageResource(R.drawable.knot)
            else -> ibTwo!!.setImageDrawable(null)
        }
        when (board[0][2]) {
            "X" -> ibThree!!.setImageResource(R.drawable.cross)
            "O" -> ibThree!!.setImageResource(R.drawable.knot)
            else -> ibThree!!.setImageDrawable(null)
        }
        when (board[1][0]) {
            "X" -> ibFour!!.setImageResource(R.drawable.cross)
            "O" -> ibFour!!.setImageResource(R.drawable.knot)
            else -> ibFour!!.setImageDrawable(null)
        }
        when (board[1][1]) {
            "X" -> ibFive!!.setImageResource(R.drawable.cross)
            "O" -> ibFive!!.setImageResource(R.drawable.knot)
            else -> ibFive!!.setImageDrawable(null)
        }
        when (board[1][2]) {
            "X" -> ibSix!!.setImageResource(R.drawable.cross)
            "O" -> ibSix!!.setImageResource(R.drawable.knot)
            else -> ibSix!!.setImageDrawable(null)
        }
        when (board[2][0]) {
            "X" -> ibSeven!!.setImageResource(R.drawable.cross)
            "O" -> ibSeven!!.setImageResource(R.drawable.knot)
            else -> ibSeven!!.setImageDrawable(null)
        }
        when (board[2][1]) {
            "X" -> ibEight!!.setImageResource(R.drawable.cross)
            "O" -> ibEight!!.setImageResource(R.drawable.knot)
            else -> ibEight!!.setImageDrawable(null)
        }
        when (board[2][2]) {
            "X" -> ibNine!!.setImageResource(R.drawable.cross)
            "O" -> ibNine!!.setImageResource(R.drawable.knot)
            else -> ibNine!!.setImageDrawable(null)
        }
    }

    private fun is_valid_move(i: Int): Boolean {
        return if (i < 1 || i > 9) false else board[(i - 1) / 3][(i - 1) % 3] == "-"
    }

    private fun checkWinning(): String? {
        for (i in 0..2) {
            if (board[i][0] == board[i][1] && board[i][0] == board[i][2] && board[i][0] != "-"
            ) {
                return board[i][0]
            }
            if (board[0][i] == board[1][i] && board[0][i] == board[2][i] && board[0][i] != "-"
            ) {
                return board[0][i]
            }
        }
        if (board[0][0] == board[1][1] && board[0][0] == board[2][2] && board[0][0] != "-"
        ) {
            return board[0][0]
        }
        return if (board[0][2] == board[1][1] && board[0][2] == board[2][0] && board[0][2] != "-"
        ) {
            board[0][2]
        } else "-"
    }

    private fun check_draw(): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "-") {
                    return false
                }
            }
        }
        return true
    }

    override fun onBackPressed() {
        finish()
        Process.killProcess(Process.myPid())
        super.onBackPressed()
    }
}