/*
 * *
 *  * Copyright Trimble Inc., 2019 - 2020 All rights reserved.
 *  *
 *  * Licensed Software Confidential and Proprietary Information of Trimble Inc.,
 *   made available under Non-Disclosure Agreement OR License as applicable.
 *
 *   Product Name: MyMedia
 *
 *   Module Name: app
 *
 *   File name: PdfRenderAdapter.kt
 *
 *   Author: yogeshwaran
 *
 *   Created On: 8/8/20 6:38 PM
 *
 *   Abstract: Adapter to view pdf pages
 *
 *   Environment: Mobile Profile :Android
 *
 *  *
 *  * Notes:
 *  *
 *  * Revision History:
 *  *
 *  *
 *
 */
package com.example.pdfviewer

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PdfRenderAdapter : RecyclerView.Adapter<PdfRenderAdapter.PdfViewHolder>() {

    private var imageList: List<Bitmap> = ArrayList()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PdfViewHolder =
        PdfViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_pdf_page, p0, false))

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(viewHolder: PdfViewHolder, position: Int) =
        viewHolder.setImage(imageList[position])

    fun setList(imageList: List<Bitmap>) {
        this.imageList = imageList
        notifyDataSetChanged()
    }

    class PdfViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.pdf_image)
        fun setImage(bitmap: Bitmap) = imageView.setImageBitmap(bitmap)
    }
}