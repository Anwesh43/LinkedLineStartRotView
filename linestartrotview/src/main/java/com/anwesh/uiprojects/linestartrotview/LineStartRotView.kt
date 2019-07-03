package com.anwesh.uiprojects.linestartrotview

/**
 * Created by anweshmishra on 03/07/19.
 */

import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.view.View
import android.view.MotionEvent

val nodes : Int = 5
val lines : Int = 4
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#4527A0")
val backColor : Int = Color.parseColor("#BDBDBD")
val sweepDeg : Float = 360f / lines

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawLineStart(i : Int, deg : Float, sc : Float, size : Float, paint : Paint) {
    save()
    rotate(Math.max(deg, i * sweepDeg))
    drawLine(0f, 0f, 0f, -size * sc, paint)
    restore()
}

fun Canvas.drawLinesStart(sc1 : Float, sc2 : Float, size : Float, paint : Paint) {
    var deg : Float = 0f
    for (j in 0..(lines - 1)) {
        var sc1j : Float = sc1.divideScale(j, lines)
        var sc2j : Float = sc2.divideScale(j, lines)
        deg += sweepDeg * sc2j
        drawLineStart(j, deg, sc1j, size, paint)
    }
}

fun Canvas.drawLSRNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    for (j in 0..(lines - 1)) {
        drawLinesStart(sc1, sc2, size, paint)
    }
}

