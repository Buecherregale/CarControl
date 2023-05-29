package de.buecherregale.carcontrol.activities

import android.content.Intent
import android.net.InetAddresses
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import de.buecherregale.carcontrol.R
import java.io.File



class MainActivity : AppCompatActivity() {

    data class Config(val ip: String, val port: Int)

    private val cfgName: String = "cfg.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get the text fields
        val ipText = findViewById<TextView>(R.id.ip)
        val portText = findViewById<TextView>(R.id.port)

        // read / write config
        val file = File(filesDir, cfgName)
        // check if cfg already present
        val gson = Gson()
        if(file.exists()) {
            // read existing cfg
            val cfgContents = file.inputStream().bufferedReader().use {
                it.readText()
            }
            val cfg: Config = gson.fromJson(cfgContents, Config::class.java)
            ipText.text = cfg.ip
            portText.text = cfg.port.toString()
        }

        // get button
        val continueBtn = findViewById<Button>(R.id.continueBtn)
        continueBtn.setOnClickListener {
            // write the text to the cfg
            val ip = ipText.text.toString()
            val port = portText.text.toString()
            // validation
            if(validate(ip, port)) {
                writeCfg(ip, port.toInt())
                // open next activity
                val intent = Intent(this, TiltControl::class.java)
                intent.putExtra("ip", ip)
                intent.putExtra("port", port)
                startActivity(intent)
            }
        }

        // clear the default strings when entering
        ipText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) return@OnFocusChangeListener
            if(ipText.text.toString() == getString(R.string.ip_address)) {
                ipText.text = ""
            }
        }
        portText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) return@OnFocusChangeListener
            if(portText.text.toString() == getString(R.string.port)) {
                portText.text = ""
            }
        }
    }

    private fun validate(ip: String, port: String) : Boolean {
        var validIP = true
        if(!InetAddresses.isNumericAddress(ip)) {

            val ipText = findViewById<TextView>(R.id.ip)
            ipText.error = getString(R.string.err_invalid_ip)
            validIP = false
        }

        val portInt = port.toIntOrNull()
        if((portInt == null) || (portInt !in (0..65535))) {

            val portText = findViewById<TextView>(R.id.port)
            portText.error = getString(R.string.err_invalid_port)
            return false
        }
        return validIP
    }

    private fun writeCfg(ip: String, port: Int) {
        val gson = Gson()
        val file = File(filesDir, cfgName)

        val cfg = Config(ip, port)

        file.printWriter().use {
            it.write(gson.toJson(cfg))
        }
    }
}