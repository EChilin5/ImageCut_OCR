package com.eachilin.imagecut

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.text.Html
import android.util.Log
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import com.eachilin.imagecut.activity.MainActivity
import com.eachilin.imagecut.databinding.ActivityCaptureTakenimageBinding
import com.eachilin.imagecut.models.Post
import com.eachilin.imagecut.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.*


private const val TAG = "CaptureTakenimageActivity"
private const val PICK_PHOTO_CODE = 1234
private lateinit var photoFile: File

private const val REQUEST_CODE = 42


class CaptureTakenimageActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding : ActivityCaptureTakenimageBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private var tts : TextToSpeech? = null


    private var FILE_NAME ="photo.jpg"
    private var photoUri : Uri?= null
    private var imagePath :String? = null

    private var takenImage : Bitmap? = null
    private var userText:String = ""

    private lateinit var ivCapturedImage : ImageView
    private lateinit var ivbtnSave : Button
    private lateinit var ivbtnAnnotation :Button
    private lateinit var ivbtnTalk : Button
    private lateinit var etSearch : EditText
    private lateinit var etTitle : EditText
    private lateinit var etImageText : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureTakenimageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()

        tts = TextToSpeech(this, this)


        firestoreDb = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        takePhoto()

        ivbtnSave.setOnClickListener {
            uploadStorage()
        }
        ivbtnTalk.setOnClickListener {
            speakOut()
        }

        ivbtnAnnotation.setOnClickListener {
            closeActivity()
        }



        etSearch.doAfterTextChanged { startHighlight() }

//        etSearch.setOnClickListener {
//            startHighlight()
//        }
    }

    private fun closeActivity() {
        var intent: Intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startHighlight() {
        var textToHighlight = etSearch.text.toString()
        textToHighlight = textToHighlight.lowercase()
        var replaceWith = "<span style='background-color:yellow'>$textToHighlight</span>"
        var original = etImageText.text.toString()
        original = original.lowercase()
        var modified = original.replace(oldValue = textToHighlight, newValue = replaceWith)
        etImageText.setText(Html.fromHtml(modified))
    }

    private fun initView() {
        ivCapturedImage = binding.ivCapturedImage
        ivbtnSave = binding.ivbtnSave
        ivbtnAnnotation = binding.ivbtnAnnotate
        ivbtnTalk = binding.ivbtnPlayText
        etSearch = binding.etSearch
        etTitle = binding.etTitle
        etImageText = binding.etImageText

    }

    private fun uploadStorage() {
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}photo.jpg")
        val file = Uri.fromFile(File(photoFile.absoluteFile.toString()))
        photoReference.putFile(file)
            .continueWithTask{ photoUpload ->
                // retrieve image url
                Log.i(TAG, " uploateds bytes ${photoUpload.result}")
                photoReference.downloadUrl

            }.continueWithTask { downloadUrlTask ->

                val userName = Firebase.auth.currentUser
                var currentUserName = ""
                userName?.let {
                    for (profile in it.providerData) {
                        // Id of the provider (ex: google.com)
                        currentUserName = profile.email.toString()
                    }
                }
                currentUserName = currentUserName.dropLast(10)

                val user = User("", currentUserName)
                val post = Post("", etTitle.text.toString(), etImageText.text.toString(),
                    downloadUrlTask.result.toString(), System.currentTimeMillis(), user)

                firestoreDb.collection("post").add(post)
            }.addOnCompleteListener { postCreationTask ->
                if(!postCreationTask.isSuccessful){
                    Log.e(TAG, "Exception during Firebase Operations" , postCreationTask.exception)
                    Toast.makeText(this, "failed ${postCreationTask.exception} ", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                    var intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

    }


    private fun takePhoto(){
        val takenPictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile(FILE_NAME)

//            takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)
        val fileProvider = FileProvider.getUriForFile(this, "com.eachilin.imagecut.fileprovider", photoFile)

        takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        if(packageManager?.let { takenPictureIntent.resolveActivity(it) } != null){
            startActivityForResult(takenPictureIntent, PICK_PHOTO_CODE)
        }else{
            Toast.makeText(this, "unable to open", Toast.LENGTH_SHORT).show()
        }


    }

    private fun getPhotoFile(fileName: String): File {

        val storage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PICK_PHOTO_CODE && resultCode == Activity.RESULT_OK ){
            imagePath = photoFile.absolutePath
            takenImage  = BitmapFactory.decodeFile(photoFile.absolutePath)

            ivCapturedImage.setImageBitmap(takenImage)
            ivCapturedImage.rotation = 90F
//            ivImageText.setImageBitmap(takenImage)
            mlAction()
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun mlAction(){
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image : InputImage = takenImage?.let { InputImage.fromBitmap(it, 90) }!!

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                etImageText.setText(visionText.text)

            }
            .addOnFailureListener { e ->
                Log.e(TAG, e.toString())
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun speakOut(){
        val text = etImageText.text.toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "The language is not supported")
            } else {
                ivbtnTalk.isEnabled = true
            }
        } else {
            Log.e(TAG, "Failed to Initialize Data")
        }
    }



    override fun onDestroy() {
        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }


}