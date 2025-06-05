package com.example.temple_run.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.temple_run.adapter.HighScoreAdapter
// import com.example.temple_run.adapter.HighScoreAdapter // You'll create this
import com.example.temple_run.databinding.FragmentHighScoreListBinding // Assuming you have this binding
import com.example.temple_run.model.Score

class FragmentHighScoreList : Fragment() {

    // 1. Define the interface
    interface OnScoreSelectedListener {
        fun onScoreItemSelected(score: Score)
    }
    private var listener: OnScoreSelectedListener? = null

    private var _binding: FragmentHighScoreListBinding? = null
    private val binding get() = _binding!!

    private var scores: ArrayList<Score> = ArrayList()
     private lateinit var highScoreAdapter: HighScoreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            scores = it.getParcelableArrayList<Score>(ARG_SCORES) ?: ArrayList()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnScoreSelectedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnScoreSelectedListener")
        }
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHighScoreListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        if (scores.isNotEmpty()) {
            binding.recyclerViewHighScores.visibility = View.VISIBLE
            binding.recyclerViewHighScores.layoutManager = LinearLayoutManager(context)
            // 3. Pass the listener to the adapter
            highScoreAdapter = HighScoreAdapter(scores) { selectedScore ->
                listener?.onScoreItemSelected(selectedScore)
            }
            binding.recyclerViewHighScores.adapter = highScoreAdapter
        } else {
            Log.d("FragmentHighScoreList", "No scores to display in RecyclerView.")
            binding.recyclerViewHighScores.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SCORES = "scores_list_arg"

        @JvmStatic
        fun newInstance(scores: ArrayList<Score>) =
            FragmentHighScoreList().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_SCORES, scores)
                }
            }
    }
}