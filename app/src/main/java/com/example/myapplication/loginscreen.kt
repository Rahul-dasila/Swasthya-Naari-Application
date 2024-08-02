package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

object SocketManager {
    var client: Socket? = null
}
class loginscreen : AppCompatActivity() {
    private val address = "192.168.216.102"
    private val port = 5000
    private lateinit var writer : PrintWriter
    private lateinit var reader: BufferedReader
    private lateinit var client : Socket
    private lateinit var signin : TextView
    private lateinit var usrname : TextInputEditText
    private lateinit var pass : TextInputEditText
    private lateinit var usn : String
    private lateinit var pss : String
    private lateinit var userInput : TextInputLayout
    private lateinit var userpass : TextInputLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginscreen)
        signin = findViewById(R.id.textView5)
        usrname = findViewById(R.id.userName)
        pass = findViewById(R.id.password)
        userInput = findViewById(R.id.UserInput)
        userpass = findViewById(R.id.userPass)
        val clk = AnimationUtils.loadAnimation(this,R.anim.clicking)
        var flag = false
        CoroutineScope(Dispatchers.IO).launch {
            flag =  connectStart(address,port)
        }
        signin.setOnClickListener {
            signin.startAnimation(clk)
            usn = usrname.text.toString()
            pss = pass.text.toString()
            getIdPass(usn , pss)
            if(usn.length == 9 && pss.length >= 5) {
                if (flag) {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (sendData(writer)) {
                            CoroutineScope(Dispatchers.IO).launch {
                                retrieveData(reader)
                            }
                        }
                    }
                } else {
                    Toast.makeText(this,"SERVER IS SHUT DOWN..",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun connectStart(address: String, port: Int):Boolean{
        var flag = 0
        withContext(Dispatchers.IO){
            try{
                client = Socket(address,port)
                SocketManager.client = client
                if(client.isConnected){
                    withContext(Dispatchers.Main){
                        flag =1
                        Toast.makeText(this@loginscreen, "Connection done", Toast.LENGTH_SHORT).show()

                    }
                }else{
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@loginscreen, "Connection failed", Toast.LENGTH_SHORT).show()
                    }
                }
                writer = PrintWriter(OutputStreamWriter(client.getOutputStream()),true)
                reader = BufferedReader(InputStreamReader(client.getInputStream()))
            }
            catch (e:Exception){
                println(e.stackTrace)
            }
        }
        return flag != 0
    }



    private fun getIdPass(id1 : String , pass1 : String){
        if(id1.length == 9){
            usn = id1.lowercase()
        }else
        {
            userInput.error = "Invalid Id"
            usn = "0"
        }
        if(pass1.length < 6){
            userpass.error = "Minimum password length is 5"
            pss =  "0"
        }
        else{
            pss = pass1.lowercase()
        }
    }




    private suspend fun sendData(writer :PrintWriter):Boolean{
        withContext(Dispatchers.IO){
            writer.println(usn)
            writer.flush()
        }
        withContext(Dispatchers.IO){
            writer.println(pss)
            writer.flush()
        }
        return true
    }



    private suspend fun retrieveData(reader: BufferedReader){

        withContext(Dispatchers.IO){
            val prm = reader.readLine()
            if(prm=="true"){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@loginscreen, "Access Granted", Toast.LENGTH_SHORT).show()
                    val name : String
                    withContext(Dispatchers.IO){
                        name = reader.readLine()
                    }
                    val it = Intent(this@loginscreen,MainActivity::class.java)
                    it.putExtra("adminName",name)
                    startActivity(it)
                }
            }
            else if(prm == "false"){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@loginscreen, "Access Denied", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                withContext(Dispatchers.Main){
                    Toast.makeText(this@loginscreen, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}