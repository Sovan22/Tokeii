package com.demomiru.tokeiv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.demomiru.tokeiv2.MovieAdapter2.Companion.differCallback
import com.demomiru.tokeiv2.utils.retrofitBuilder

class TVShowAdapter(
private val clickHandler : (TVshow,Int) -> Unit
) :
    ListAdapter<TVshow,TVShowAdapter.ViewHolder>(differCallback) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tvShow = getItem(position)
        holder.titleTextView.text = tvShow.name
        holder.imageView
            .load("https://image.tmdb.org/t/p/w500${tvShow.poster_path}")
        ViewCompat.setTransitionName(holder.imageView, "image_$position")

        holder.itemView.setOnClickListener {
            clickHandler(tvShow,position)
        }
    }
    companion object {
        val differCallback = object : DiffUtil.ItemCallback<TVshow>() {
            override fun areItemsTheSame(oldItem: TVshow, newItem: TVshow): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TVshow, newItem: TVshow): Boolean {
                return oldItem == newItem
            }
        }
    }

}

class TvShowPagingSource(private val list: Int): PagingSource<Int, TVshow>() {
    private val retrofit = retrofitBuilder()
    private val tvService = retrofit.create(TMDBService::class.java)
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TVshow> {
        return try {
            val currentPage = params.key ?: 1
            val response = when(list){
                1-> tvService.getPopularTVShows(BuildConfig.TMDB_API_KEY,"en-US",currentPage)
                2-> tvService.getTrendingTVShows(BuildConfig.TMDB_API_KEY, "en-US",currentPage)
                3-> tvService.getTopRatedTVShows(BuildConfig.TMDB_API_KEY,"en-US",currentPage)
                else -> throw Exception("wrong list parameter input")
            }

            val data = response.body()!!.results
            val responseData = mutableListOf<TVshow>()
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


    override fun getRefreshKey(state: PagingState<Int, TVshow>): Int? {
        return null
    }

}

class TVShowAdapter2(private val clickHandler : (TVshow,Int) -> Unit) :
    PagingDataAdapter<TVshow,TVShowAdapter2.ViewHolder>(differCallback) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tvShow = getItem(position)!!
        holder.titleTextView.text = tvShow.name
        holder.imageView
            .load("https://image.tmdb.org/t/p/w500${tvShow.poster_path}")
        ViewCompat.setTransitionName(holder.imageView, "image_$position")

        holder.itemView.setOnClickListener {
            clickHandler(tvShow,position)
        }
        holder.setIsRecyclable(false)
    }
    companion object {
        val differCallback = object : DiffUtil.ItemCallback<TVshow>() {
            override fun areItemsTheSame(oldItem: TVshow, newItem: TVshow): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TVshow, newItem: TVshow): Boolean {
                return oldItem == newItem
            }
        }
    }
}