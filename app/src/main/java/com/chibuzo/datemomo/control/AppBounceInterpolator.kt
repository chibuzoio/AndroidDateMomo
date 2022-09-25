package com.chibuzo.datemomo.control

import android.view.animation.Interpolator
import kotlin.math.cos
import kotlin.math.pow

class AppBounceInterpolator(amplitude: Double, frequency: Double) :
    Interpolator {
    private var amplitude: Double = 1.0
    private var frequency: Double = 10.0

    override fun getInterpolation(time: Float): Float {
        return (-1 * Math.E.pow(-time / this.amplitude) *
                cos(this.frequency * time) + 1).toFloat()
    }

    init {
        this.amplitude = amplitude
        this.frequency = frequency
    }
}


