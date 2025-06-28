package com.example.my_blog

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class BlogDetailActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var ivImage: ImageView
    private lateinit var vvVideo: VideoView
    private lateinit var dbHelper: BlogDbHelper
    private var blogId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog_detail)

        tvTitle = findViewById(R.id.tvBlogTitle)
        tvContent = findViewById(R.id.tvBlogContent)
        ivImage = findViewById(R.id.ivBlogImage)
        vvVideo = findViewById(R.id.vvBlogVideo)

        dbHelper = BlogDbHelper(this)

        blogId = intent.getIntExtra("blog_id", -1)
        if (blogId == -1) {
            Toast.makeText(this, "Invalid blog id", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadBlog(blogId)
    }

    private fun loadBlog(id: Int) {
        Thread {
            val blog = dbHelper.getBlogById(id)
            runOnUiThread {
                if (blog != null) {
                    tvTitle.text = blog.title
                    try {
                        if (blog.imageBlob != null && blog.imageBlob.isNotEmpty()) {
                            val bitmap = BitmapFactory.decodeByteArray(blog.imageBlob, 0, blog.imageBlob.size)
                            ivImage.setImageBitmap(bitmap)
                            ivImage.visibility = android.view.View.VISIBLE
                            vvVideo.visibility = android.view.View.GONE
                            tvContent.visibility = android.view.View.VISIBLE
                            tvContent.text = blog.content
                        } else if (blog.videoBlob != null && blog.videoBlob.isNotEmpty()) {
                            // For simplicity, save video to cache and play from there
                            val videoFile = java.io.File.createTempFile("blog_video", ".mp4", cacheDir)
                            videoFile.writeBytes(blog.videoBlob)
                            vvVideo.setVideoPath(videoFile.absolutePath)
                            vvVideo.start()
                            vvVideo.visibility = android.view.View.VISIBLE
                            ivImage.visibility = android.view.View.GONE
                            tvContent.visibility = android.view.View.VISIBLE
                            tvContent.text = blog.content
                        } else if (!blog.mediaUri.isNullOrEmpty() && !blog.mediaType.isNullOrEmpty()) {
                            val uri = Uri.parse(blog.mediaUri)
                            if (blog.mediaType == "image") {
                                try {
                                    ivImage.setImageURI(uri)
                                    ivImage.visibility = android.view.View.VISIBLE
                                } catch (e: Exception) {
                                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                                    ivImage.visibility = android.view.View.GONE
                                }
                                vvVideo.visibility = android.view.View.GONE
                                tvContent.visibility = android.view.View.VISIBLE
                                tvContent.text = blog.content
                            } else if (blog.mediaType == "video") {
                                try {
                                    vvVideo.setVideoURI(uri)
                                    vvVideo.start()
                                    vvVideo.visibility = android.view.View.VISIBLE
                                } catch (e: Exception) {
                                    Toast.makeText(this, "Failed to load video", Toast.LENGTH_SHORT).show()
                                    vvVideo.visibility = android.view.View.GONE
                                }
                                ivImage.visibility = android.view.View.GONE
                                tvContent.visibility = android.view.View.VISIBLE
                                tvContent.text = blog.content
                            } else {
                                ivImage.visibility = android.view.View.GONE
                                vvVideo.visibility = android.view.View.GONE
                                tvContent.visibility = android.view.View.VISIBLE
                                tvContent.text = blog.content
                            }
                        } else {
                            ivImage.visibility = android.view.View.GONE
                            vvVideo.visibility = android.view.View.GONE
                            tvContent.visibility = android.view.View.VISIBLE
                            tvContent.text = blog.content
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error displaying blog: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        ivImage.visibility = android.view.View.GONE
                        vvVideo.visibility = android.view.View.GONE
                        tvContent.visibility = android.view.View.VISIBLE
                        tvContent.text = blog.content
                    }
                } else {
                    Toast.makeText(this, "Blog not found (ID: $id)", Toast.LENGTH_LONG).show()
                    // Don't finish, just show an error
                    tvTitle.text = "Blog not found"
                    tvContent.text = "The blog you are looking for does not exist."
                    ivImage.visibility = android.view.View.GONE
                    vvVideo.visibility = android.view.View.GONE
                }
            }
        }.start()
    }
} 