package com.project.bg_tube.ui.adapters

import android.content.Context
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.bg_tube.R
import com.project.bg_tube.data.request.Playlist
import com.project.bg_tube.ui.adapters.listener.OnItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import org.json.JSONObject
import java.io.FileNotFoundException
import java.lang.reflect.InvocationTargetException
import java.net.URL

class FragmentAdapter(context: Context, listener: OnItemClickListener) : RecyclerView.Adapter<FragmentAdapter.FragmentViewHolder>() {

    var listener:OnItemClickListener ?= null
    var list: ArrayList<Playlist>? = null
    var context: Context? = null
    private var positionCheck = 0
    private var isStartViewCheck = true

    init {
        this.context = context
        this.listener = listener
    }



    fun setData(list: ArrayList<Playlist>) {
        this.list = list
        notifyDataSetChanged()
    }
    fun getData() : ArrayList<Playlist> {
        return if(list != null) {list!!} else{ArrayList()
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FragmentAdapter.FragmentViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resource_playlist, parent, false)

        return FragmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: FragmentAdapter.FragmentViewHolder, position: Int) {
        val playing: Playlist? = list?.get(position)

        GlobalScope.launch(Dispatchers.Main) {
            if (isStartViewCheck) {
                if (position > 6) isStartViewCheck = false
            } else {
                if (position > positionCheck) {
                    holder.viewCard.animation = AnimationUtils.loadAnimation(context, R.anim.fall_down)
                } else {
                    holder.viewCard.animation = AnimationUtils.loadAnimation(context, R.anim.raise_up)
                }
            }

            holder.itemView.setOnClickListener {
                listener?.OnItemClick(position);
            }

            holder.textTitle?.text = getQuietly(playing?.videoUrl, 1)
            holder.contentAuthor.text = getQuietly(playing?.videoUrl, 2)
            Glide.with(context!!.applicationContext).load(getQuietly(playing?.videoUrl, 3)).centerCrop().into(
                holder.thumbnailImage
            )
            positionCheck = position
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    class FragmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.title)
        val thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        val contentAuthor : TextView = itemView.findViewById(R.id.content_author)
        val viewCard : ConstraintLayout = itemView.findViewById(R.id.viewCard)
    }

    fun getQuietly(youtubeUrl: String?, requestCode: Int): String? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        var embededURL : URL? = null

        embededURL = URL("http://www.youtube.com/oembed?url=$youtubeUrl&format=json")

        when(requestCode){
            1 -> {
                return if(check(embededURL)){ JSONObject(IOUtils.toString(embededURL)).getString("title") } else { "Oops! We got a problem, We cannot parse that video. sorry." }
            }
            2 -> {
                return if(check(embededURL)){ JSONObject(IOUtils.toString(embededURL)).getString("author_name") } else { "no data" }
            }
            3 -> {
                return return if(check(embededURL)){ JSONObject(IOUtils.toString(embededURL)).getString("thumbnail_url") } else { "no data" }
            }
        }
        return null
    }
    fun check(urlTube : URL) : Boolean{
        return try{ true }catch(e : FileNotFoundException){ false }catch (e : InvocationTargetException) { false }
    }
}