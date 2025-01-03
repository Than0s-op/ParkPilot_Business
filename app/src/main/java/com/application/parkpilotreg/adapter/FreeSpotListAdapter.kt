package com.application.parkpilotreg.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.application.parkpilotreg.FreeSpot
import com.application.parkpilotreg.R
import com.application.parkpilotreg.activity.AddFreeSpot
import com.application.parkpilotreg.module.firebase.database.FreeSpotStore
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator

class FreeSpotListAdapter(
    private val context: Context,
    private val layout: Int,
    private val freeSpotList: List<FreeSpot>,
    private val onDeleteClick: (String) -> Unit,
) : RecyclerView.Adapter<FreeSpotListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewLandMark: TextView = itemView.findViewById(R.id.textViewLandMark)
        val buttonDelete: MaterialButton = itemView.findViewById(R.id.buttonDelete)
        val card: CardView = itemView.findViewById(R.id.card)
        val progressCircular: CircularProgressIndicator =
            itemView.findViewById(R.id.progress_circular)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return freeSpotList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val freeSpot = freeSpotList[position]
        holder.textViewLandMark.text = freeSpot.landMark
        holder.buttonDelete.setOnClickListener {
            // Handle delete button click
            showDialog {
                holder.buttonDelete.visibility = View.GONE
                holder.progressCircular.visibility = View.VISIBLE
                onDeleteClick(freeSpot.id)
            }
        }
        holder.card.setOnClickListener {
            val intent = Intent(context, AddFreeSpot::class.java)
            intent.putExtra("id", freeSpot.id)
            context.startActivity(intent)
        }
    }

    private fun showDialog(onAccepted: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete this item?")
            .setNegativeButton("decline") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("accept") { dialog, which ->
                onAccepted()
                dialog.dismiss()
            }
            .show()
    }
}