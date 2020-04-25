package edu.training.droidbountyhunterkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings.Secure
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.widget.Toast
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import edu.training.droidbountyhunterkotlin.network.NetworkServices
import edu.training.droidbountyhunterkotlin.network.onTaskListener
import edu.training.droidbountyhunterkotlin.utils.PictureTools
import edu.training.droidbountyhunterkotlin.utils.PictureTools.Companion.MEDIA_TYPE_IMAGE
import kotlinx.android.synthetic.main.activity_detalle.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DetalleActivity : AppCompatActivity(), LocationListener {

    var fugitivo: Fugitivo? = null
    var database: DatabaseBountyHunter? = null
    private var UDID: String? = ""

    private var direccionImagen: Uri? = null
    private val REQUEST_CODE_PHOTO_IMAGE = 1787
    private val REQUEST_CODE_GPS = 1234
    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)

        @SuppressLint("HardwareIds")
        UDID = Secure.getString(contentResolver, Secure.ANDROID_ID)
        //UDID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


        fugitivo = intent.extras["fugitivo"] as Fugitivo

        //Nombre del Fugitivo del Intent y se usa como Titulo
        //title = intent.extras["titulo"] as CharSequence?
        //title = fugitivo!!.name + " - " + fugitivo!!.id
        title = String.format(
            getString(R.string.detalleNombreId),
            fugitivo!!.name,
            fugitivo!!.id.toString()
        )

        //Check si es Fugitivo o Capturado
        if (fugitivo!!.status == 0) {
            etiquetaMensaje.text = getString(R.string.fugitivoSuelto)
            activarGPS()
        } else {
            etiquetaMensaje.text = getString(R.string.fugitivoAtrapado)
            botonCapturar.visibility = View.GONE


            if(fugitivo!!.photo.isNotEmpty())
            {
                val bitmap = PictureTools.decodeSampledBitmapFromUri(fugitivo!!.photo, 200,200)
                pictureFugitive.setImageBitmap(bitmap);
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detalle, menu)

        var menuItemCapturar = menu.findItem(R.id.toolbarCapturar)
        var menuItemFoto = menu.findItem(R.id.toolbarFoto)
        if (fugitivo!!.status == 1) {
            menuItemCapturar.setVisible(false)
            menuItemFoto.setVisible(false)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when(item.itemId)
        {
            R.id.toolbarCapturar -> capturarFugitivoPresionado()
            R.id.toolbarEliminar -> eliminarFugitivoPresionado()
            R.id.toolbarFoto -> OnFotoClick()
            R.id.toobarGPS -> OnMapClick()
        }
        return super.onOptionsItemSelected(item)
    }



    fun capturarFugitivoPresionado() {
        database = DatabaseBountyHunter(this)
        fugitivo!!.status = 1
        if(fugitivo!!.photo.isEmpty())
        {
            Toast.makeText(
                applicationContext,
                "Es necesario tomar la foto antes de capturar al fugiivo",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val pattern = "dd / MM / yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(Date())

        //Api Lvl 26+
        //fugitivo!!.dateCapture = LocalDateTime.now().toString();
        fugitivo!!.dateCapture = date
        database!!.actualizarFugitivo(fugitivo!!)

        val services = NetworkServices(object : onTaskListener {
            override fun tareaCompletada(respuesta: String) {
                val obj = JSONObject(respuesta)
                val mensaje = obj.optString("mensaje", "")
                mensajeDeCerrado(mensaje)
            }

            override fun tareaConError(codigo: Int, mensaje: String, error: String) {
                Toast.makeText(
                    applicationContext,
                    "Ocurrio un problema en la comunicación con el WebService!!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        services.execute("Atrapar", UDID)
        botonCapturar.visibility = GONE
        botonEliminar.visibility = GONE
        setResult(0)
        finish()
    }

    fun eliminarFugitivoPresionado() {
        database = DatabaseBountyHunter(this)
        database!!.borraFugitivo(fugitivo!!)
        setResult(0)
        finish()
    }

    fun mensajeDeCerrado(mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.create()
        builder.setTitle("Alerta!!!")
            .setMessage(mensaje)
            .setOnDismissListener {
                setResult(fugitivo!!.status)
                finish()
            }.show()
    }

    fun OnFotoClick() {
        if (PictureTools.permissionReadMemmory(this)) {
            obtenFotoDeCamara()
        }
    }

    fun OnMapClick()
    {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("fugitivo",fugitivo)
        startActivity(intent)
    }

    private fun obtenFotoDeCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        direccionImagen = PictureTools.getOutputMediaFileUri(this, MEDIA_TYPE_IMAGE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, direccionImagen)
        startActivityForResult(intent, REQUEST_CODE_PHOTO_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PHOTO_IMAGE) {
            //Recibimos el resultado esperado del Intent
            if (resultCode == Activity.RESULT_OK) {
                //Salio todo bien en la activity

                fugitivo!!.photo = PictureTools.currentPhotoPath
                val bitMap = PictureTools.decodeSampledBitmapFromUri(PictureTools.currentPhotoPath,200,200)
                pictureFugitive.setImageBitmap(bitMap)
            }
        }
        else if (resultCode == REQUEST_CODE_GPS)
        {
            activarGPS()
        }
    }

    override fun onLocationChanged(location: Location?) {
        fugitivo!!.latitude = location!!.latitude
        fugitivo!!.logitude = location!!.longitude
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onProviderEnabled(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onProviderDisabled(p0: String?) {
        TODO("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    private fun activarGPS()
    {
        if(isGPSActivated())
        {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,0f, this)
            Toast.makeText(this,"Activando GPS...", Toast.LENGTH_LONG).show()

            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE

            //Best Provider
            val provider = locationManager!!.getBestProvider(criteria, true)
            //Get Last Location
            val location = locationManager!!.getLastKnownLocation(provider)

            if(location != null)
            {
                fugitivo!!.latitude = location.latitude
                fugitivo!!.logitude = location.longitude
            }
        }
    }

    private fun apagarGPS() {
        if(locationManager != null)
        {
            try {
                locationManager!!.removeUpdates(this)
                Toast.makeText(this,"Apagando GPS...", Toast.LENGTH_LONG).show()
            }
            catch(exception: SecurityException)
            {
                Toast.makeText(this,"Hubo error al Apagar GPS...$exception", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun isGPSActivated() : Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                //Deberiamos explicar al usuario porque lo necesitamos
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_GPS)
                    return false
                }
                else
                {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_GPS)
                    return false
                }
            }
            else {
                return true
            }
        }
        else {
            return true
        }
    }

    override fun onDestroy() {
        apagarGPS()
        pictureFugitive.setImageBitmap(null)
        System.gc()
        super.onDestroy()
    }



}