package com.example.sfa.presentation.ui.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sfa.data.model.Question
import com.example.sfa.data.model.Section
import com.example.sfa.databinding.ItemSectionBinding

class SectionFormAdapter (
    val sections: MutableList<Section>,
    private val onDeleteSection: (Int) -> Unit
) : RecyclerView.Adapter<SectionFormAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val binding = ItemSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SectionViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        if (position in sections.indices) {
            val section = sections[position]
            holder.bind(section, position, onDeleteSection)
        }
    }

    override fun getItemCount(): Int = sections.size

    class SectionViewHolder(
        private val binding: ItemSectionBinding,
        private val adapter: SectionFormAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var questionAdapter: QuestionFormAdapter

        fun bind(section: Section, position: Int, onDeleteSection: (Int) -> Unit) {
            binding.sectionTitle.setText(section.sectionName)

            questionAdapter = QuestionFormAdapter(section.questions) { questionIndex ->
                section.questions.removeAt(questionIndex)
                questionAdapter.notifyItemRemoved(questionIndex)
                questionAdapter.notifyItemRangeChanged(questionIndex, section.questions.size)
            }

            binding.questionRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            binding.questionRecyclerView.adapter = questionAdapter

            binding.sectionTitle.addTextChangedListener {
                section.sectionName = it.toString()
            }

            binding.addQuestionButton.setOnClickListener {
                section.questions.add(Question(label = "", hint = "Enter answer"))
                questionAdapter.notifyItemInserted(section.questions.size - 1)
            }

            binding.deleteSectionButton.setOnClickListener {
                if (position in adapter.sections.indices) {
                    onDeleteSection(position)
                }
            }
        }
    }
}
