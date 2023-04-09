package com.example.tea

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.tea.databinding.ArtcileFragmentItemBinding

import com.example.tea.placeholder.PlaceholderContent.PlaceholderItem
import com.example.tea.databinding.FragmentItemBinding
import com.example.tea.models.article.Article
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList

class DraftRecyclerViewAdapter(
    private val values: List<Article>,
    private val context: FragmentActivity?
) : RecyclerView.Adapter<DraftRecyclerViewAdapter.ViewHolder>() {

    var filterList = ArrayList<Article>()

    init {
        filterList = values as ArrayList<Article>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ArtcileFragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.headerView.text = item.title
        holder.contentView.text = item.description
        holder.userNameView.text = item.login

        val text = "2022-01-06 20:30:45"
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val localDateTime = LocalDateTime.parse(text, pattern)

        holder.articleDate.text = localDateTime.dayOfMonth.toString() + " " + localDateTime.month + " " + localDateTime.year.toString()

        if(item.photo.length > 100){
            holder.articleImageView.visibility = ImageView.VISIBLE
            holder.articleImageView.setImageBitmap(convert(item.photo))
        }
        else{
            holder.articleImageView.visibility = ImageView.GONE
        }

        holder.articleCard.setOnClickListener {
            val intent = Intent(context, DraftActivity::class.java)
            intent.putExtra("id", item.id)
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = filterList.size

    inner class ViewHolder(binding: ArtcileFragmentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val headerView: TextView = binding.articleHeader
        val contentView: TextView = binding.articleText
        val userNameView: TextView = binding.userNameHeader
        val articleImageView : ImageView = binding.articleImage
        val articleDate : TextView = binding.articleDate
        val articleCard : LinearLayout = binding.articleCard

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    @Throws(IllegalArgumentException::class)
    fun convert(base64Str: String): Bitmap? {
        val decodedBytes: ByteArray = android.util.Base64.decode(
            base64Str.substring(base64Str.indexOf(",") + 1),
            android.util.Base64.DEFAULT
        )
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

}