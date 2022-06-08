package com.eachilin.imagecut.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eachilin.imagecut.DetailActivity
import com.eachilin.imagecut.databinding.ItemSnapshotBinding
import com.eachilin.imagecut.models.Post


class PostsAdapter(private val post: MutableList<Post>, var onFirebaseDeleteDoc: (String, Int, String) -> Unit) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>(){

    private var _binding : ItemSnapshotBinding? = null
    private val binding get() = _binding!!



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        _binding = ItemSnapshotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(_binding!!)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = post[position]
        holder.bind(post, binding)
        binding.ivDelete.setOnClickListener {
            onFirebaseDeleteDoc(post.id.toString(), position, post.image_url)
        }
    }

    override fun getItemCount(): Int {
        return post.size
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class PostViewHolder(itemView: ItemSnapshotBinding) : RecyclerView.ViewHolder(itemView.root) {
        fun bind(post: Post, binding: ItemSnapshotBinding) {
            binding.tvContent.text = post.title


            binding.clSnapShot.setOnClickListener {
                val intent  = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra("post", post)
                itemView.context.startActivity(intent)
            }

            Glide.with(itemView.context)
                .load(post.image_url)
                .into(binding.ivSnapImage)

        }


    }

}