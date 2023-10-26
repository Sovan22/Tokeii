package com.demomiru.tokeiv2.anime

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.disk.DiskCache
import coil.load
import coil.memory.MemoryCache
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.utils.GogoAnime

class AnimeAdapter(context: Context, private val onClick: (GogoAnime.AnimeSearchResponse) -> Unit): ListAdapter<GogoAnime.AnimeSearchResponse, AnimeAdapter.ViewHolder>(
    DiffCallBack
) {

    private val imageLoader = ImageLoader.Builder(context)
        .memoryCache { MemoryCache.Builder(context).maxSizePercent(0.25).build() }
        .build()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val imageView : ImageView = view.findViewById(R.id.image_view)
        val title : TextView = view.findViewById(R.id.title_text_view)
        val dub: TextView = view.findViewById(R.id.tv_show_detail_tv)
    }

    object DiffCallBack: DiffUtil.ItemCallback<GogoAnime.AnimeSearchResponse>(){
        override fun areItemsTheSame(
            oldItem: GogoAnime.AnimeSearchResponse,
            newItem: GogoAnime.AnimeSearchResponse
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: GogoAnime.AnimeSearchResponse,
            newItem: GogoAnime.AnimeSearchResponse
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent,false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val anime = getItem(position)
        holder.imageView.load(anime.posterUrl,imageLoader)
        holder.title.text = anime.name
        if(anime.dub!=null){
            holder.dub.visibility = if(anime.dub) View.VISIBLE else View.GONE
        }
        holder.itemView.setOnClickListener {
            onClick(anime)
        }
    }
}

class AnimeEpisodeAdapter(private val onClick: (GogoAnime.Episode,Int) -> Unit) : ListAdapter<GogoAnime.Episode, AnimeEpisodeAdapter.ViewHolder>(
    DiffCallBack
){

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val episodeText : TextView = itemView.findViewById(R.id.episode_no_text)
    }

    object DiffCallBack : DiffUtil.ItemCallback<GogoAnime.Episode>() {
        override fun areItemsTheSame(
            oldItem: GogoAnime.Episode,
            newItem: GogoAnime.Episode
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: GogoAnime.Episode,
            newItem: GogoAnime.Episode
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_item_viem,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = getItem(position)
        holder.episodeText.text = episode.name
        holder.itemView.setOnClickListener {
            onClick(episode,position)
        }
    }


}