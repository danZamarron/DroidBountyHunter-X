package edu.training.droidbountyhunterkotlin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import edu.training.droidbountyhunterkotlin.utils.PictureTools
import edu.training.droidbountyhunterkotlin.R as miLayout

class FugitivoCustomListAdapter(
    val context: Context,
    fugitivoList: List<Fugitivo>
) :
    BaseAdapter() {


    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mainContext: Context? = null
    private var fList: List<Fugitivo> = ArrayList()

    init {
        fList = fugitivoList;
    }

    override fun getCount(): Int {
        return fList.size
    }

    override fun getItem(position: Int): Any {
        return fList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(miLayout.layout.item_fugitivo_list_upgrade, parent, false)

        val nombreTextView = rowView.findViewById(miLayout.id.textNombreUpgrade) as TextView
        val fechaTextView = rowView.findViewById(miLayout.id.textFechaUpgrade) as TextView
        val imgView = rowView.findViewById(miLayout.id.imgFugitivo) as ImageView

        val fugitivo: Fugitivo = getItem(position) as Fugitivo

        nombreTextView.text = fugitivo.name
        fechaTextView.text =
            fugitivo.dateCapture.ifEmpty { context.getString(miLayout.string.nocapturado) }

        if (fugitivo.photo.isNotEmpty()) {
            val bitmap = PictureTools.decodeSampledBitmapFromUri(fugitivo.photo, 200, 200)
            imgView.setImageBitmap(bitmap);
        }
        return rowView
    }

}

