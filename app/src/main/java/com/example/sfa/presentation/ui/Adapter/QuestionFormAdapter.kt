package com.example.sfa.presentation.ui.Adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sfa.data.model.Question
import com.example.sfa.databinding.ItemQuestionBinding

class QuestionFormAdapter (
    private val questions: MutableList<Question>,
    private val onDeleteQuestion: (Int) -> Unit
) : RecyclerView.Adapter<QuestionFormAdapter.QuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], position, onDeleteQuestion)
    }

    override fun getItemCount(): Int = questions.size

    class QuestionViewHolder(private val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question, position: Int, onDeleteQuestion: (Int) -> Unit) {
            binding.questionLabel.setText(question.label)
            binding.editText.setText(question.value)

            binding.questionLabel.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    question.label = s.toString()
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            binding.editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    question.value = s.toString()
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            binding.deleteQuestionButton.setOnClickListener {
                onDeleteQuestion(adapterPosition)
            }
        }
    }
}