package com.example.my_blog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

data class Article(
    val title: String,
    val description: String?,
    val url: String,
    val source: Source
)

data class Source(
    val name: String
)

interface NewsApi {
    @GET("v2/top-headlines")
    fun getTopHeadlines(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}

class TrendingNewsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trending_news)

        recyclerView = findViewById(R.id.recyclerViewNews)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NewsAdapter { url ->
            openNewsUrl(url)
        }
        recyclerView.adapter = adapter

        fetchNews()
    }

    private fun fetchNews() {
        // Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create OkHttpClient with logging
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val newsApi = retrofit.create(NewsApi::class.java)
        val apiKey = "e7fe3544d5134943b8f16820d6783cc3"
        Log.d("TrendingNews", "Making API call with key: ${apiKey.take(5)}...")
        
        val call = newsApi.getTopHeadlines("us", apiKey)

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                Log.d("TrendingNews", "Response code: ${response.code()}")
                Log.d("TrendingNews", "Response message: ${response.message()}")
                
                if (response.isSuccessful) {
                    val newsResponse = response.body()
                    Log.d("TrendingNews", "Response successful: ${newsResponse?.status}")
                    Log.d("TrendingNews", "Total results: ${newsResponse?.totalResults}")
                    
                    newsResponse?.articles?.let { articles ->
                        Log.d("TrendingNews", "Articles received: ${articles.size}")
                        if (articles.isEmpty()) {
                            Log.w("TrendingNews", "No articles in the response")
                            Toast.makeText(this@TrendingNewsActivity, "No news articles found", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("TrendingNews", "First article title: ${articles.firstOrNull()?.title}")
                            adapter.submitList(articles)
                        }
                    } ?: run {
                        Log.e("TrendingNews", "Articles list is null")
                        Toast.makeText(this@TrendingNewsActivity, "No news articles found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("TrendingNews", "Error response: $errorBody")
                    Log.e("TrendingNews", "Error code: ${response.code()}")
                    Toast.makeText(this@TrendingNewsActivity, "Error fetching news: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Log.e("TrendingNews", "Network error", t)
                Log.e("TrendingNews", "Error message: ${t.message}")
                Toast.makeText(this@TrendingNewsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openNewsUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("TrendingNews", "Error opening URL: $url", e)
            Toast.makeText(this, "Could not open the article", Toast.LENGTH_SHORT).show()
        }
    }
}

class NewsAdapter(private val onItemClick: (String) -> Unit) : 
    androidx.recyclerview.widget.ListAdapter<Article, NewsAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): NewsViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NewsViewHolder(itemView: android.view.View) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val titleView: android.widget.TextView = itemView.findViewById(R.id.tvTitle)
        private val descriptionView: android.widget.TextView = itemView.findViewById(R.id.tvDescription)
        private val sourceView: android.widget.TextView = itemView.findViewById(R.id.tvSource)

        fun bind(article: Article) {
            titleView.text = article.title
            descriptionView.text = article.description ?: "No description available"
            sourceView.text = "Source: ${article.source.name}"
            
            itemView.setOnClickListener {
                onItemClick(article.url)
            }
        }
    }
}

class NewsDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
} 