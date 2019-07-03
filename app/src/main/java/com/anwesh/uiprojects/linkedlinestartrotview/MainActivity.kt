package com.anwesh.uiprojects.linkedlinestartrotview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linestartrotview.LineStartRotView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LineStartRotView.create(this)
    }
}
