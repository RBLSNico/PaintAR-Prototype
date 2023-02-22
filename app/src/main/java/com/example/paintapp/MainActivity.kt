package com.example.paintapp

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

import org.opencv.imgproc.Imgproc
import org.opencv.android.Utils;
import org.opencv.core.*
import org.opencv.core.Scalar
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import org.opencv.core.CvType
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.res.TypedArray
import androidx.appcompat.widget.AppCompatButton
import androidx.gridlayout.widget.GridLayout
import android.content.Context
import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import androidx.annotation.ColorInt
import kotlinx.android.synthetic.main.activity_main.*
import android.app.AlertDialog
import android.content.pm.ActivityInfo
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.paintapp.databinding.ActivityMainBinding
import com.skydoves.colorpickerview.ColorEnvelope
import org.opencv.imgcodecs.Imgcodecs
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


typealias Coordinates = Pair<Point, Point>

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("opencv_java")
        }
    }


    private val imageStack = Stack<Bitmap>()
    private lateinit var originalImage: Bitmap


    lateinit var binding: ActivityMainBinding
    lateinit var toggle: ActionBarDrawerToggle
    var touchCount = 0
    lateinit var tl: Point
    private lateinit var br: Point
    lateinit var bitmap: Bitmap
    var chosenColor = Color.WHITE
//        Scalar(200.0, 0.0, 0.0)

    private val TAG = MainActivity::class.java.simpleName


    private enum class LoadImage {
        PICK_FROM_CAMERA,
        PICK_FROM_GALLERY
    }
