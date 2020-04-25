package edu.training.droidbountyhunterkotlin.network

import android.content.Context
import android.util.Log
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import org.json.JSONArray
import org.json.JSONException

val TAG : String = JSONUtils::class.java.simpleName

class JSONUtils {

    companion object{

        fun parsearFugitivos(respuesta: String, context: Context) : Boolean
        {
            val database = DatabaseBountyHunter(context)

            Log.w(TAG, "Entre a ParsearFugitivos")
            try
            {
                val array = JSONArray(respuesta)

                for (i in 0 until array.length())
                {
                    val jsonObj = array.getJSONObject(i)
                    val nombreFugitivo = jsonObj.optString("name","")
                    database.insertaFugitivo(Fugitivo(0, nombreFugitivo, 0))
                }
            }
            catch(e: JSONException)
            {
                e.printStackTrace()
                Log.e(TAG, "Fallo Json en pasearFugitivos")
                return false;
            }
            finally {
                return true
            }
        }


    }

}