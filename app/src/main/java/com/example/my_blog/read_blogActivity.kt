package com.example.my_blog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReadBlogActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlogAdapter
    private lateinit var dbHelper: BlogDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_blog)

        recyclerView = findViewById(R.id.recyclerViewBlogs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dbHelper = BlogDbHelper(this)

        adapter = BlogAdapter(
            onItemClick = { blog ->
                // Open the full blog view
                val intent = Intent(this, BlogDetailActivity::class.java)
                intent.putExtra("blog_id", blog.id)
                startActivity(intent)
            },
            onEditClick = { blog ->
                val intent = Intent(this, WriteBlogActivity::class.java)
                intent.putExtra("blog_id", blog.id)
                startActivity(intent)
            },
            onDeleteClick = { blog ->
                val deleted = dbHelper.deleteBlog(blog.id)
                if (deleted > 0) {
                    Toast.makeText(this, "Blog deleted", Toast.LENGTH_SHORT).show()
                    loadBlogs()
                } else {
                    Toast.makeText(this, "Failed to delete blog", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerView.adapter = adapter
        loadBlogs()
    }

    private fun loadBlogs() {
        Thread {
            val blogs = dbHelper.getAllBlogs()
            runOnUiThread {
                if (blogs.isEmpty()) {
                    Toast.makeText(this, "No blogs found", Toast.LENGTH_SHORT).show()
                } else {
                    adapter.submitList(blogs)
                }
            }
        }.start()
    }
}

class BlogAdapter(
    private val onItemClick: (Blog) -> Unit,
    private val onEditClick: (Blog) -> Unit,
    private val onDeleteClick: (Blog) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Blog, BlogAdapter.BlogViewHolder>(BlogDiffCallback()) {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): BlogViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blog, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BlogViewHolder(itemView: android.view.View) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.tvBlogTitle)
        private val previewView: TextView = itemView.findViewById(R.id.tvBlogPreview)
        private val dateView: TextView = itemView.findViewById(R.id.tvBlogDate)
        private val btnEdit: android.widget.Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: android.widget.Button = itemView.findViewById(R.id.btnDelete)

        fun bind(blog: Blog) {
            titleView.text = blog.title
            previewView.text = blog.content.take(150) + if (blog.content.length > 150) "..." else ""
            dateView.text = "Posted by User ${blog.authorId}"
            itemView.setOnClickListener { onItemClick(blog) }
            btnEdit.setOnClickListener { onEditClick(blog) }
            btnDelete.setOnClickListener { onDeleteClick(blog) }
        }
    }
}

class BlogDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Blog>() {
    override fun areItemsTheSame(oldItem: Blog, newItem: Blog): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Blog, newItem: Blog): Boolean {
        return oldItem == newItem
    }
}
