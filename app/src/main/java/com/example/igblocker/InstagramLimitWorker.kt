package com.example.igblocker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class InstagramLimitWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        // Ova klasa se više ne koristi u novom sustavu inteligentnog blokiranja
        return Result.success()
    }
}
