package com.example.my_blog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class WriteBlogActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var cbDraft: CheckBox
    private lateinit var btnPickMedia: Button
    private lateinit var ivPreview: ImageView
    private lateinit var vvPreview: VideoView
    private lateinit var btnSave: Button
    private lateinit var btnPublish: Button

    private lateinit var dbHelper: BlogDbHelper

    private var blogId: Int = -1
    private var mediaUri: String? = null
    private var mediaType: String? = null  // "image" or "video"

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Permission denied to access storage", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_blog)

        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        cbDraft = findViewById(R.id.cbDraft)
        btnPickMedia = findViewById(R.id.btnPickMedia)
        ivPreview = findViewById(R.id.ivPreview)
        vvPreview = findViewById(R.id.vvPreview)
        btnSave = findViewById(R.id.btnSave)
        btnPublish = findViewById(R.id.btnPublish)

        dbHelper = BlogDbHelper(this)

        blogId = intent.getIntExtra("blog_id", -1)
        if (blogId != -1) loadBlog(blogId)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val mimeType = contentResolver.getType(uri) ?: ""
                mediaUri = uri.toString()
                mediaType = when {
                    mimeType.startsWith("image") -> "image"
                    mimeType.startsWith("video") -> "video"
                    else -> null
                }

                if (mediaType == "image") {
                    ivPreview.setImageURI(uri)
                    ivPreview.visibility = ImageView.VISIBLE
                    vvPreview.visibility = VideoView.GONE
                } else if (mediaType == "video") {
                    vvPreview.setVideoURI(uri)
                    vvPreview.start()
                    vvPreview.visibility = VideoView.VISIBLE
                    ivPreview.visibility = ImageView.GONE
                } else {
                    Toast.makeText(this, "Unsupported media type", Toast.LENGTH_SHORT).show()
                    mediaUri = null
                    mediaType = null
                }
            }
        }

        btnPickMedia.setOnClickListener {
            pickMediaLauncher.launch("image/* video/*")
        }

        btnSave.setOnClickListener {
            saveBlog(isDraft = true)
        }

        btnPublish.setOnClickListener {
            saveBlog(isDraft = false)
        }
    }

    private fun saveBlog(isDraft: Boolean) {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val authorId = 1 // static author for now

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (content.isEmpty() && mediaUri.isNullOrEmpty()) {
            Toast.makeText(this, "Add content or media", Toast.LENGTH_SHORT).show()
            return
        }

        if (blogId == -1) {
            val newId = dbHelper.insertBlog(title, content, authorId, isDraft, mediaUri, mediaType)
            if (newId != -1L) {
                Toast.makeText(this, "Blog saved", Toast.LENGTH_SHORT).show()
                if (!isDraft) {
                    startActivity(Intent(this, ReadBlogActivity::class.java).apply {
                        putExtra("blog_id", newId.toInt())
                    })
                }
                finish()
            } else {
                Toast.makeText(this, "Failed to save blog", Toast.LENGTH_SHORT).show()
            }
        } else {
            val updated = dbHelper.updateBlog(blogId, title, content, isDraft, mediaUri, mediaType) > 0
            if (updated) {
                Toast.makeText(this, "Blog updated", Toast.LENGTH_SHORT).show()
                if (!isDraft) {
                    startActivity(Intent(this, ReadBlogActivity::class.java).apply {
                        putExtra("blog_id", blogId)
                    })
                }
                finish()
            } else {
                Toast.makeText(this, "Failed to update blog", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBlog(id: Int) {
        val blog = dbHelper.getBlogById(id)
        if (blog != null) {
            etTitle.setText(blog.title)
            etContent.setText(blog.content)
            cbDraft.isChecked = blog.isDraft
            mediaUri = blog.mediaUri
            mediaType = blog.mediaType

            if (!mediaUri.isNullOrEmpty() && !mediaType.isNullOrEmpty()) {
                val uri = Uri.parse(mediaUri)
                if (mediaType == "image") {
                    ivPreview.setImageURI(uri)
                    ivPreview.visibility = ImageView.VISIBLE
                    vvPreview.visibility = VideoView.GONE
                } else if (mediaType == "video") {
                    vvPreview.setVideoURI(uri)
                    vvPreview.start()
                    vvPreview.visibility = VideoView.VISIBLE
                    ivPreview.visibility = ImageView.GONE
                }
            }
        }
    }
}
