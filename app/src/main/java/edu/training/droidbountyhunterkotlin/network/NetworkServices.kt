package edu.training.droidbountyhunterkotlin.network

import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

const val GET = "GET"
const val POST = "POST"
const val CONTENT_TYPE = "Content-Type"
const val TYPE_JSON = "application/json"

class NetworkServices(val listener: onTaskListener) : AsyncTask<String, Void, Boolean>() {
    private val TAG = NetworkServices::class.java.simpleName

    //Get
    //private val endpoint_fugitivos = "http://3.13.226.218/droidBHServices.svc/fugitivos"
    private val endpoint_fugitivos = "http://3.13.226.218/droidBHServices.svc/fugitivos"

    //Post
    //private val endpoint_atrapados = "http://3.13.226.218/droidBHServices.svc/atrapados"
    private val endpoint_atrapados = "http://3.13.226.218/droidBHServices.svc/atrapados"

    private var JSONStr: String = ""
    private var tipo: SERVICE_TYPE = SERVICE_TYPE.FUGITIVOS
    private var codigo: Int = 0
    private var mensaje: String = ""
    private var error: String = ""


    override fun doInBackground(vararg params: String?): Boolean {
        val esFugitivo = params[0]!!.equals("Fugitivos", true)

        tipo = if (esFugitivo) SERVICE_TYPE.FUGITIVOS else SERVICE_TYPE.ATRAPADOS

        var urlConnection: HttpURLConnection? = null
        var stringWebService: String = if (esFugitivo) endpoint_fugitivos else endpoint_atrapados
        var paramsString = if (params.size > 1) params[1]!! else ""

        try {
            urlConnection = getStructuredRequest(tipo, stringWebService, paramsString)
            //?: <-- Elvis Operator
            val inputStream = urlConnection.inputStream ?: return false
            val reader = BufferedReader(InputStreamReader(inputStream))
            val buffer = StringBuffer()

            do {
                val line: String? = reader.readLine()
                if (line != null)
                buffer.append(line).append("\n")
                    //buffer.appendln(line)
            } while (line != null)

            if (buffer.isEmpty())
                return false

            JSONStr = buffer.toString()
            Log.d(TAG, "Respuesta del Servidor: $JSONStr")
            return true

        } catch (e: FileNotFoundException) {
            manageError(urlConnection)
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            manageError(urlConnection)
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            manageError(urlConnection)
            e.printStackTrace()
            return false
        } finally {
            urlConnection?.disconnect()
        }

    }

    override fun onProgressUpdate(vararg values: Void?) {
        super.onProgressUpdate(*values)
    }

    @Throws(IOException::class, JSONException::class)
    private fun getStructuredRequest(type: SERVICE_TYPE, endpoint: String, id: String
    ): HttpURLConnection {
        val TIME_OUT = 5000
        val urlConnection: HttpURLConnection
        val url: URL?
        if (type === SERVICE_TYPE.FUGITIVOS) { //------ GET Fugitivos----------
            url = URL(endpoint)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.readTimeout = TIME_OUT
            urlConnection.requestMethod = GET
            urlConnection.setRequestProperty(CONTENT_TYPE, TYPE_JSON)
            urlConnection.connect()
        } else { //--------------------- POST Atrapados------------------------
            url = URL(endpoint)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.readTimeout = TIME_OUT
            urlConnection.requestMethod = POST
            urlConnection.setRequestProperty(CONTENT_TYPE, TYPE_JSON)
            urlConnection.doInput = true
            urlConnection.doOutput = true
            urlConnection.connect()

            val jsonObject = JSONObject()
            jsonObject.put("UDIDString", id)

            val dataOutputStream = DataOutputStream(urlConnection.outputStream)
            dataOutputStream.write(jsonObject.toString().toByteArray())
            dataOutputStream.flush()
            dataOutputStream.close()
        }
        Log.d(TAG, url.toString())
        return urlConnection
    }

    private fun manageError(urlConnection: HttpURLConnection?) {
        if (urlConnection != null) {
            try {
                codigo = urlConnection.responseCode
                if (urlConnection.errorStream != null) {
                    val inputStream = urlConnection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val buffer = StringBuffer()
                    do
                    {
                        val line: String? = reader.readLine()
                        if (line != null)
                            buffer.append(line).append("\n")
                    }
                    while (line != null)
                    error = buffer.toString()
                } else {
                    mensaje = urlConnection.responseMessage
                }
                error = urlConnection.errorStream.toString()
                Log.e(TAG, "Error: $error, code: $codigo")
            } catch (e1: IOException) {
                e1.printStackTrace()
                Log.e(TAG, "Error")
            }
        } else {
            codigo = 105
            mensaje = "Error: No internet connection"
            Log.e(TAG, "code: $codigo, $mensaje")
        }
    }

    override fun onPostExecute(result: Boolean?) {
        if (result!!){
            listener.tareaCompletada(JSONStr)
        }else{
            listener.tareaConError(codigo, mensaje, error)
        }
    }

}

enum class SERVICE_TYPE {
    FUGITIVOS,
    ATRAPADOS
}

interface onTaskListener {
    fun tareaCompletada(respuesta: String)
    fun tareaConError(codigo: Int, mensaje: String, error: String)
}