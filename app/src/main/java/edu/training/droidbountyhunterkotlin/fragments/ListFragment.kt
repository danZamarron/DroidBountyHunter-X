package edu.training.droidbountyhunterkotlin.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import edu.training.droidbountyhunterkotlin.DetalleActivity
import edu.training.droidbountyhunterkotlin.R
import edu.training.droidbountyhunterkotlin.adapters.FugitivoCustomListAdapter
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import edu.training.droidbountyhunterkotlin.network.JSONUtils
import edu.training.droidbountyhunterkotlin.network.NetworkServices
import edu.training.droidbountyhunterkotlin.network.onTaskListener
import kotlinx.android.synthetic.main.fragment_list.*

const val SECTION_NUMBER: String = "section_number"

class ListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //!! (Double Bang) es para que si llega null saque un error de Null Pointer Expression
        //!! Es por que puede ser null pero yo se que no es null y lo forzo
        val modo = arguments!![SECTION_NUMBER] as Int

//        val dummyData = listOf(
//            "Juan X",
//            "Pedro Y",
//            "Paco Z",
//            "Hugo A",
//            "Luis B",
//            "Karen M"
//        )
//        val adaptador = ArrayAdapter<String>(context, R.layout.item_fugitivo_list, dummyData)
//        listaFugitivosCapturados.adapter = adaptador

        actualizarDatos(listaFugitivosCapturados, modo)

        listaFugitivosCapturados.setOnItemClickListener { adapterView, view,
                                                          position, id ->

              //Cuando se manda uno por uno en el intent
//            val intent = Intent(context, DetalleActivity::class.java)
//            intent.putExtra("titulo",(view as TextView).text)
//            intent.putExtra("modo", modo)
//            startActivity(intent)

            //cuando se empaqueta todo para enviar de un golpe
            val intent = Intent(context, DetalleActivity::class.java)
            val fugitivos = listaFugitivosCapturados.tag as Array<Fugitivo>
            intent.putExtra("fugitivo", fugitivos[position])
            startActivityForResult(intent, 0)

        }

    }

    private fun actualizarDatos(listView: ListView, modo: Int)
    {
        val database = DatabaseBountyHunter(context!!)
        val fugitivos = database.obtenerFugitivos(modo)

        if(fugitivos.isNotEmpty())
        {
            val values = ArrayList<String>()
            fugitivos.mapTo(values)
            {elemento ->
                elemento.name
            }
            val adaptador = ArrayAdapter<String>(context, R.layout.item_fugitivo_list, values)
            //val adaptador = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, values)


            val values2 = fugitivos.toList()
            //val adaptador2 = FugitivoCustomListAdapter(context!!, R.layout.item_fugitivo_list_upgrade,values2);
            val adaptador2 = FugitivoCustomListAdapter(context!!,values2)

            listView.adapter = adaptador2
            listView.tag = fugitivos
        }
        else
        {
            //BD Vacios
            if(modo == 0) {

                val services = NetworkServices(object: onTaskListener{

                    override fun tareaCompletada(respuesta: String) {
                        JSONUtils.parsearFugitivos(respuesta, context!!)
                        actualizarDatos(listView, modo)
                    }

                    override fun tareaConError(codigo: Int, mensaje: String, error: String) {
                        Toast.makeText(context,
                            "Ocurrio un problema con el WebService!!! --- Código de error: $codigo \n Mensaje: $mensaje",
                            Toast.LENGTH_LONG)
                            .show();
                    }


                })

                services.execute("Fugitivos")

            }
        }
    }
}