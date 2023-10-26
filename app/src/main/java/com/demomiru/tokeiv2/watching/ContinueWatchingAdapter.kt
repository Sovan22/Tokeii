package com.demomiru.tokeiv2.watching

import android.annotation.SuppressLint

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import android.widget.TextView

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.demomiru.tokeiv2.R
import com.google.android.material.progressindicator.LinearProgressIndicator



class ContinueWatchingAdapter(private val onClick: (ContinueWatching,Boolean)->Unit): ListAdapter<ContinueWatching,ContinueWatchingAdapter.ViewHolder>(DiffCallBack){
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val imageView: ImageView = view.findViewById(R.id.image_view)
        val titleTextView: TextView = view.findViewById(R.id.title_text_view)
        val progressBar : LinearProgressIndicator = view.findViewById(R.id.item_progress)
        val tvShowDetail : TextView = view.findViewById(R.id.tv_show_detail_tv)
        val backGround :View = view.findViewById(R.id.blur_background)
        val delete : ImageView = view.findViewById(R.id.remove_continue)
    }

    object DiffCallBack : DiffUtil.ItemCallback<ContinueWatching>() {
        override fun areItemsTheSame(oldItem: ContinueWatching, newItem: ContinueWatching): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ContinueWatching, newItem: ContinueWatching): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view,parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.titleTextView.text = item.title
        if (item.type != "anime")
            holder.imageView.load("https://image.tmdb.org/t/p/w500${item.imgLink}")
        else
            holder.imageView.load(item.imgLink)
        holder.progressBar.progress = item.progress
        holder.progressBar.visibility = View.VISIBLE
        holder.tvShowDetail.visibility = View.GONE
        if(item.type != "movie"){

            holder.tvShowDetail.text = if(item.type == "tvshow") "S${item.season} E${item.episode}" else "E${item.episode+1}"
            holder.tvShowDetail.visibility = View.VISIBLE
        }
        holder.itemView.setOnClickListener {
            onClick(item,false)
        }
       holder.itemView.setOnLongClickListener {
           // User performed a long press
           holder.backGround.visibility = View.VISIBLE
           holder.delete.visibility = View.VISIBLE
           holder.itemView.setOnClickListener(null)
           holder.delete.setOnClickListener {
               onClick(item,true)
           }

           Handler(Looper.getMainLooper()).postDelayed({
               holder.backGround.visibility = View.GONE
               holder.delete.visibility = View.GONE
               holder.itemView.setOnClickListener { onClick(item,false) }
           }, 4000)  // Delay of 4 second

           true
       }
    }
}