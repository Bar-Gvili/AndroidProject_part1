package com.example.temple_run.adapter // Or your actual package name

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.temple_run.databinding.ItemHighScoreBinding // This will be generated from item_high_score.xml
import com.example.temple_run.model.Score // Your Score model

class HighScoreAdapter(
    private val scores: List<Score>,
    private val onItemClicked: (Score) -> Unit // Lambda to handle item click
) :
    RecyclerView.Adapter<HighScoreAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(
        private val binding: ItemHighScoreBinding,
        private val onItemClicked: (Score) -> Unit // Pass lambda to ViewHolder
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(score: Score, rank: Int) {
            binding.itemTextViewRank.text = "${rank}."
            binding.itemTextViewScore.text = "Score: ${score.score}"
            binding.itemTextViewDate.text = score.getFormattedDate()
            binding.itemTextViewLocation.text = "Loc: (${"%.2f".format(score.latitude)}, ${"%.2f".format(score.longitude)})"

            // 2. Set click listener on the item's root view
            binding.root.setOnClickListener {
                onItemClicked(score)
            }
        }
    }

    // Called when RecyclerView needs a new ViewHolder (a new row for the list)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val binding = ItemHighScoreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScoreViewHolder(binding, onItemClicked)
    }

    // Called by RecyclerView to display the data at the specified position
    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position]
        holder.bind(score, position + 1) // Pass the score and its rank (position + 1) to the ViewHolder
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int {
        return scores.size
    }
}