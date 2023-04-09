package com.example.tea.ui.drafts

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tea.ArticleItemRecyclerViewAdapter
import com.example.tea.DraftRecyclerViewAdapter
import com.example.tea.database.DatabaseHelper
import com.example.tea.databinding.FragmentDraftBinding
import com.example.tea.models.article.Article
import java.util.ArrayList

class DraftFragment : Fragment() {

    lateinit var adapter: DraftRecyclerViewAdapter
    private lateinit var articlesRv: RecyclerView

    private var _binding: FragmentDraftBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val draftViewModel =
            ViewModelProvider(this).get(DraftViewModel::class.java)

        _binding = FragmentDraftBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadArticles()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initAdapter(articles: ArrayList<Article>) {
        adapter = DraftRecyclerViewAdapter(articles, activity)
        articlesRv = binding.list
        articlesRv.adapter = adapter
    }

    private fun loadArticles() {

        val db = DatabaseHelper(activity, null)

        val artciles = db.getArticles()


        if(artciles?.count() == 0){
            binding.draftNothingShow.text = "Нет публикаций"
            binding.draftNothingShow.visibility = TextView.VISIBLE
            binding.list.visibility = TextView.GONE
            return
        }

        // создаем адаптер
        initAdapter(artciles)

    }
}