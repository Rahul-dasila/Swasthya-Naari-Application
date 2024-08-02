package com.example.myapplication

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOError
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var img : ImageView
    private lateinit var username : TextInputEditText
    private lateinit var adharnumber : TextInputEditText
    private lateinit var getdata : TextView
    private lateinit var Eage : TextInputEditText
    private lateinit var Egender : TextInputEditText
    private lateinit var Enumber : TextInputEditText
    private lateinit var cBox : CheckBox
    private var userN = "NULL"
    private var adhrN = "NULL"
    private var uAge: String = "NULL"
    private var yGender = "NULL"
    private var uNumber = "NULL"
    private var checked = "F"
    private var cStock = "5"
    private var pStock = "5"
    private lateinit var aName : TextView
    private lateinit var socket : Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        socket = SocketManager.client!!
        img = findViewById(R.id.imageView)
        val adminName = intent.getStringExtra("adminName")
        img.setImageResource(R.drawable.addimage)
        username = findViewById(R.id.username)
        adharnumber = findViewById(R.id.adharnumber)
        Eage = findViewById(R.id.age)
        Enumber = findViewById(R.id.pnumber)
        Egender = findViewById(R.id.gender)
        getdata = findViewById(R.id.getdata)
        aName = findViewById(R.id.aName)
        cBox = findViewById(R.id.checkBox)
        cBox.setOnClickListener {
            checked = if(cBox.isChecked){
                "T"
            }else{
                "F"
            }
        }


        val clk = AnimationUtils.loadAnimation(this,R.anim.clicking)

        aName.text = "Hello $adminName"
        getdata.setOnClickListener {
            getdata.startAnimation(clk)
            userN = username.text.toString()
            adhrN = adharnumber.text.toString()
            uAge = Eage.text.toString()
            yGender = Egender.text.toString().lowercase()
            uNumber = Enumber.text.toString()
            var flg = false
            if(yGender=="m" || yGender=="f"){
                flg = true
            }
            if (userN =="NULL" || adhrN.length !=12  || uAge.length != 2 || yGender =="NULL" || !flg || uNumber.length!=10) {
                // Show error message
                 Toast.makeText(this, "Please fill the all fields correctly", Toast.LENGTH_SHORT).show()
            } else {
                // All fields are filled, proceed with sending data
//                Toast.makeText(this, userN, Toast.LENGTH_SHORT).show()
                ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        img.setImageURI(data?.data)
        val uri = data?.data
        uri?.let {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
            CoroutineScope(IO).launch{
                sendImageToServer(bitmap)
            }
            if(socket.isConnected){
                Toast.makeText(this,"Successfully added new user... ",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Server error",Toast.LENGTH_SHORT).show()
            }

        }
    }


    private suspend fun sendImageToServer(bitmap: Bitmap) {
        withContext(IO) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                if (socket != null) {
                    if (socket.isConnected) {
                            val dt: String = "123$adhrN$uAge$yGender$uNumber$checked$userN"
                            val dt2 = dt.toByteArray(Charsets.UTF_8)
                            CoroutineScope(Dispatchers.IO).launch {
                                sendData(dt2,socket)
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                               sendImage(byteArray,socket)
                            }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
            }
        }
    }


    private suspend fun sendData(str : ByteArray,socket : Socket):Boolean{
        withContext(Dispatchers.IO){
            val writer2 = socket.getOutputStream()
            writer2.write(str)
            writer2.flush()
        }
        return true
    }


    private suspend fun sendImage(byteArray: ByteArray,socket: Socket) {
        withContext(Dispatchers.IO) {
            val outputStream = BufferedOutputStream(socket.getOutputStream())
            outputStream.write(byteArray)
            outputStream.flush()
            val writer2 = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)
            writer2.print("stop")
            writer2.flush()
//            outputStream.close()
        }
    }

}
