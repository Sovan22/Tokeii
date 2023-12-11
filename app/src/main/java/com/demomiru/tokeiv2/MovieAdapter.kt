package com.demomiru.tokeiv2

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.demomiru.tokeiv2.utils.retrofitBuilder
import com.demomiru.tokeiv2.utils.yearExtract

class MovieAdapter(private val movies: List<Movie>,
                   private val clickHandler : (Movie) -> Unit
) :
    RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return movies.size
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.titleTextView.text = movie.title + " (${yearExtract(movie.release_date)})"
        holder.imageView.load("https://image.tmdb.org/t/p/w500${movie.poster_path}")
        holder.itemView.setOnClickListener {
            clickHandler(movie)
        }
    }

}

class MoviesPagingSource(private val list: Int): PagingSource<Int, Movie>() {
    private val retrofit = retrofitBuilder()
    private val movieService = retrofit.create(MovieService::class.java)
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        return try {
            val currentPage = params.key ?: 1
            val response = when(list){
                1-> movieService.getPopularMovies(BuildConfig.TMDB_API_KEY,"en-US",currentPage)
                2-> movieService.getTrendingMovies(BuildConfig.TMDB_API_KEY, "en-US",currentPage)
                3-> movieService.getTopRatedMovies(BuildConfig.TMDB_API_KEY,"en-US",currentPage)
                else -> throw Exception("wrong list parameter input")
            }

            val data = response.body()!!.results
            val responseData = mutableListOf<Movie>()
            responseData.addAll(data)

            LoadResult.Page(
                data = responseData,
                prevKey = if (currentPage == 1) null else -1,
                nextKey = currentPage.plus(1)
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }


    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return null
    }


}

class MovieAdapter2(
    private val clickHandler : (Movie) -> Unit
) :
    PagingDataAdapter<Movie,MovieAdapter2.ViewHolder>(differCallback) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view,parent,false)
        return ViewHolder(view)
    }

    companion object {
        val differCallback = object : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
                return oldItem == newItem
            }
        }
    }



    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = getItem(position)!!
        holder.titleTextView.text = movie.title + " (${yearExtract(movie.release_date)})"
        holder.imageView.load("https://image.tmdb.org/t/p/w500${movie.poster_path}")
        holder.itemView.setOnClickListener {
            clickHandler(movie)
        }
        holder.setIsRecyclable(false)
    }

}