//    private val PICK_FROM_CAMERA = 1
//    private val PICK_FROM_GALLERY = 2

    private var texture = false

    private val PERMISSIONS = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            toggle= ActionBarDrawerToggle(this@MainActivity, drawerLayout, R.string.open,R.string.close)
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()

            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            navView.setNavigationItemSelectedListener {

                val textView1 : TextView = findViewById<TextView>(R.id.acquaepoxytextView)
                val textView2 : TextView = findViewById<TextView>(R.id.coolshadestextView)
                val textView3 : TextView = findViewById<TextView>(R.id.healthyhometextView)
                val textView4 : TextView = findViewById<TextView>(R.id.permacoattextView)

                when(it.itemId){
                    R.id.action_acqua_epoxy->{
                        showacquaEpoxy()
                        textView1.text = "BOYSEN® Acqua Epoxy™"
                        acquaepoxytextView.visibility = View.VISIBLE
                        acquascrollView.visibility = View.VISIBLE
                        coolshadestextView.visibility = View.GONE
                        coolshadesscrollView.visibility = View.GONE
                        healthyhometextView.visibility = View.GONE
                        healthyhomescrollView.visibility = View.GONE
                        permacoatscrollView.visibility = View.GONE
                        permacoattextView.visibility = View.GONE
                    }
                    R.id.action_healthy_home->{
                        showhealthyhome()
                        textView3.text = "BOYSEN® Healthy Home™"
                        healthyhometextView.visibility = View.VISIBLE
                        healthyhomescrollView.visibility = View.VISIBLE
                        acquaepoxytextView.visibility = View.GONE
                        acquascrollView.visibility = View.GONE
                        coolshadestextView.visibility = View.GONE
                        coolshadesscrollView.visibility = View.GONE
                        permacoatscrollView.visibility = View.GONE
                        permacoattextView.visibility = View.GONE
                    }
                    R.id.action_cool_shades->{
                        showcoolshades()
                        textView2.text = "BOYSEN® Cool Shades™"
                        coolshadestextView.visibility = View.VISIBLE
                        coolshadesscrollView.visibility = View.VISIBLE
                        healthyhometextView.visibility = View.GONE
                        healthyhomescrollView.visibility = View.GONE
                        acquaepoxytextView.visibility = View.GONE
                        acquascrollView.visibility = View.GONE
                        permacoatscrollView.visibility = View.GONE
                        permacoattextView.visibility = View.GONE

                    }
                    R.id.action_permacoat->{
                        showpermacoat()
                        textView4.text = "BOYSEN® Permacoat™ Latex"
                        permacoatscrollView.visibility = View.VISIBLE
                        permacoattextView.visibility = View.VISIBLE
                        healthyhometextView.visibility = View.GONE
                        healthyhomescrollView.visibility = View.GONE
                        acquaepoxytextView.visibility = View.GONE
                        acquascrollView.visibility = View.GONE
                        coolshadestextView.visibility = View.GONE
                        coolshadesscrollView.visibility = View.GONE

                    }
                }
                true

            }
        }

        tl = Point()
        br = Point()

        openCamera()
        showhealthyhome()

        val textView1 : TextView = findViewById<TextView>(R.id.acquaepoxytextView)
        textView1.setOnClickListener {
            val description = "BOYSEN® Acqua Epoxy™ is a two-component water-based acrylic epoxy paint that has superior solvent, chemical, and stain resistance. It is ideal for concrete floors and properly primed metal surfaces. It has the advantages of easier application and better weather resistance properties."
            val builder = AlertDialog.Builder(this)
            builder.setTitle("BOYSEN® Acqua Epoxy™")
                .setMessage(description)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        val textView2 : TextView = findViewById<TextView>(R.id.coolshadestextView)
        textView2.setOnClickListener {
            val description = "BOYSEN® Cool Shades™ is a water-based, low- VOC paint designed with highly engineered infrared-reflecting (IR) pigments making it possible to create elegant shades of roof coating while reducing heat build-up on painted surfaces. Its 100% acrylic binder provides excellent dirt pick-up resistance and preserves heat-reflective properties over time."
            val builder = AlertDialog.Builder(this)
            builder.setTitle("BOYSEN® Cool Shades™")
                .setMessage(description)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        val textView3 : TextView = findViewById<TextView>(R.id.healthyhometextView)
        textView3.setOnClickListener {
            val description = "BOYSEN® Healthy Home™ is a premium, odor- less, low-VOC, lead-free paint. It is an acrylic water-based interior coating with antibacterial protection to give extra defense against bacteria such as E. coli and Salmonella, as well as mildew and fungus."
            val builder = AlertDialog.Builder(this)
            builder.setTitle("BOYSEN® Healthy Home™")
                .setMessage(description)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        val textView4 : TextView = findViewById<TextView>(R.id.permacoattextView)
        textView4.setOnClickListener {
            val description = "BOYSEN® Permacoat™ Latex is a 100% acrylic paint with excellent hiding and outstanding durability."
            val builder = AlertDialog.Builder(this)
            builder.setTitle("BOYSEN® Permacoat™ Latex")
                .setMessage(description)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }




//        openGallery()

//      acqua expoxy
        whiteButton.setOnClickListener {
            showToast("B-2900 White")
            chosenColor = Color.parseColor("#f6f7f1")
            textView1.text = "B-2900 White"
            texture = false
        }

        grayButton.setOnClickListener {
            showToast("B-2930 Gray")
            chosenColor = Color.parseColor("#585f65")
            textView1.text = "B-2930 Gray"
            texture = false

        }

        handicapblueButton.setOnClickListener {
            showToast("B-2940 Handicap Blue")
            chosenColor = Color.parseColor("#01356f")
            textView1.text = "B-2940 Handicap Blue"
            texture = false

        }

        chromegreenButton.setOnClickListener {
            showToast("B-2950 Chrome Green")
            chosenColor = Color.parseColor("#2c3c22")
            textView1.text = "B-2950 Chrome Green"
            texture = false
        }

        trafficYellowButton.setOnClickListener {
            showToast("B-2965 Traffic Yellow")
            chosenColor = Color.parseColor("#feac00")
            textView1.text = "B-2965 Traffic Yellow"
            texture = false
        }

        safetyorangeButton.setOnClickListener {
            showToast("B-2970 Safety Orange")
            chosenColor = Color.parseColor("#dc2f0f")
            textView1.text = "B-2970 Safety Orange"
            texture = false
        }

//        blackButton.setOnClickListener {
//            showToast("B-2990 Black")
//            chosenColor = Color.parseColor("#0f0e0c")
//            textView1.text = "B-2990 Black"
        texture = false
//        }
//
//        ceramicredButton.setOnClickListener {
//            showToast("B-2976 Ceramic Red")
//            chosenColor = Color.parseColor("#803b1c")
//            textView1.text = "B-2976 Ceramic Red"
        texture = false
//        }

//      cool shades
        reflectingwhiteButton.setOnClickListener {
            showToast("B-8500 Reflecting White")
            chosenColor = Color.parseColor("#fcfbf8")
            textView2.text = "B-8500 Reflecting White"
            texture = false
        }

        shadygrayButton.setOnClickListener {
            showToast("B-8501 Shady Gray")
            chosenColor = Color.parseColor("#dad9db")
            textView2.text = "B-8501 Shady Gray"
            texture = false
        }

        radiantbeigeButton.setOnClickListener {
            showToast("B-8520 Radiant Beige")
            textView2.text = "B-8520 Radiant Beige"
            chosenColor = Color.parseColor("#c8bc99")
            texture = false
        }

        polarizeblueButton.setOnClickListener {
            showToast("B-8547 Polarize Blue")
            textView2.text = "B-8547 Polarize Blue"
            chosenColor = Color.parseColor("#0072ae")
            texture = false
        }

        springgreenButton.setOnClickListener {
            showToast("B-8550 Spring Green")
            textView2.text = "B-8550 Spring Green"
            chosenColor = Color.parseColor("#007256")
            texture = false
        }

        vibrantterracottaButton.setOnClickListener {
            showToast("B-8573 Vibrant Terra Cotta")
            textView2.text = "B-8573 Vibrant Terra Cotta"
            chosenColor = Color.parseColor("#b36747")
            texture = false
        }

        zenbrownButton.setOnClickListener {
            showToast("B-8580 Zen Brown")
            textView2.text = "B-8580 Zen Brown"
            chosenColor = Color.parseColor("#391d19")
            texture = false
        }

//      healthy home
        hygienicwhiteButton.setOnClickListener {
            showToast("B-7410 Hygienic White")
            textView3.text = "B-7410 Hygienic White"
            chosenColor = Color.parseColor("#f6f7f1")
            texture = false
        }

        lighttanButton.setOnClickListener {
            showToast("B-7420 Light Tan")
            textView3.text = "B-7420 Light Tan"
            chosenColor = Color.parseColor("#d6bf93")
            texture = false
        }

        cleanslateButton.setOnClickListener {
            showToast("B-7430 Clean Slate")
            textView3.text = "B-7430 Clean Slate"
            chosenColor = Color.parseColor("#9fa8a7")
            texture = false
        }

        serenelakeButton.setOnClickListener {
            showToast("B-7440 Serene Lake")
            textView3.text = "B-7440 Serene Lake"
            chosenColor = Color.parseColor("#92b2e1")
            texture = false
        }

        freshairButton.setOnClickListener {
            showToast("B-7442 Fresh Air")
            textView3.text = "B-7442 Fresh Air"
            chosenColor = Color.parseColor("#7eb2da")
            texture = false
        }

        floraButton.setOnClickListener {
            showToast("B-7450 Flora")
            textView3.text = "B-7450 Flora"
            chosenColor = Color.parseColor("#a3b787")
            texture = false
        }

        shiningochreButton.setOnClickListener {
            showToast("B-7460 Shining Ochre")
            textView3.text = "B-7460 Shining Ochre"
            chosenColor = Color.parseColor("#f1c062")
            texture = false
        }

        bloomButton.setOnClickListener {
            showToast("B-7470 Bloom")
            textView3.text = "B-7470 Bloom"
            chosenColor = Color.parseColor("#eadcd9")
            texture = false
        }

        rosyButton.setOnClickListener {
            showToast("B-7472 Rosy")
            textView3.text = "B-7472 Rosy"
            chosenColor = Color.parseColor("#ca8f89")
            texture = false
        }
//        HH_8_G99Button.setOnClickListener {
//            showToast("HH-8-G99")
//            textView3.text = "HH-8-G99"
//            chosenColor = Color.parseColor("#fce2bd")
//        }
//
//        HH_9_G81Button.setOnClickListener {
//            showToast("HH-8-G99")
//            textView3.text = "HH-8-G99"
//            chosenColor = Color.parseColor("#f7c495")
//        }
//
//        HH_9_G83Button.setOnClickListener {
//            showToast("HH-9-G83")
//            textView3.text = "HH-9-G83"
//            chosenColor = Color.parseColor("#e7aa67")
//        }
//
//        HH_9_G100Button.setOnClickListener {
//            showToast("HH-9-G100")
//            textView3.text = "HH_9_G100"
//            chosenColor = Color.parseColor("#f7dec0")
//        }
////
//        HH_10_G82Button.setOnClickListener {
//            showToast("HH-10-G82")
//            textView3.text = "HH-10-G82"
//            chosenColor = Color.parseColor("#e0ab75")
//        }
//
//        HH_10_G93Button.setOnClickListener {
//            showToast("HH-10-G93")
//            textView3.text = "HH-10-G93"
//            chosenColor = Color.parseColor("#f6d2b0")
//        }
//
//        HH_10_G96Button.setOnClickListener {
//            showToast("HH-10-G96")
//            textView3.text = "HH-10-G96"
//            chosenColor = Color.parseColor("#eed8c1")
//        }
//
//        HH_9_G100Button.setOnClickListener {
//            showToast("HH-9-G100")
//            textView3.text = "HH_9_G100"
//            chosenColor = Color.parseColor("#f7dec0")
//        }

        //permacoat

        faintsilverButton.setOnClickListener {
            showToast("B-7514 Faint Silver")
            textView4.text = "B-7514 Faint Silver"
            chosenColor = Color.parseColor("#e7e2da")
            texture = false
        }
        bridalwhiteButton.setOnClickListener {
            showToast("B-7509 Bridal White")
            textView4.text = "B-7509 Bridal White"
            chosenColor = Color.parseColor("#eee6d7")
            texture = false
        }
        wintermorningButton.setOnClickListener {
            showToast("B-7502 Winter Morning")
            textView4.text = "B-7502 Winter Morning"
            chosenColor = Color.parseColor("#e9e4dc")
            texture = false
        }
        tullewhiteButton.setOnClickListener {
            showToast("B-7501 Tulle White")
            textView4.text = "B-7501 Tulle White"
            chosenColor = Color.parseColor("#e8e8e0")
            texture = false
        }
        shadediceButton.setOnClickListener {
            showToast("B-7507 Shaded Ice")
            textView4.text = "B-7507 Shaded Ice"
            chosenColor = Color.parseColor("#dddee0")
            texture = false
        }
        coastlightButton.setOnClickListener {
            showToast("B-7504 Coast Light")
            textView4.text = "B-7504 Coast Light"
            chosenColor = Color.parseColor("#fbebd1")
            texture = false
        }

        graycastleButton.setOnClickListener {
            showToast("B-7530 Gray Castle")
            textView4.text = "B-7530 Gray Castle"
            chosenColor = Color.parseColor("#ebe4d9")
            texture = false
        }

        almostwinterButton.setOnClickListener {
            showToast("B-7506 Almost Winter")
            textView4.text = "B-7506 Almost Winter"
            chosenColor = Color.parseColor("#e0ddd4")
            texture = false
        }

        snowfieldButton.setOnClickListener {
            showToast("B-7505 Snow Field")
            textView4.text = "B-7505 Snow Field"
            chosenColor = Color.parseColor("#dbdddc")
            texture = false
        }

        vanillaiceButton.setOnClickListener {
            showToast("B-7511 Vanilla Ice")
            textView4.text = "B-7511 Vanilla Ice"
            chosenColor = Color.parseColor("#f9eedb")
            texture = false
        }

        crispecruButton.setOnClickListener {
            showToast("B-7513 Crisp Ecru")
            textView4.text = "B-7513 Crisp Ecru"
            chosenColor = Color.parseColor("#f8e7cd")
            texture = false
        }

        aquacoolButton.setOnClickListener {
            showToast("B-7541 Aqua Cool")
            textView4.text = "B-7541 Aqua Cool"
            chosenColor = Color.parseColor("#85c7d5")
            texture = false
        }

        amishlinenButton.setOnClickListener {
            showToast("B-7523 Amish Linen")
            textView4.text = "B-7523 Amish Linen"
            chosenColor = Color.parseColor("#ecdcc5")
            texture = false
        }
        pureivoryButton.setOnClickListener {
            showToast("B-7521 Pure Ivory")
            textView4.text = "B-7521 Pure Ivory"
            chosenColor = Color.parseColor("#fcdfbd")
            texture = false
        }
        dappertanButton.setOnClickListener {
            showToast("B-7524 Dapper Tan")
            textView4.text = "B-7524 Dapper Tan"
            chosenColor = Color.parseColor("#d4bea7")
            texture = false
        }
        basicbeigeButton.setOnClickListener {
            showToast("B-7522 Basic Beige")
            textView4.text = "B-7522 Basic Beige"
            chosenColor = Color.parseColor("#ded7c5")
            texture = false
        }
        breezeButton.setOnClickListener {
            showToast("B-7540 Breeze")
            textView4.text = "B-7540 Breeze"
            chosenColor = Color.parseColor("#7fa4d8")
            texture = false
        }
        peachmedleyButton.setOnClickListener {
            showToast("B-7571 Peach Medley")
            textView4.text = "B-7571 Peach Medley"
            chosenColor = Color.parseColor("#eec4ae")
            texture = false
        }

        boneivoryButton.setOnClickListener {
            showToast("B-7525 Bone Ivory")
            textView4.text = "B-7525 Bone Ivory"
            chosenColor = Color.parseColor("#e9d1ab")
            texture = false
        }

        pawnbeigeButton.setOnClickListener {
            showToast("B-7508 Pawn Beige")
            textView4.text = "B-7508 Pawn Beige"
            chosenColor = Color.parseColor("#b99e8b")
            texture = false
        }

        wetsandButton.setOnClickListener {
            showToast("B-7526 Wet Sand")
            textView4.text = "B-7526 Wet Sand"
            chosenColor = Color.parseColor("#bfb39b")
            texture = false
        }

        hintofmintButton.setOnClickListener {
            showToast("B-7551 Hint of Mint")
            textView4.text = "B-7551 Hint of Mint"
            chosenColor = Color.parseColor("#e5efcc")
            texture = false
        }

        nuevoterraButton.setOnClickListener {
            showToast("B-7580 Nuevo Terra")
            textView4.text = "B-7580 Nuevo Terra"
            chosenColor = Color.parseColor("#ce886e")
            texture = false
        }

        yellowwareButton.setOnClickListener {
            showToast("B-7560 Yellow Ware")
            textView4.text = "B-7560 Yellow Ware"
            chosenColor = Color.parseColor("#ffd986")
            texture = false
        }


        archipelagoButton.setOnClickListener {
            showToast("B-7582 Archipelago")
            textView4.text = "B-7582 Archipelago"
            chosenColor = Color.parseColor("#92806c")
            texture = false
        }
        stonebeigeButton.setOnClickListener {
            showToast("B-7520 Stone Beige")
            textView4.text = "BB-7520 Stone Beige"
            chosenColor = Color.parseColor("#baaa90")
            texture = false
        }
        snappygreenButton.setOnClickListener {
            showToast("B-7550 Snappy Green")
            textView4.text = "B-7550 Snappy Green"
            chosenColor = Color.parseColor("#dbdc96")
            texture = false
        }
        oldredwoodButton.setOnClickListener {
            showToast("B-7570 Old Redwood")
            textView4.text = "B-7570 Old Redwood"
            chosenColor = Color.parseColor("#9d5c5a")
            texture = false
        }
        ojayButton.setOnClickListener {
            showToast("B-7566 Ojay")
            textView4.text = "B-7566 Ojay"
            chosenColor = Color.parseColor("#db9e65")
            texture = false
        }
        chocolatekissButton.setOnClickListener {
            showToast("B-7588 Chocolate Kiss")
            textView4.text = "B-7588 Chocolate Kiss"
            chosenColor = Color.parseColor("#705d4d")
            texture = false
        }

        mudpieButton.setOnClickListener {
            showToast("B-7581 Mud Pie")
            textView4.text = "B-7581 Mud Pie"
            chosenColor = Color.parseColor("#8f857b")
            texture = false
        }
        limelillyButton.setOnClickListener {
            showToast("B-7552 Lime Lily")
            textView4.text = "B-7552 Lime Lily"
            chosenColor = Color.parseColor("#ced87f")
            texture = false
        }
        ashtonButton.setOnClickListener {
            showToast("B-7503 Ashton Grey")
            textView4.text = "B-7503 Ashton Grey"
            chosenColor = Color.parseColor("#9c9791")
            texture = false
        }
        mysteryButton.setOnClickListener {
            showToast("B-7512 Mystery Winter")
            textView4.text = "B-7512 Mystery Winter"
            chosenColor = Color.parseColor("#90a691")
            texture = false
        }

        val selectedcolortv : TextView = findViewById(R.id.selectedcolorview)



        viewresultbtn.setOnClickListener {
            showImage()
        }

        takenewphotobtn.setOnClickListener {
            openCamera()
            showhealthyhome()
        }

        val protanopiaMatrix = floatArrayOf(
            0.567f, 0.433f, 0f, 0f, 0f,
            0.558f, 0.442f, 0f, 0f, 0f,
            0f, 0.242f, 0.758f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )

        val deuteranopiaMatrix = floatArrayOf(
            0.625f, 0.375f, 0f, 0f, 0f,
            0.7f, 0.3f, 0f, 0f, 0f,
            0f, 0.3f, 0.7f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )

        val tritanopiaMatrix = floatArrayOf(
            0.95f, 0.05f, 0f, 0f, 0f,
            0f, 0.433f, 0.567f, 0f, 0f,
            0f, 0.475f, 0.525f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )

        val protanopiaFilter = ColorMatrixColorFilter(protanopiaMatrix)
        val deuteranopiaFilter = ColorMatrixColorFilter(deuteranopiaMatrix)
        val tritanopiaFilter = ColorMatrixColorFilter(tritanopiaMatrix)




    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }




    private fun openGallery() {
        val i = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, LoadImage.PICK_FROM_GALLERY.ordinal)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            true
        }

        val textView1 : TextView = findViewById<TextView>(R.id.acquaepoxytextView)
        val textView2 : TextView = findViewById<TextView>(R.id.coolshadestextView)
        val textView3 : TextView = findViewById<TextView>(R.id.healthyhometextView)
        val textView4 : TextView = findViewById<TextView>(R.id.permacoattextView)

        when(item.itemId) {
            R.id.action_undo -> {
                undoImage()
            }
            R.id.action_open_img -> {
                showImage()
            }
            R.id.action_process_image -> {
                showResultLayouts()
            }
            R.id.action_take_photo -> {
                openCamera()
            }
            R.id.action_get_gallery -> {
                openGallery()
            }
            R.id.action_get_color -> {
                chooseColor()
            }
            R.id.action_get_texture -> {
                chooseTexture()
            }
            R.id.action_boysen_color -> {
                chooseBoysen()
            }
            R.id.action_help -> {
                showHelp()
            }
            R.id.action_about -> {
                showAbout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun chooseTexture() {
        texture = true
    }

    private fun chooseBoysen(){
        texture = true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        when (requestCode) {
            LoadImage.PICK_FROM_CAMERA.ordinal -> if (resultCode == Activity.RESULT_OK) {

                try {

//                    bitmap = data?.getExtras()?.get("data") as Bitmap
                    imageFromData.setImageURI(Uri.parse(imageFilePath))
                    val loadedImage = imageFromData.drawable.toBitmap()
                    originalImage = loadedImage


                    // Load the original image from file


                    bitmap = imageFromData.drawable.toBitmap()
                    bitmap = getResizedBitmap(bitmap,bitmap.width/5,bitmap.height/5)
                    showImage()

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            LoadImage.PICK_FROM_GALLERY.ordinal -> if (resultCode == Activity.RESULT_OK) {
                loadFromGallery(data)
            }
        }

        imageFromData.setOnTouchListener(object : View.OnTouchListener {

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                imageStack.push(bitmap.copy(bitmap.config, false))
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (touchCount == 0) {
                        tl.x = event.x.toDouble()
                        tl.y = event.y.toDouble()
//                        touchCount++

                        if(texture) {
                            applyTexture(bitmap, tl)
                        } else {
                            rpPaintHSV(bitmap,tl)
                        }

                    }

                }
                return true
            }
        })

    }

    private fun loadFromGallery(data:Intent?) {
        val selectedImage = data?.data
        val filePathColumn: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = getContentResolver().query(selectedImage!!,filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val picturePath = cursor?.getString(columnIndex!!)
        cursor?.close()

        bitmap = BitmapFactory.decodeFile(picturePath)

        bitmap = getResizedBitmap(bitmap,bitmap.width/5,bitmap.height/5)
        showImage()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LoadImage.PICK_FROM_CAMERA.ordinal -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.e(TAG, "Permission has been denied by user")
                } else {
                    openCamera()
//                    val i =  Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                    startActivityForResult(i, PICK_FROM_GALLERY)
                    Log.e(TAG, "Permission has been granted by user")
                }
            }

        }
    }

    private lateinit var imageFilePath: String

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format( Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        )

        imageFilePath = image.getAbsolutePath()
        return image;
    }

    private fun saveImage(image: Bitmap) {
        val pictureFile = createImageFile()
        if (pictureFile == null) {
            Log.e(TAG, "Error creating media file, check storage permissions: ")
            return
        }
        try {
            val fos = FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found: " + e.message)
        } catch (e: IOException) {
            Log.e(TAG, "Error accessing file: " + e.message)
        }
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, PERMISSIONS, LoadImage.PICK_FROM_CAMERA.ordinal)

        } else {
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this,"com.example.paintapp.provider", photoFile)
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(captureIntent, LoadImage.PICK_FROM_CAMERA.ordinal)
            }

        }
        selectedcolorview.visibility = View.GONE
        selectedcolorview.visibility = View.GONE
        middleLayout.visibility = View.GONE
        viewresultbtn.visibility = View.GONE
        coolshadesscrollView.visibility = View.GONE
        coolshadestextView.visibility = View.GONE
        healthyhomescrollView.visibility = View.GONE
        healthyhometextView.visibility = View.GONE
        acquascrollView.visibility = View.GONE
        acquaepoxytextView.visibility = View.GONE
        takenewphotobtn.visibility = View.VISIBLE
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.getWidth()
        val height = bm.getHeight()
        val scaleWidth = newWidth / width.toFloat()
        val scaleHeight = newHeight / height.toFloat()
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix =  Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)

        return resizedBitmap
    }

    private fun rpPaintHSV(bitmap: Bitmap, p: Point): Mat {
        val cannyMinThres = 30.0
        val ratio = 2.5

        // show intermediate step results
        // grid created here to do that
//        showResultLayouts()
        showImage()

        val mRgbMat = Mat()
        Utils.bitmapToMat(bitmap, mRgbMat)

        showImage(mRgbMat,inputImage)

        Imgproc.cvtColor(mRgbMat,mRgbMat,Imgproc.COLOR_RGBA2RGB)

        val mask = Mat(Size(mRgbMat.width()/8.0, mRgbMat.height()/8.0), CvType.CV_8UC1, Scalar(0.0))
//        Imgproc.dilate(mRgbMat, mRgbMat,mask, Point(0.0,0.0), 5)

        val img = Mat()
        mRgbMat.copyTo(img)

        // grayscale
        val mGreyScaleMat = Mat()
        Imgproc.cvtColor(mRgbMat, mGreyScaleMat, Imgproc.COLOR_RGB2GRAY, 3)
        Imgproc.medianBlur(mGreyScaleMat,mGreyScaleMat,3)


        val cannyGreyMat = Mat()
        Imgproc.Canny(mGreyScaleMat, cannyGreyMat, cannyMinThres, cannyMinThres*ratio, 3)

//        showImage(cannyGreyMat,greyScaleImage)

        //hsv
        val hsvImage = Mat()
        Imgproc.cvtColor(img,hsvImage,Imgproc.COLOR_RGB2HSV)

        //got the hsv values
        val list = ArrayList<Mat>(3)
        Core.split(hsvImage, list)

        val sChannelMat = Mat()
        Core.merge(listOf(list.get(1)), sChannelMat)
        Imgproc.medianBlur(sChannelMat,sChannelMat,3)
//        showImage(sChannelMat,floodFillImage)

        // canny
        val cannyMat = Mat()
        Imgproc.Canny(sChannelMat, cannyMat, cannyMinThres, cannyMinThres*ratio, 3)
//        showImage(cannyMat,HSVImage)

        Core.addWeighted(cannyMat,0.5, cannyGreyMat,0.5 ,0.0,cannyMat)
        Imgproc.dilate(cannyMat, cannyMat,mask, Point(0.0,0.0), 5)

//        showImage(cannyMat,cannyEdgeImage)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val seedPoint = Point(p.x*(mRgbMat.width()/width.toDouble()), p.y*(mRgbMat.height()/height.toDouble()))

        Imgproc.resize(cannyMat, cannyMat, Size(cannyMat.width() + 2.0, cannyMat.height() + 2.0))

        Imgproc.medianBlur(mRgbMat,mRgbMat,15)

        val floodFillFlag = 8
        Imgproc.floodFill(
            mRgbMat,
            cannyMat,
            seedPoint,
            Scalar(Color.red(chosenColor).toDouble(),Color.green(chosenColor).toDouble(),Color.blue(chosenColor).toDouble()),
            Rect(),
            Scalar(5.0, 5.0, 5.0),
            Scalar(5.0, 5.0, 5.0),
            floodFillFlag
        )
//        showImage(mRgbMat,floodFillImage)
        Imgproc.dilate(mRgbMat, mRgbMat, mask, Point(0.0,0.0), 5)

        //got the hsv of the mask image
        val rgbHsvImage = Mat()
        Imgproc.cvtColor(mRgbMat,rgbHsvImage,Imgproc.COLOR_RGB2HSV)

        val list1 = ArrayList<Mat>(3)
        Core.split(rgbHsvImage, list1)

        //merged the "v" of original image with mRgb mat
        val result = Mat()
        Core.merge(listOf(list1.get(0),list1.get(1),list.get(2)), result)

        // converted to rgb
        Imgproc.cvtColor(result, result, Imgproc.COLOR_HSV2RGB)

        Core.addWeighted(result,0.7, img,0.3 ,0.0,result )

        showImage(result,imageFromData)
        return result


    }


    private fun showImage(image: Mat, view: ImageView) {
        val mBitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, mBitmap)
        view.setImageBitmap(mBitmap)

        bitmap = mBitmap
        saveImage(bitmap)
    }

    private fun showResultLayouts() {
        imageFromData.visibility = View.GONE

//       topLayout.visibility = View.VISIBLE
        selectedcolorview.visibility = View.VISIBLE
        middleLayout.visibility = View.VISIBLE
        viewresultbtn.visibility = View.VISIBLE
        acquascrollView.visibility = View.GONE
        acquaepoxytextView.visibility = View.GONE
        coolshadesscrollView.visibility = View.GONE
        coolshadestextView.visibility = View.GONE
        healthyhomescrollView.visibility = View.GONE
        healthyhometextView.visibility = View.GONE
//        bottomLayout.visibility = View.VISIBLE
    }

    private fun showselectedcolor() {

    }

    private fun showImage() {
        imageFromData.visibility = View.VISIBLE

        try {
            imageFromData.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "No image selected",Toast.LENGTH_SHORT).show()
        }
    }

    private fun undoImage() {
        if (!imageStack.empty()) {
            // Retrieve previous image from the stack and set as current image
            val previousImage = imageStack.pop()
            imageFromData.setImageBitmap(previousImage)
        }
    }

    private fun resetImage(){
        bitmap = originalImage
        imageFromData.setImageBitmap(bitmap)
    }


    private fun previousImage(): Bitmap? {
        return if (!imageStack.empty()) {
            imageStack.peek()
        } else {
            null
        }
    }

    fun applyColorBlindnessFilters(inputFilePath: String){
    }

    private fun showacquaEpoxy () {
        acquascrollView.visibility = View.VISIBLE
        acquaepoxytextView.visibility = View.VISIBLE
        coolshadesscrollView.visibility = View.GONE
        coolshadestextView.visibility = View.GONE
        healthyhomescrollView.visibility = View.GONE
        healthyhometextView.visibility = View.GONE
        selectedcolorview.visibility = View.GONE
        permacoattextView.visibility = View.GONE
        permacoatscrollView.visibility = View.GONE
    }

    private fun showcoolshades () {
        coolshadesscrollView.visibility = View.VISIBLE
        coolshadestextView.visibility = View.VISIBLE
        acquascrollView.visibility = View.GONE
        acquaepoxytextView.visibility = View.GONE
        healthyhomescrollView.visibility = View.GONE
        healthyhometextView.visibility = View.GONE
        selectedcolorview.visibility = View.GONE
        permacoattextView.visibility = View.GONE
        permacoatscrollView.visibility = View.GONE

    }

    private fun showhealthyhome () {
        healthyhomescrollView.visibility = View.VISIBLE
        healthyhometextView.visibility = View.VISIBLE
        coolshadesscrollView.visibility = View.GONE
        coolshadestextView.visibility = View.GONE
        acquascrollView.visibility = View.GONE
        acquaepoxytextView.visibility = View.GONE
        selectedcolorview.visibility = View.GONE
        permacoattextView.visibility = View.GONE
        permacoatscrollView.visibility = View.GONE

    }

    private fun showpermacoat() {
        permacoattextView.visibility = View.VISIBLE
        permacoatscrollView.visibility = View.VISIBLE
        healthyhomescrollView.visibility = View.GONE
        healthyhometextView.visibility = View.GONE
        coolshadesscrollView.visibility = View.GONE
        coolshadestextView.visibility = View.GONE
        acquascrollView.visibility = View.GONE
        acquaepoxytextView.visibility = View.GONE
        selectedcolorview.visibility = View.GONE
    }

