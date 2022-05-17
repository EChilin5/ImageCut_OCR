package com.eachilin.imagecut.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.eachilin.imagecut.CaptureTakenimageActivity
import com.eachilin.imagecut.adapter.PostsAdapter
import com.eachilin.imagecut.databinding.FragmentHomeBinding
import com.eachilin.imagecut.models.Post
import com.eachilin.imagecut.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

private const val TAG = "CreateActivity"
private const val PICK_PHOTO_CODE = 1234
private lateinit var photoFile: File

private const val REQUEST_CODE = 42


class HomeFragment : Fragment() {

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var FILE_NAME ="photo.jpg"
    private var photoUri : Uri?= null
    private var imagePath :String? = null

    private var takenImage : Bitmap? = null
    private lateinit var firestoreDb : FirebaseFirestore

    private lateinit var post : MutableList<Post>
    private lateinit var adapter : PostsAdapter
    private lateinit var etSearch : EditText

    private var signedInUser : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        post = mutableListOf()
        adapter = PostsAdapter(post, ::deleteFireStoreDoc)
        binding.rvPicturesSnap.adapter = adapter
        binding.rvPicturesSnap.layoutManager = LinearLayoutManager(context)

        firestoreDb = FirebaseFirestore.getInstance()
        etSearch = binding.etSearchDetails

        if(post.isEmpty()){
            fetchData()
        }

        binding.flTakePhoto.setOnClickListener{
            openCaptureImageActivity()
        }
        etSearch.doAfterTextChanged { task->
            var text= task

            val temp = mutableListOf<Post>()

            if(text?.isNotEmpty() == true){
                for (postEntry in post){
                    var title = postEntry.title.lowercase()
                    if(title.contains(text)){
                        temp.add(postEntry)
                    }
                }
            }
            if (text != null) {
                if(text.isEmpty()){
                    binding.rvPicturesSnap.adapter =  PostsAdapter(post, ::deleteFireStoreDoc)
                }else{
                    binding.rvPicturesSnap.adapter =  PostsAdapter(temp, ::deleteFireStoreDoc)

                }
            }
        }

    }

    private fun fetchData() {
        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "signed in user : ${signedInUser?.username}")
            }
            .addOnFailureListener {  exception ->
                Log.i(TAG, "Failure fetching signed in user", exception)
            }

        Log.i(TAG, "${signedInUser?.username} found")
        var postReference =firestoreDb.collection("post")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING )
        postReference = postReference.whereEqualTo("user.username", "ea")
        postReference.addSnapshotListener { snapshot, exception ->
            if(exception != null || snapshot == null){
                Log.e(TAG, "exception occurred", exception)
                return@addSnapshotListener
            }

            for (dc: DocumentChange in snapshot?.documentChanges!!) {
                if (dc.type == DocumentChange.Type.ADDED) {

                    val postItem: Post = dc.document.toObject(Post::class.java)
                    post.add(postItem)
                }
            }
            adapter.notifyDataSetChanged()
        }

    }

    private fun openCaptureImageActivity() {
        var intent = Intent(context, CaptureTakenimageActivity::class.java)
        intent.putExtra("signed_in_user", signedInUser?.username)
        startActivity(intent)
    }


    private fun takePhoto(){
        val takenPictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile(FILE_NAME)

//            takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)
        val fileProvider = context?.let { FileProvider.getUriForFile(it, "com.eachilin.imagecut.fileprovider", photoFile) }
        takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        if(context?.packageManager?.let { takenPictureIntent.resolveActivity(it) } != null){
            startActivityForResult(takenPictureIntent, REQUEST_CODE)
        }else{
            Toast.makeText(context, "unable to open", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getPhotoFile(fileName: String): File {

        val storage = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK ){
            imagePath = photoFile.absolutePath
            takenImage  = BitmapFactory.decodeFile(photoFile.absolutePath)
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
//                tvData.text = visionText.text


            }
            .addOnFailureListener { e ->
                Log.e(TAG, e.toString())
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteFireStoreDoc(postId: String, position: Int){
        firestoreDb.collection("post")
            .document(postId).delete().addOnCompleteListener { task->
                if(task.isSuccessful){
                    adapter.notifyItemRemoved(position)
                    post.removeAt(position)
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()

                }else{
                    Toast.makeText(context, "Unable to Delete", Toast.LENGTH_SHORT).show()

                }

        }
    }




    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {

            }
    }
}