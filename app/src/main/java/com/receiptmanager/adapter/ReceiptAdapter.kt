package com.receiptmanager.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.receiptmanager.R
import com.receiptmanager.model.Receipt
import com.receiptmanager.util.CategoryHelper
import com.receiptmanager.util.FileManager

class ReceiptAdapter(
    private val context: Context,
    private var receipts: MutableList<Receipt>,
    private val onOpen: (Receipt) -> Unit,
    private val onShare: (Receipt) -> Unit,
    private val onDelete: (Receipt) -> Unit
) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconCircle: FrameLayout = view.findViewById(R.id.iconCircle)
        val ivFileIcon: ImageView   = view.findViewById(R.id.ivFileIcon)
        val tvTitle: TextView       = view.findViewById(R.id.tvTitle)
        val tvDate: TextView        = view.findViewById(R.id.tvDate)
        val tvCategory: TextView    = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView      = view.findViewById(R.id.tvAmount)
        val btnShare: ImageButton   = view.findViewById(R.id.btnShare)
        val btnDelete: ImageButton  = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_receipt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receipt = receipts[position]
        val mime = FileManager.getMimeType(receipt.filePath)

        holder.tvTitle.text = receipt.title
        holder.tvDate.text  = receipt.date

        // Amount
        if (receipt.amount > 0) {
            holder.tvAmount.visibility = View.VISIBLE
            holder.tvAmount.text = "₹%.2f".format(receipt.amount)
        } else {
            holder.tvAmount.visibility = View.GONE
        }

        // Category badge
        val emoji = CategoryHelper.getEmoji(receipt.category)
        holder.tvCategory.text = "$emoji ${receipt.category}"
        val bg = android.graphics.drawable.GradientDrawable()
        bg.cornerRadius = 40f
        bg.setColor(Color.parseColor("#22${CategoryHelper.getColor(receipt.category)}"))
        holder.tvCategory.background = bg
        holder.tvCategory.setTextColor(CategoryHelper.getColorInt(receipt.category))

        // File icon
        when {
            mime == "application/pdf" -> {
                holder.iconCircle.setBackgroundResource(R.drawable.circle_bg_pdf)
                holder.ivFileIcon.setImageResource(android.R.drawable.ic_menu_agenda)
                holder.ivFileIcon.setColorFilter(context.getColor(R.color.colorIconPdf))
            }
            mime?.startsWith("image/") == true -> {
                holder.iconCircle.setBackgroundResource(R.drawable.circle_bg_image)
                holder.ivFileIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                holder.ivFileIcon.setColorFilter(context.getColor(R.color.colorIconImage))
            }
            mime?.contains("word") == true -> {
                holder.iconCircle.setBackgroundResource(R.drawable.circle_bg_doc)
                holder.ivFileIcon.setImageResource(android.R.drawable.ic_menu_edit)
                holder.ivFileIcon.setColorFilter(context.getColor(R.color.colorIconDoc))
            }
            else -> {
                holder.iconCircle.setBackgroundResource(R.drawable.circle_bg_image)
                holder.ivFileIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                holder.ivFileIcon.setColorFilter(context.getColor(R.color.colorIconDefault))
            }
        }

        holder.itemView.setOnClickListener { onOpen(receipt) }
        holder.btnShare.setOnClickListener  { onShare(receipt) }
        holder.btnDelete.setOnClickListener { onDelete(receipt) }
    }

    override fun getItemCount() = receipts.size

    fun updateData(newList: List<Receipt>) {
        receipts.clear()
        receipts.addAll(newList)
        notifyDataSetChanged()
    }
}