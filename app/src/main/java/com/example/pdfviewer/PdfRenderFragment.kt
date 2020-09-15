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
 *   File name: PdfRenderFragment.kt
 *
 *   Author: yogeshwaran
 *
 *   Created On: 8/8/20 6:38 PM
 *
 *   Abstract: PdfRenderFragment is a Fragment to render pdf files
 *
 *   Environment: Mobile Profile :Android
 *
 *  *
 *  * Notes:
 *  *
 *  * Revision History:
 *  * Created PdfRenderFragment
 *  *
 *
 */

package com.example.pdfviewer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfRenderFragment : Fragment() {
    private lateinit var fileDescriptor: ParcelFileDescriptor
    private lateinit var pdfRenderer: PdfRenderer
    private var currentPage: PdfRenderer.Page? = null

    companion object {
        fun newInstance(): PdfRenderFragment {
            return PdfRenderFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val file = File(requireContext().cacheDir, "sample pdf.pdf")
        if (!file.exists()) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into the cache directory.
            val asset = requireContext().assets?.open("sample pdf.pdf")
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var size: Int
            while (asset?.read(buffer).also { size = it!! } != -1) {
                output.write(buffer, 0, size)
            }
            asset?.close()
            output.close()
        }
        fileDescriptor =
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
        setAdapter()
    }

    private fun setAdapter() {
        val pdfAdapter = PdfRenderAdapter()
        documentPageList.layoutManager = LinearLayoutManager(context)
        pdfAdapter.setList(renderPdf(getPageCount()))
        documentPageList.adapter = pdfAdapter
    }

    private fun renderPdf(pageCount: Int): List<Bitmap> {
        val list = ArrayList<Bitmap>()
        for (index in 0 until pageCount) {
            list.add(getPage(index))
        }
        return list
    }

    private fun getPageCount() = pdfRenderer.pageCount

    private fun getPage(index: Int): Bitmap {
        currentPage?.close()
        currentPage = pdfRenderer.openPage(index)
        val bitmap = Bitmap.createBitmap(
            (currentPage as PdfRenderer.Page).width, (currentPage as PdfRenderer.Page).height,
            Bitmap.Config.ARGB_8888
        )
        (currentPage as PdfRenderer.Page).render(
            bitmap,
            null,
            null,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
        )
        return bitmap
    }

    private fun closeRenderer() {
        currentPage?.let { currentPage?.close() }
        pdfRenderer.close()
        fileDescriptor.close()
    }

    override fun onDestroy() {
        try {
            closeRenderer()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}