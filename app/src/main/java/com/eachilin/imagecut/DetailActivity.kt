package com.eachilin.imagecut

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.eachilin.imagecut.databinding.ActivityDetailBinding
import com.eachilin.imagecut.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "DetailActivity"
class DetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailBinding
    private lateinit var firestoreDb: FirebaseFirestore

    private var tts : TextToSpeech? = null


    private lateinit var ivbtnSave : ImageButton
    private lateinit var ivbtnAnnotate : ImageButton
    private lateinit var tvImageText : TextView
    private lateinit var ivbtnPlay : ImageButton
    private lateinit var etSearch : EditText
    private lateinit var tvTitle : TextView
    private lateinit var postInfo : Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        firestoreDb = FirebaseFirestore.getInstance()
        postInfo  = intent.getSerializableExtra("post") as Post

        tvImageText.text = postInfo.imagedescription
        tvTitle.text = postInfo.title


        tvImageText.setMovementMethod(ScrollingMovementMethod())

        ivbtnSave.setOnClickListener {
            updateInformation()
        }

        ivbtnPlay.setOnClickListener {
            speakOut()
        }

    }

    private fun initView() {
        ivbtnSave = binding.ivbtnSaveDetails
        ivbtnAnnotate = binding.ivbtnAnnotateDetails
        tvImageText = binding.tvImageTextDetails
        ivbtnPlay = binding.ivbtnPlayTextDetails
        etSearch = binding.etSearchDetails
        tvTitle = binding.tvTitleDetails
    }

    private fun updateInformation(){
      // firestoreDb.collection("post").document(postInfo.id).update("imagedescription", "${postInfo.imagedescription} testing update feature")
        Toast.makeText(this, "post have been update ${postInfo.id}", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "${postInfo.id}")
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
}