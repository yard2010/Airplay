package com.example.airplay

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.math.abs
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener  {

    private lateinit var sensorManager: SensorManager

    private var gravity = FloatArray(3) { 0f }
    private var velocity = FloatArray(3) { 0f }
    private var position = FloatArray(3) { 0f }
    private var acceleration = FloatArray(3) { 0f }

    private val series = LineGraphSeries<DataPoint>()
    private var currentX = 0.0;

    private var lastTime = 0L

    private var shouldReset = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lastTime = System.nanoTime();

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)

        val graph = findViewById<GraphView>(R.id.graph)
        graph.viewport.setMaxX(50.0)
        graph.viewport.isXAxisBoundsManual = true
        graph.addSeries(this.series)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {

            // TODO: use dt to make this change consistent
            val dt = System.nanoTime() - lastTime;

            val xAccel: Float? = event.values?.getOrNull(0)
            val yAccel: Float? = event.values?.getOrNull(1)
            val zAccel: Float? = event.values?.getOrNull(2)

            val alpha = 0;
            acceleration[0] = acceleration[0] * alpha + xAccel!! * (1 - alpha)
            acceleration[1] = acceleration[1] * alpha + yAccel!! * (1 - alpha)
            acceleration[2] = acceleration[2] * alpha + zAccel!! * (1 - alpha)

            val currentAcceleration = sqrt(
                acceleration[0] * acceleration[0]
                    + acceleration[1] * acceleration[1]
                    + acceleration[2] * acceleration[2]
            )
            if (abs(currentAcceleration) > 2) {
                currentX += 0.1;
                this.findViewById<TextView>(R.id.accelTextView).text = "Accel: $currentAcceleration"
                this.series.appendData(DataPoint(currentX, currentAcceleration.toDouble()), true, 200000)
            }

            // TODO: Might be unnecessary
            velocity[0] = velocity[0] * alpha + (xAccel - gravity[0]) * (1 - alpha)
            velocity[1] = velocity[1] * alpha + (yAccel - gravity[1]) * (1 - alpha)
            velocity[2] = velocity[2] * alpha + (zAccel - gravity[2]) * (1 - alpha)

            position[0] += velocity[0]
            position[1] += velocity[1]
            position[2] += velocity[2]

            if (abs(velocity[0]) > 0.5) {
                this.findViewById<TextView>(R.id.xTextView).text = "X: ${(position[0] * 10)}"
            }
            if (abs(velocity[1]) > 0.5) {
                this.findViewById<TextView>(R.id.yTextView).text = "Y: ${(position[1] * 10)}"
            }
            if (abs(velocity[2]) > 0.5) {
                this.findViewById<TextView>(R.id.zTextView).text = "Z: ${(position[2] * 10)}"
            }
        }

    }

    fun handleButtonClick(view: View) {
        shouldReset = true;
        velocity = FloatArray(3) { 0f }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

