package com.id.wtf.wateringapps.models

import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.TimePicker
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.id.wtf.wateringapps.R
import com.id.wtf.wateringapps.utils.Session
import kotlinx.android.synthetic.main.activity_connect.*
import kotlinx.android.synthetic.main.activity_menu.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MenuActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var session: Session
    lateinit var jam : String
    lateinit var menit : String
    val URL ="http://iot.alwiyahyamuljabar.xyz/Android/getData.php"
    lateinit var pDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_menu)
        session= Session(applicationContext)

        //
        pDialog= ProgressDialog(this)
        pDialog.setMessage("Memuat...")
        pDialog.show()
        time()
        //

        //
        if(session.getValueString("jam") !=null){
            jam = session.getValueString("jam")!!
            menit= session.getValueString("menit")!!
            txtJadwal.text=jam+"."+menit
        }
        //

        //
        if(session.getValueBoolean("siram")!= null && session.getValueBoolean("siram") == true){
            switchSiram.isChecked=true
            btnJadwal.isEnabled=true
        }else if(session.getValueBoolean("siram")==false) {
            switchSiram.isChecked = false
            btnJadwal.isEnabled = false
        }
        //
        switchSiram.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener{ buttonView, isChecked ->

        if(isChecked){
            session.notif("siram",true)
            btnJadwal.isEnabled=true
            btnJadwal.setOnClickListener(this)
        }else {
            session.notif("siram", false)
            btnJadwal.isEnabled=false
        }
        })
        parseData()
    }

    private fun parseData(){
        val stringRequest = StringRequest(Request.Method.GET, URL,
            Response.Listener { response ->
                try {
                    val obj= JSONObject(response)
                    txtSuhu.text=obj.getString("temperature")
                    val hum=obj.getString("humidity")
                    txtWind.text=obj.getString("humidity")+"%"
                    txtTanggal.text=obj.getString("date")
                    pDialog.dismiss()
                }catch (a : JSONException){
                    a.printStackTrace()
                }
            },Response.ErrorListener {

            })
        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(stringRequest)
    }

    private fun time(){
        val sdf = SimpleDateFormat("HH:mm")
        val tanggal : String =sdf.format(Date())
        val pagi ="05:00"
        val siang = "11:00"
        val sore = "15:00"
        val malam = "18:00"
        val pagiku : Date = sdf.parse(pagi)
        val now : Date = sdf.parse(tanggal)
        val siangku : Date = sdf.parse(siang)
        val soreku : Date = sdf.parse(sore)
        val  malamku : Date = sdf.parse(malam)

        if(now.after(pagiku) && now.before(siangku)){
            txtJam.text="Selamat Pagi!"
        }else if(now.after(siangku) && now.before(soreku)){
            txtJam.text="Selamat Siang!"
        }else if(now.after(soreku) && now.before(malamku)){
            txtJam.text="Selamat Sore!"
        }else{
            txtJam.text="Selamat Malam!"
        }
    }

    private fun showTimePicker(v: View?){
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timePicker  = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener(function = {view, hourOfDay, minute ->

            jam = hourOfDay.toString()
            menit = minute.toString()
            session.save("jam",jam)
            session.save("menit",menit)
            txtJadwal.text=jam+"."+menit
        }),hour,minute,true)
        timePicker.show()
    }

    override fun onClick(v: View?) {
        if(v == btnJadwal) {
            showTimePicker(v)

        }
    }
}
