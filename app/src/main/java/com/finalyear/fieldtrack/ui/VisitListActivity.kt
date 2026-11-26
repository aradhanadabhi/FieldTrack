package com.finalyear.fieldtrack.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finalyear.fieldtrack.databinding.ActivityVisitListBinding
import com.finalyear.fieldtrack.db.DatabaseHelper
import com.finalyear.fieldtrack.model.Visit

class VisitListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisitListBinding
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: VisitAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper.getInstance(this)

        adapter = VisitAdapter(
            onEdit = { visit -> showEditDialog(visit) },
            onDelete = { visit ->
                db.deleteVisit(visit.id)
                loadVisits()
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadVisits()
    }

    private fun loadVisits() {
        // ---------- SELECT ----------
        val visits = db.getAllVisits()
        adapter.submitList(visits)
        binding.emptyText.visibility =
            if (visits.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showEditDialog(visit: Visit) {
        val input = android.widget.EditText(this).apply { setText(visit.notes) }
        AlertDialog.Builder(this)
            .setTitle("Edit notes for ${visit.siteName}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                // ---------- UPDATE ----------
                db.updateVisit(visit.copy(notes = input.text.toString()))
                loadVisits()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

/**
 * Simple RecyclerView adapter for the visit list.
 */
class VisitAdapter(
    private val onEdit: (Visit) -> Unit,
    private val onDelete: (Visit) -> Unit
) : RecyclerView.Adapter<VisitAdapter.VisitViewHolder>() {

    private var items: List<Visit> = emptyList()

    fun submitList(newItems: List<Visit>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VisitViewHolder {
        val binding = com.finalyear.fieldtrack.databinding.ItemVisitBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return VisitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        holder.bind(items[position], onEdit, onDelete)
    }

    override fun getItemCount() = items.size

    class VisitViewHolder(
        private val binding: com.finalyear.fieldtrack.databinding.ItemVisitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(visit: Visit, onEdit: (Visit) -> Unit, onDelete: (Visit) -> Unit) {
            binding.siteNameText.text = visit.siteName
            binding.notesText.text = visit.notes
            binding.coordsText.text = String.format(
                java.util.Locale.US, "%.5f, %.5f", visit.latitude, visit.longitude
            )
            binding.syncText.text = if (visit.synced == 1) "Synced" else "Pending sync"
            binding.btnEdit.setOnClickListener { onEdit(visit) }
            binding.btnDelete.setOnClickListener { onDelete(visit) }
        }
    }
}