//    private fun chooseColor() {
//        texture = false
//
//        val colorPicker = AmbilWarnaDialog(this@MainActivity, chosenColor, object: AmbilWarnaDialog.OnAmbilWarnaListener {
//
//            override fun onCancel(dialog: AmbilWarnaDialog) {
//            }
//
//            override fun onOk(dialog: AmbilWarnaDialog ,color: Int) {
//                chosenColor = color
//            }
//        })
//
//        colorPicker.show()
//    }

    private fun chooseColor() {
        texture = false

        val builder = ColorPickerDialog.Builder(this)
            .setTitle("Choose Color")
            .setPositiveButton("Ok", object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    chosenColor = envelope?.color ?: chosenColor
                    val hexCode = envelope?.hexCode ?: "#000000"
                    Toast.makeText(this@MainActivity, "Selected color: $hexCode", Toast.LENGTH_SHORT).show()
                    val textView1 : TextView = findViewById<TextView>(R.id.acquaepoxytextView)
                    val textView2 : TextView = findViewById<TextView>(R.id.healthyhometextView)
                    val textView4 : TextView = findViewById<TextView>(R.id.permacoattextView)
                    val textView3 : TextView = findViewById<TextView>(R.id.coolshadestextView)
                    textView1.text = "Color: #$hexCode"
                    textView2.text = "Color: #$hexCode"
                    textView3.text = "Color: #$hexCode"
                    textView4.text = "Color: #$hexCode"

                    textView1.setOnClickListener {
                        val description = "Custom color is selected"
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle("Custom color is currently selected.")
                            .setMessage(description)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                    textView2.setOnClickListener {
                        val description = "Custom color is currently selected."
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle("Custom Color")
                            .setMessage(description)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                    textView3.setOnClickListener {
                        val description = "Custom color is currently selected."
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle("Custom Color")
                            .setMessage(description)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                    textView4.setOnClickListener {
                        val description = "Custom color is currently selected."
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle("Custom Color")
                            .setMessage(description)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .show()
                    }


                }
            })
            .setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)

            .show()
    }





    private fun applyTexture(bitmap: Bitmap, p: Point) {
        val cannyMinThres = 30.0
        val ratio = 2.5

        // show intermediate step results
        // grid created here to do that
//        showResultLayouts()
        showImage()

        val mRgbMat = Mat()
        Utils.bitmapToMat(bitmap, mRgbMat)

        showImage(mRgbMat,inputImage)

        Imgproc.cvtColor(mRgbMat,mRgbMat,Imgproc.COLOR_RGBA2RGB)

        val mask = Mat(Size(mRgbMat.width()/8.0, mRgbMat.height()/8.0), CvType.CV_8UC1, Scalar(0.0))
//        Imgproc.dilate(mRgbMat, mRgbMat,mask, Point(0.0,0.0), 5)

        val img = Mat()
        mRgbMat.copyTo(img)

        // grayscale
        val mGreyScaleMat = Mat()
        Imgproc.cvtColor(mRgbMat, mGreyScaleMat, Imgproc.COLOR_RGB2GRAY, 3)
        Imgproc.medianBlur(mGreyScaleMat,mGreyScaleMat,3)


        val cannyGreyMat = Mat()
        Imgproc.Canny(mGreyScaleMat, cannyGreyMat, cannyMinThres, cannyMinThres*ratio, 3)

//        showImage(cannyGreyMat,greyScaleImage)

        //hsv
        val hsvImage = Mat()
        Imgproc.cvtColor(img,hsvImage,Imgproc.COLOR_RGB2HSV)

        //got the hsv values
        val list = ArrayList<Mat>(3)
        Core.split(hsvImage, list)

        val sChannelMat = Mat()
        Core.merge(listOf(list.get(1)), sChannelMat)
        Imgproc.medianBlur(sChannelMat,sChannelMat,3)
//        showImage(sChannelMat,floodFillImage)

        // canny
        val cannyMat = Mat()
        Imgproc.Canny(sChannelMat, cannyMat, cannyMinThres, cannyMinThres*ratio, 3)
//        showImage(cannyMat,HSVImage)

        Core.addWeighted(cannyMat,0.5, cannyGreyMat,0.5 ,0.0,cannyMat)
        Imgproc.dilate(cannyMat, cannyMat,mask, Point(0.0,0.0), 5)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val seedPoint = Point(p.x*(mRgbMat.width()/width.toDouble()), p.y*(mRgbMat.height()/height.toDouble()))

        Imgproc.resize(cannyMat, cannyMat, Size(cannyMat.width() + 2.0, cannyMat.height() + 2.0))
        val cannyMat1 = Mat()
        cannyMat.copyTo(cannyMat1)

//        Imgproc.medianBlur(mRgbMat,mRgbMat,15)

        val wallMask = Mat(mRgbMat.size(),mRgbMat.type())

        val floodFillFlag = 8
        Imgproc.floodFill(
            wallMask,
            cannyMat,
            seedPoint,
            Scalar(255.0,255.0,255.0/*chosenColor.toDouble(),chosenColor.toDouble(),chosenColor.toDouble()*/),
            Rect(),
            Scalar(5.0, 5.0, 5.0),
            Scalar(5.0, 5.0, 5.0),
            floodFillFlag
        )
//        showImage(wallMask,greyScaleImage)
//
//        showImage(cannyMat,cannyEdgeImage)

        //second floodfill is not working 5
        Imgproc.floodFill(
            mRgbMat,
            cannyMat1,
            seedPoint,
            Scalar(0.0,0.0,0.0/*chosenColor.toDouble(),chosenColor.toDouble(),chosenColor.toDouble()*/),
            Rect(),
            Scalar(5.0, 5.0, 5.0),
            Scalar(5.0, 5.0, 5.0),
            floodFillFlag
        )
//        showImage(mRgbMat,HSVImage)

        val texture = getTextureImage()

        val textureImgMat = Mat()
        Core.bitwise_and(wallMask ,texture,textureImgMat)

//        showImage(textureImgMat,floodFillImage)

        val resultImage = Mat()
        Core.bitwise_or(textureImgMat,mRgbMat,resultImage)

        showImage(resultImage,outputImage)

        ////alpha blending

        //got the hsv of the mask image
        val rgbHsvImage = Mat()
        Imgproc.cvtColor(resultImage,rgbHsvImage,Imgproc.COLOR_RGB2HSV)

        val list1 = ArrayList<Mat>(3)
        Core.split(rgbHsvImage, list1)

        //merged the "v" of original image with mRgb mat
        val result = Mat()
        Core.merge(listOf(list1.get(0),list1.get(1),list.get(2)), result)

        // converted to rgb
        Imgproc.cvtColor(result, result, Imgproc.COLOR_HSV2RGB)

        Core.addWeighted(result,0.8, img,0.2 ,0.0,result )

        showImage(result,outputImage)
    }

    private fun getTextureImage(): Mat {
        var textureImage = BitmapFactory.decodeResource(getResources(), R.drawable.texture_small_brick_red)
        textureImage = getResizedBitmap(textureImage,bitmap.width,bitmap.height)
        val texture = Mat()
        Utils.bitmapToMat(textureImage,texture)
        Imgproc.cvtColor(texture,texture,Imgproc.COLOR_RGBA2RGB)
        return texture
    }

    private fun showHelp() {
        val description = "PaintAR allows you to easily visualize paint colors with just a few taps. Here's how to get started: \n" +
                "\n" +
                "1. Select your desired color from the available selections. \n" +
                "\n" +
                "2. Tap on the target wall once. This will automatically display the selected color on the wall. \n" +
                "\n" +
                "3. If you wish to select another color, tap on the wall again. This will overlay the initially selected color on the wall.\n" +
                "\n" +
                "\n" +
                "And that's all you need to know to get started with visualizing paint colors with PaintAR!"
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Help")
            .setMessage(description)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showAbout(){
        val description = "Developed by National University (NU) Laguna students for study related purposes, the PaintAR Wall Paint Visualizer mobile application is an augmented reality-based application that allows users to visualize different paint colors on their walls. With a general objective of bringing convenience and ease to the painting process, PaintAR offers an interactive and efficient way for users to see how their walls will look before committing to a color."
        val builder = AlertDialog.Builder(this)
        builder.setTitle("About")
            .setMessage(description)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getTextureImage2(): Mat {
        var textureImage = BitmapFactory.decodeResource(getResources(), R.drawable.black)
        textureImage = getResizedBitmap(textureImage,bitmap.width,bitmap.height)
        val texture = Mat()
        Utils.bitmapToMat(textureImage,texture)
        Imgproc.cvtColor(texture,texture,Imgproc.COLOR_RGBA2RGB)
        return texture
    }

}
