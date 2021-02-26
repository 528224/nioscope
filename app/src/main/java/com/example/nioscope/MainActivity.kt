package com.example.nioscope

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.reflect.Method


//https://stackoverflow.com/questions/5800002/how-to-remove-drawn-line-in-android

class MainActivity : AppCompatActivity() {

    val width = 500
    val height = 300
    val columns = 5
    val rows = 10

    var currentValueIndex = -1

    var canvas: Canvas? = null
    var paint: Paint? = null
    var paintForGragh: Paint? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val myView = PixelGridView(this)
        //id_frame.addView(myView)
        initialise()
        reload()
        reload.setOnClickListener { reload() }


    }

    fun refreshCanvas(){
        // Initialize a new Bitmap object
        val bitmap = Bitmap.createBitmap(
            width, // Width
            height, // Height
            Bitmap.Config.ARGB_8888 // Config
        )

        // Initialize a new Canvas instance
        canvas = Canvas(bitmap)

        // Draw a solid color on the canvas as background
        canvas?.drawColor(Color.LTGRAY)

        iv.setImageBitmap(bitmap)
        drawGrids(columns, rows)
    }

    fun initialise(){

        // Initialize a new Paint instance to draw the line
        val paint1 = Paint()
        // Line color
        paint1.setColor(Color.WHITE)
        paint1.setStyle(Paint.Style.STROKE)
        // Line width in pixels
        paint1.setStrokeWidth(2f)
        paint1.setAntiAlias(true)
        paint = paint1


        val paint2 = Paint()
        // Line color
        paint2.setColor(Color.RED)
        paint2.setStyle(Paint.Style.STROKE)
        // Line width in pixels
        paint2.setStrokeWidth(5f)
        paint2.setAntiAlias(true)
        paintForGragh = paint2
    }


    fun drawGrids(hori: Int, vert: Int){
        for (x in 0..hori) {
            val y0 = 0f
            val y1 = height.toFloat()
            val x0 = (x * width/hori).toFloat()
            val x1 = x0
            canvas?.drawLine(
                x0, // startX
                y0, // startY
                x1, // stopX
                y1, // stopY
                paint!! // Paint
            )
        }

        for (y in 0..vert) {
            val x0 = 0f
            val x1 = width.toFloat()
            val y0 = (y * height/vert).toFloat()
            val y1 = y0
            canvas?.drawLine(
                x0, // startX
                y0, // startY
                x1, // stopX
                y1, // stopY
                paint!! // Paint
            )
        }
    }

    fun drawLine(fromIndex: Int, fromValue: Int, toIndex: Int, toValue: Int){
        val x0 = (fromIndex * width/columns).toFloat()
        val x1 = (toIndex * width/columns).toFloat()
        val y0 = (fromValue * height/rows).toFloat()
        val y1 = (toValue * height/rows).toFloat()
        canvas?.drawLine(
            x0, // startX
            y0, // startY
            x1, // stopX
            y1, // stopY
            paintForGragh!! // Paint
        )
    }




    //For testing

    fun test(){
        val values = ArrayList<Int>()
           for (i in 0..columns){
               values.add((0..rows).random())
           }

        values.forEachIndexed { index, value ->
            if (index>0){
                drawLine(index-1,values[index-1],index, values[index])
            }
        }
    }

    fun generateDummyInput(): Any?{
        val input: Any? = null
        if (currentValueIndex < 0){
            currentValueIndex++
            return "START"
        }else if (currentValueIndex < columns){
            currentValueIndex++
            return (0..rows).random()
        }else if (currentValueIndex == columns){
            currentValueIndex++
            return "END"
        }else if (currentValueIndex == columns+1){
            currentValueIndex++
            return 2
        }else if (currentValueIndex == columns+2){
            currentValueIndex = -1
            return 5
        }
        return input
    }

    fun reload(){
        refreshCanvas()
        test()
    }

    fun on(view: View) {}
    fun off(view: View) {}
    fun visible(view: View) {}
    fun list(view: View) {}
    fun listBondedDevices(view: View) {}
    fun listAvailabelDevices(view: View) {}


}
