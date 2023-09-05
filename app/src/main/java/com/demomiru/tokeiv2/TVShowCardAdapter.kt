package com.demomiru.tokeiv2


import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import coil.load


class TVShowCardAdapter(private val tvShows: List<List<TVshow>>): RecyclerView.Adapter<TVShowCardAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val verticalImage: ImageView = itemView.findViewById(R.id.vertical_container)
        val horizontalImage1: ImageView = itemView.findViewById(R.id.horizontal_container1)
        val horizontalImage2: ImageView = itemView.findViewById(R.id.horizontal_container2)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tvShow = tvShows[position  % tvShows.size]
        holder.verticalImage.load("https://image.tmdb.org/t/p/w500${tvShow[0].poster_path}")
        holder.horizontalImage1.load("https://image.tmdb.org/t/p/original${tvShow[1].backdrop_path}")
        holder.horizontalImage2.load("https://image.tmdb.org/t/p/original${tvShow[2].backdrop_path}")
    }

    override fun getItemCount(): Int {
       return Int.MAX_VALUE
    }
}