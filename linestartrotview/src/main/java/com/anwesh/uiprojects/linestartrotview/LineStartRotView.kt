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
val totalDeg : Float = 360f
val sweepDeg : Float = totalDeg / lines
val rFactor : Int = 4
val delay : Long = 20


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
    rotate(Math.max(sweepDeg * i, deg))
    drawLine(0f, 0f, 0f, -size * sc, paint)
    restore()
}

fun Canvas.drawLinesStart(sc1 : Float, sc2 : Float, size : Float, paint : Paint) {
    var deg : Float = totalDeg * sc2
    for (j in 0..(lines - 1)) {
        var sc1j : Float = sc1.divideScale(j, lines)
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
    save()
    translate(w / 2, gap * (i + 1))
    drawCircle(0f, 0f, size / rFactor, paint)
    for (j in 0..(lines - 1)) {
        drawLinesStart(sc1, sc2, size, paint)
    }
    restore()
}

class LineStartRotView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, lines)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LSRNode(var i : Int, val state : State = State()) {

        private var next : LSRNode ?= null
        private var prev : LSRNode ?= null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes -1) {
                next = LSRNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLSRNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LSRNode {
            var curr : LSRNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LineStartRot(var i : Int) {

        private val root : LSRNode = LSRNode(0)
        private var curr : LSRNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *=- -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LineStartRotView) {

        private val animator : Animator = Animator(view)
        private val lsr : LineStartRot = LineStartRot(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            lsr.draw(canvas, paint)
            animator.animate {
                lsr.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lsr.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : LineStartRotView {
            val view : LineStartRotView = LineStartRotView(activity)
            activity.setContentView(view)
            return view
        }
    }
}