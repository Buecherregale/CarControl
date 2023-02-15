package de.buecherregale.carcontrol.listener

import android.text.Editable
import android.text.TextWatcher

abstract class TextEditListener : TextWatcher {
    override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(text: Editable?) {}
}