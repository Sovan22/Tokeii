package com.demomiru.tokeiv2



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import at.blogc.android.views.ExpandableTextView
import coil.load


//class MyDiffUtilCallback : DiffUtil.ItemCallback<MyItem>() {
//    override fun areItemsTheSame(oldItem: MyItem, newItem: MyItem): Boolean {
//        // Return true if items are the same.
//        return oldItem.id == newItem.id
//    }
//
//    override fun areContentsTheSame(oldItem: MyItem, newItem: MyItem): Boolean {
//        // Return true if contents are the same.
//        return oldItem == newItem
//    }
//}


class EpisodeAdapter2(private val episodes : List<Episode>,
                      private val clickHandler : (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter2.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val episodeText : TextView = itemView.findViewById(R.id.episode_no_text)
        val episodeImg : ImageView = itemView.findViewById(R.id.episode_img)
        val episodeOverview : ExpandableTextView = itemView.findViewById(R.id.episode_overview_text)
        val fl : FrameLayout = itemView.findViewById(R.id.expanded_episode_fl)
        val expandText : ImageView = itemView.findViewById(R.id.expand_text)
        val expandableTextView : ExpandableTextView = itemView.findViewById(R.id.episode_overview_text)
        val episodeNumber : TextView = itemView.findViewById(R.id.episode_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_expanded_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = episodes[position]
        holder.episodeText.text = episode.name
        holder.episodeNumber.text = episode.episode_number
        holder.episodeOverview.text = episode.overview
        holder.episodeImg.load("https://image.tmdb.org/t/p/w500${episode.still_path}")
        holder.fl.setOnClickListener {
            clickHandler(episode)
        }
        val etv = holder.expandableTextView
        etv.setAnimationDuration(750L)
        holder.expandText.setOnClickListener {
            if(etv.isExpanded){
                etv.collapse()
                holder.expandText.load(R.drawable.baseline_keyboard_arrow_down_24)
            }else{
                etv.expand()
                holder.expandText.load(R.drawable.baseline_keyboard_arrow_up_24)
            }
        }



    }

    override fun getItemCount(): Int {
        return episodes.size
    }
}