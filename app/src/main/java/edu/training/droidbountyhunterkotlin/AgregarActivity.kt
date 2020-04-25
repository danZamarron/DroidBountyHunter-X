package edu.training.droidbountyhunterkotlin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import kotlinx.android.synthetic.main.activity_agregar.*

class AgregarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_agregar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_grabar) {
            guardarFugitivoToolbar()
        }
        return super.onOptionsItemSelected(item)
    }


    fun guardarFugitivoPresionado(view: View)
    {
     val nombre = nombreFugitivoTextView.text.toString()
        if(nombre.isNotEmpty())
        {
            val database = DatabaseBountyHunter(this)
            database.insertaFugitivo(Fugitivo(0,nombre,0))
            //Define que resultado tuvo
            setResult(0)
            //mata actividad y regresa
            finish()
        }
        else
        {
            AlertDialog.Builder(this).setTitle("Alerta!").setMessage("Agrege nombre del Fugitivo").show()
        }
    }

    fun guardarFugitivoToolbar()
    {
        val nombre = nombreFugitivoTextView.text.toString()
        if(nombre.isNotEmpty())
        {
            val database = DatabaseBountyHunter(this)
            database.insertaFugitivo(Fugitivo(0,nombre,0))
            //Define que resultado tuvo
            setResult(0)
            //mata actividad y regresa
            finish()
        }
        else
        {
            AlertDialog.Builder(this).setTitle("Alerta!").setMessage("Agrege nombre del Fugitivo").show()
        }
    }
}