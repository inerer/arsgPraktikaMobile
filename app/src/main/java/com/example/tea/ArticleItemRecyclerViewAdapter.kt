package com.example.tea

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.tea.databinding.ArtcileFragmentItemBinding
import com.example.tea.models.article.Article
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ArticleItemRecyclerViewAdapter(
    private val values: List<Article>,
    private val context: FragmentActivity?
) : RecyclerView.Adapter<ArticleItemRecyclerViewAdapter.ViewHolder>(), Filterable {

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

    override fun getFilter(): Filter{
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filterList = values as ArrayList<Article>
                } else {
                    val resultList = ArrayList<Article>()
                    for (row in values) {
                        if ((row.title.lowercase()
                                .contains(charSearch.lowercase()) || row.login.lowercase()
                                    .contains(charSearch.lowercase()))
                        )
                        {
                            resultList.add(row)
                        }
                    }
                    filterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filterList = values as ArrayList<Article>
                notifyDataSetChanged()
            }

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.headerView.text = item.title
        holder.contentView.text = item.description
        holder.userNameView.text = item.login

        try {
            val text = item.dateOfPublication
            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            val localDateTime = LocalDateTime.parse(text, pattern)

            holder.articleDate.text = localDateTime.dayOfMonth.toString() + " " + localDateTime.month + " " + localDateTime.year.toString()
        }
        catch (e : java.lang.Exception){
            val text = item.dateOfPublication
            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val localDateTime = LocalDateTime.parse(text, pattern)

            holder.articleDate.text = localDateTime.dayOfMonth.toString() + " " + localDateTime.month + " " + localDateTime.year.toString()
        }



        if(item.photo.length > 100){
            holder.articleImageView.visibility = ImageView.VISIBLE
            holder.articleImageView.setImageBitmap(convert(item.photo))
        }
        else{
            holder.articleImageView.visibility = ImageView.GONE
        }

        holder.articleCard.setOnClickListener {
            val intent = Intent(context, ArticleActivity::class.java)
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