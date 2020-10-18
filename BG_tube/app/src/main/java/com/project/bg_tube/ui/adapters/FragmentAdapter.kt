package com.project.bg_tube.ui.adapters

import android.content.Context
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bumptech.glide.Glide
import com.project.bg_tube.R
import com.project.bg_tube.data.database.PlayListDataBase
import com.project.bg_tube.data.request.PlayList
import com.project.bg_tube.ui.adapters.listener.OnItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import org.json.JSONObject
import java.io.FileNotFoundException
import java.lang.reflect.InvocationTargetException
import java.net.URL

class FragmentAdapter(context: Context, listener: OnItemClickListener) : RecyclerView.Adapter<FragmentAdapter.FragmentViewHolder>() {

    var listener:OnItemClickListener ?= null
    var list: ArrayList<PlayList>? = null
    var context: Context? = null
    private var positionCheck = 0
    private var isStartViewCheck = true

    init {
        this.context = context
        this.listener = listener
    }



    fun setData(list: ArrayList<PlayList>) {
        this.list = list
        notifyDataSetChanged()
    }
    fun getData() : ArrayList<PlayList> {
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
        val playing: PlayList? = list?.get(position)


        holder.textTitle?.text = playing!!.title
        holder.contentAuthor.text = playing!!.author_name
        Glide.with(context!!.applicationContext).load(playing!!.thumbnail_url).centerCrop().into(holder.thumbnailImage)


        GlobalScope.async {
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

            positionCheck = position
        }
    }
    private fun getListData(): List<PlayList> {
        val db = Room.databaseBuilder(
            context!!,
            PlayListDataBase::class.java, "PlayListDB"
        ).build()

        return db.playListDAO().getAll()
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
}