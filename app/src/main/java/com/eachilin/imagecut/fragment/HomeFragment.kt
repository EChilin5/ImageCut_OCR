package com.eachilin.imagecut.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.eachilin.imagecut.CaptureTakenimageActivity
import com.eachilin.imagecut.adapter.PostsAdapter
import com.eachilin.imagecut.databinding.FragmentHomeBinding
import com.eachilin.imagecut.models.Post
import com.eachilin.imagecut.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File




private const val TAG = "CreateActivity"
private lateinit var photoFile: File

private const val REQUEST_CODE = 42


class HomeFragment : Fragment() {

    private lateinit var postListener: ListenerRegistration
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        savedInstanceState: Bundle?,
    ): View {
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
            val text= task

            val temp = mutableListOf<Post>()

            if(text?.isNotEmpty() == true){
                for (postEntry in post){
                    val title = postEntry.title.lowercase()
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

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchData() {
        val email = getEmail()
        Log.e(TAG, email)

        var postReference =firestoreDb.collection("post")

            .orderBy("creation_time_ms", Query.Direction.DESCENDING )
        postReference = postReference.whereEqualTo("user.username", email)
       postListener =  postReference.addSnapshotListener { snapshot, exception ->
            if(exception != null || snapshot == null){
                Log.e(TAG, "exception occurred", exception)
                return@addSnapshotListener
            }

            for (dc: DocumentChange in snapshot.documentChanges) {
                if (dc.type == DocumentChange.Type.ADDED) {

                    val postItem: Post = dc.document.toObject(Post::class.java)
                    post.add(postItem)
                    Log.e(TAG, "$postItem")
                }
            }
            Log.e(TAG, "testing present ")
            if(post.isNotEmpty()){
                    clearDeafult(false)

            }

            adapter.notifyDataSetChanged()
        }

    }

    override fun onStop() {
        super.onStop()
        postListener.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        postListener.remove()
    }

    private fun getEmail(): String {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.email.toString()

    }

    private fun openCaptureImageActivity() {
        val intent = Intent(context, CaptureTakenimageActivity::class.java)
        intent.putExtra("signed_in_user", signedInUser?.username)
        startActivity(intent)
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

    private fun deleteFireStoreDoc(postId: String, position: Int, imageLink:String){

        val photoRef = FirebaseStorage.getInstance()
        photoRef.getReferenceFromUrl(imageLink).delete().addOnCompleteListener { task->
            if(task.isSuccessful){
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(context, "Unable to Delete", Toast.LENGTH_SHORT).show()

            }
        }


        firestoreDb.collection("post")
            .document(postId).delete().addOnCompleteListener { task->
                if(task.isSuccessful){
                    adapter.notifyItemRemoved(position)
                    post.removeAt(position)
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                    if(post.isEmpty()){
                        clearDeafult(true)
                    }

                }else{
                    Toast.makeText(context, "Unable to Delete", Toast.LENGTH_SHORT).show()

                }

        }
    }

    private fun clearDeafult(status: Boolean){

            binding.tvNotFoundTitle.isVisible = status
            binding.tvNotFoundTitle.isEnabled = status

            binding.ivDataNotFound.isVisible = status
            binding.ivDataNotFound.isEnabled = status
            binding.tvNoteActionDesc.isVisible = status
            binding.tvNoteActionDesc.isEnabled = status


    }

}