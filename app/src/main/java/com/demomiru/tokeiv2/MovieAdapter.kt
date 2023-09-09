package com.demomiru.tokeiv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
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


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.titleTextView.text = movie.title + " (${yearExtract(movie.release_date)})"
        holder.imageView.load("https://image.tmdb.org/t/p/w500${movie.poster_path}")
        holder.itemView.setOnClickListener {
            clickHandler(movie)
        }
    }

}
