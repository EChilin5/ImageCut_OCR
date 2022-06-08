package com.eachilin.imagecut

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import com.eachilin.imagecut.activity.MainActivity
import com.eachilin.imagecut.databinding.ActivityDetailBinding
import com.eachilin.imagecut.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


enum class WindowSizeClass { COMPACT, MEDIUM, EXPANDED }


private const val TAG = "DetailActivity"
class DetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding : ActivityDetailBinding
    private lateinit var firestoreDb: FirebaseFirestore

    private var tts : TextToSpeech? = null


    private lateinit var ivbtnSave : Button
    private lateinit var ivbtnAnnotate : Button
    private lateinit var tvImageText : EditText
    private lateinit var ivbtnPlay : Button
    private lateinit var etSearch : EditText
    private lateinit var tvTitle : EditText
    private lateinit var postInfo : Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()




        tts = TextToSpeech(this, this)



        firestoreDb = FirebaseFirestore.getInstance()
        postInfo  = intent.getSerializableExtra("post") as Post

        tvImageText.setText( postInfo.imagedescription)
        tvTitle.setText(postInfo.title)


        tvImageText.setMovementMethod(ScrollingMovementMethod())

        ivbtnSave.setOnClickListener {
            updateInformation()
        }

        ivbtnPlay.setOnClickListener {
            speakOut()
        }

        etSearch.doAfterTextChanged {
            startHighlight()
        }

        ivbtnAnnotate.setOnClickListener {
            deleteItem()
        }

        tvImageText.setOnClickListener {
            tvImageText.showSoftInputOnFocus = true
        }

        tvImageText.setOnClickListener {
            tvImageText.showSoftInputOnFocus = false
        }


    }



    private fun deleteItem() {
        firestoreDb.collection("post")
            .document(postInfo.id.toString()).delete()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(this, "Item is now deleted", Toast.LENGTH_SHORT).show()
                    var intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
    }


    private fun startHighlight() {
        var textToHighlight = etSearch.text.toString()
        textToHighlight = textToHighlight.lowercase()
        var replaceWith = "<span style='background-color:yellow'>$textToHighlight</span>"
        var original = tvImageText.text.toString()
        original = original.lowercase()
        var modified = original.replace(oldValue = textToHighlight, newValue = replaceWith)
        tvImageText.setText(Html.fromHtml(modified))
    }

    private fun initView() {
        ivbtnSave = binding.ivbtnSaveDetails
        ivbtnAnnotate = binding.ivbtnAnnotateDetails
        tvImageText = binding.etImageTextDetails
        ivbtnPlay = binding.ivbtnPlayTextDetails
        etSearch = binding.etSearchDetails
        tvTitle = binding.etTitleDetails
    }

    private fun updateInformation(){

        var updatePost: Post = Post(postInfo.id, tvTitle.text.toString(), tvImageText.text.toString(), postInfo.image_url, System.currentTimeMillis(), postInfo.user)
       firestoreDb.collection("post").document(postInfo.id.toString()).set(updatePost)
        Log.i(TAG, "${postInfo.id}")

        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun delete(){
        firestoreDb.collection("post").document(postInfo.id.toString()).delete()
    }


    private fun speakOut(){
        val text = tvImageText.text.toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }




    override fun onDestroy() {
        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            val result = tts!!.setLanguage(Locale.US)

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED ){
                Log.e(TAG, "The language is not supported")
            }else{
                ivbtnPlay.isEnabled = true
            }
        }else{
            Log.e(TAG, "Failed to Initialize Data")
        }
    }


}