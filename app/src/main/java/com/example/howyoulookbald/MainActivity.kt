package com.example.howyoulookbald

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.*
import android.view.View
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.*
import java.lang.UnsupportedOperationException;
import kotlinx.android.synthetic.main.activity_main.*
import android.graphics.drawable.BitmapDrawable
import android.hardware.*
import android.media.FaceDetector
import android.os.Vibrator
import android.util.SparseArray
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val REQUEST_TAKE_PHOTO = 1
    private var swap1IMGFileName: String = ""
    private var swapBitmap: Bitmap? = null
    private var swapFaces: SparseArray<Face>?=null
    private var detector : com.google.android.gms.vision.face.FaceDetector?= null
    private var swapMult=5
    private var shaken=false
    private var sensor:Sensor?=null
    private var sensorManager:SensorManager?=null
    private var xOld=0
    private var yOld=0
    private var zOld=0
    private var threadShould=3000
    private var oldtime:Long=0
    private val manager=supportFragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager=getSystemService(Context.SENSOR_SERVICE)as SensorManager
        sensor=sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        detector= com.google.android.gms.vision.face.FaceDetector.Builder(applicationContext)
            .setTrackingEnabled(false)
            .setLandmarkType(com.google.android.gms.vision.face.FaceDetector.ALL_LANDMARKS)
            .setClassificationType(com.google.android.gms.vision.face.FaceDetector.ALL_CLASSIFICATIONS)
            .build()
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
    override fun onSensorChanged(event: SensorEvent?) {
        var x= event!!.values[0]
        var y= event!!.values[1]
        var z= event!!.values[2]
        var currentTime = System.currentTimeMillis()
        if((currentTime-oldtime)>100) {
            var timeDiff=currentTime-oldtime
            oldtime=currentTime
            var speed=Math.abs(x+y+z-xOld-yOld-zOld)/timeDiff*10000
            if(speed>threadShould){
                var v =getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(500)
                shaken=true
                layout1.addView(Canvass(this))
            }
        }
    }
    fun btnTakePicClick(view:View) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            swapBitmap = data!!.extras.get("data") as Bitmap
            //imgP1.setImageBitmap(imageBitmap)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            swap1IMGFileName = timeStamp
            val file = File(Environment.getExternalStorageDirectory().toString(), "${timeStamp}.jpg")
            try {
                val stream: OutputStream = FileOutputStream(file)
                swapBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()
                Toast.makeText(applicationContext, "Image saved successful.", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) { // Catch the exception
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error to save image.", Toast.LENGTH_SHORT).show()
            }
            //imgSwap.setImageURI(Uri.parse(file.absolutePath))
            galleryAddPic(file)
            if(!detector!!.isOperational)
            {
                Toast.makeText(applicationContext, "Detector is not operational.", Toast.LENGTH_SHORT).show()
            }
            val swapFrame = Frame.Builder().setBitmap(swapBitmap).build()
            swapFaces = detector!!.detect(swapFrame)
            layout1.background=null
            layout1.addView(Canvass(this))

        }
    }
    private fun galleryAddPic(f : File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }
    override fun onSaveInstanceState(outState: Bundle? ) {
        super.onSaveInstanceState(outState)
        outState!!.putString("SWIMGPATH",swap1IMGFileName )
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        val path1 = savedInstanceState!!.getString("SWIMGPATH")
        if(path1!=""){
            val file = File(Environment.getExternalStorageDirectory().toString(), "$path1.jpg")
            swap1IMGFileName=path1!!
            swapBitmap=BitmapFactory.decodeFile(file.absolutePath)
            if(swapBitmap!=null) {
                val swapFrame = Frame.Builder().setBitmap(swapBitmap).build()
                swapFaces = detector!!.detect(swapFrame)
                swapMult=5
                //layout1.removeAllViews()
                layout1.addView(Canvass(this))
            }
        }
    }
    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }
    internal inner class Canvass (context: Context): View (context) {
        override fun onDraw (canvas: Canvas) {
            if (shaken){
                val swapPaint = Paint()
                swapPaint.strokeWidth = 5.0.toFloat()
                swapPaint.color= Color.RED
                swapPaint.style=Paint.Style.STROKE
                val oriBitmap=Rect(0,0,swapBitmap!!.width , swapBitmap!!.height)
                val swapRect=Rect(0,0,swapBitmap!!.width*swapMult,swapBitmap!!.height*swapMult)
                var copyBitmap=swapBitmap!!.copy(swapBitmap!!.config,true)
                if(swapFaces!!.size()==2) {
                    /*val face1 = swapFaces!!.valueAt(0)
                    var XsF1= IntArray(face1.landmarks.size)
                    var YsF1= IntArray(face1.landmarks.size)
                    var index=0
                    for(landmark in face1.landmarks ){
                        val cx =(landmark.position.x )
                        val cy =(landmark.position.y)
                        XsF1[index]=cx.toInt()
                        YsF1[index]=cy.toInt()
                        index+=1
                    }
                    val face2 = swapFaces!!.valueAt(1)
                    var XsF2= IntArray(face2.landmarks.size)
                    var YsF2= IntArray(face2.landmarks.size)
                    index=0
                    for(landmark in face2.landmarks ){
                        val cx =(landmark.position.x )
                        val cy =(landmark.position.y)
                        XsF2[index]=cx.toInt()
                        YsF2[index]=cy.toInt()
                        index+=1
                    }
                    var indexI=0
                    var indexJ=0
                    for(j in (YsF2.min()!!-12) until (YsF2.max()!!+8) ) {
                        for(i in (XsF2.min()!!-10) until (XsF2.max()!!+10) ) {
                            //val face1Pix=swapBitmap!!.getPixel((XsF1.min()!!-10)+indexI,(YsF1.min()!!-12)+indexJ)
                            copyBitmap!!.setPixel((XsF1.min()!!-10)+indexI,(YsF1.min()!!-12)+indexJ,swapBitmap!!.getPixel(i,j))
                            copyBitmap!!.setPixel(i,j,swapBitmap!!.getPixel((XsF1.min()!!-10)+indexI,(YsF1.min()!!-12)+indexJ))
                            indexI+=1
                        }
                        indexJ+=1
                        indexI=0*/
                    val face1 = swapFaces!!.valueAt(0)
                    val face2 = swapFaces!!.valueAt(1)
                    for(landmarkF1 in face1.landmarks ){
                        for(landmarkF2 in face2.landmarks ){
                            if(landmarkF1.type==landmarkF2.type){
                                for(i in -12 until 12){
                                    for(j in -5 until 5)
                                    {
                                        copyBitmap!!.setPixel(landmarkF1.position.x.toInt()+i,landmarkF1.position.y.toInt()+j,swapBitmap!!.getPixel(landmarkF2.position.x.toInt()+i,landmarkF2.position.y.toInt()+j))
                                        copyBitmap!!.setPixel(landmarkF2.position.x.toInt()+i,landmarkF2.position.y.toInt()+j,swapBitmap!!.getPixel(landmarkF1.position.x.toInt()+i,landmarkF1.position.y.toInt()+j))
                                    }
                                }
                            }
                        }
                    }
                    canvas.drawBitmap(copyBitmap!!,oriBitmap,swapRect, null)
                }
                else{
                    Toast.makeText(applicationContext, "Warning: 2 faces should be present in the image not ${swapFaces!!.size()}.", Toast.LENGTH_SHORT).show()
                    canvas.drawBitmap(copyBitmap!!,oriBitmap,swapRect, null)
                }
                shaken=false
            }
            else
            {
                val oriBitmap=Rect(0,0,swapBitmap!!.width , swapBitmap!!.height)
                val swapRect=Rect(0,0,swapBitmap!!.width*swapMult,swapBitmap!!.height*swapMult)
                canvas.drawBitmap(swapBitmap!!,oriBitmap,swapRect, null)
            }

        }
    }
}
