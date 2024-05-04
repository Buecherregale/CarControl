package de.buecherregale.carcontrol.exception

import android.app.Activity
import android.util.Log
import android.widget.TextView
import de.buecherregale.carcontrol.R
import java.lang.Thread.UncaughtExceptionHandler

class ExceptionHandler(private val activity: Activity): UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val endl = "\n"
        val errorWriter = StringBuilder()
        errorWriter.append("Exception in Thread: $thread$endl")
        errorWriter.append("Exception caused by: ${throwable.cause}$endl")
        errorWriter.append("Type of Exception: ${throwable.javaClass.simpleName}$endl")
        throwable.printStackTrace()

        Log.d("App", "handled error: \n$errorWriter")
    }
}