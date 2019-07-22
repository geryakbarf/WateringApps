package com.id.wtf.wateringapps.models

import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import com.id.wtf.wateringapps.R
import com.id.wtf.wateringapps.api.JadwalRequest
import com.id.wtf.wateringapps.data.Keterangan
import com.id.wtf.wateringapps.utils.Session
import kotlinx.android.synthetic.main.activity_menu.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MenuActivity : AppCompatActivity(), View.OnClickListener {


    lateinit var session: Session
    lateinit var jam: String
    lateinit var menit: String
    val URL = "http://iot.alwiyahyamuljabar.xyz/Android/getData.php"
    lateinit var pDialog: ProgressDialog
    lateinit var mRunnable: Runnable
    lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_menu)
        session = Session(applicationContext)

        //
        pDialog = ProgressDialog(this)
        pDialog.setMessage("Memuat...")

        time()
        mHandler = Handler()
        //

        //
        if (session.getValueString("jam") != null) {
            jam = session.getValueString("jam")!!
            menit = session.getValueString("menit")!!
            txtJadwal.text = jam + "." + menit
        }
        //

        //
        if (session.getValueBoolean("siram") != null && session.getValueBoolean("siram") == true) {
            switchSiram.isChecked = true
            btnJadwal.isEnabled = true
        } else if (session.getValueBoolean("siram") == false) {
            switchSiram.isChecked = false
            btnJadwal.isEnabled = false
        }
        //
        switchSiram.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                session.notif("siram", true)
                btnJadwal.isEnabled = true
                btnJadwal.setOnClickListener(this)
                if (session.getValueString("jam") != null) {
                    jam = session.getValueString("jam")!!
                    menit = session.getValueString("menit")!!
                } else {
                    jam = "08"
                    menit = "00"
                }
                var jadwal = jam + ":" + menit + ":00"
                if (jadwal.length < 8) {
                    jadwal = "0" + jam + ":" + menit + ":00"
                }
                val listener = Response.Listener<String> { response ->
                    try {
                        val obj = JSONObject(response)
                        val success = obj.getBoolean("success")
                        if (success) {
                            session.save("jam", jam)
                            session.save("menit", menit)
                            txtJadwal.text = jam + ":" + menit
                            Toast.makeText(applicationContext, obj.getString("data"), Toast.LENGTH_SHORT).show()
                        }

                    } catch (a: JSONException) {

                    }
                }
                val jadwalRequest = JadwalRequest(jadwal, listener)
                val requestQueue = Volley.newRequestQueue(applicationContext)
                requestQueue.add(jadwalRequest)


                //
            } else {
                val stringRequest = StringRequest(
                    Request.Method.GET,
                    "http://iot.alwiyahyamuljabar.xyz/Android/jadwalOff.php",
                    Response.Listener { response ->
                        try {
                            val obj = JSONObject(response)
                            val success: Boolean = obj.getBoolean("success")
                            if (success) {
                                Toast.makeText(
                                    applicationContext,
                                    "Jadwal Penyiraman Otomatis Dimatikan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (a: JSONException) {

                        }
                    },
                    Response.ErrorListener {

                    })
                val requestQueue = Volley.newRequestQueue(applicationContext)
                requestQueue.add(stringRequest)
                session.notif("siram", false)
                btnJadwal.isEnabled = false
            }
        })
        parseData()
        //
        btnSiram.setOnClickListener(this)
        val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("watering-apps").child("status")
        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                var value: String = p0.getValue(String::class.java)!!
                if (value.equals("Off")) {
                    Toast.makeText(applicationContext, "Penyiraman Selesai", Toast.LENGTH_SHORT).show()
                }

            }
        })
        //

        val dbAlat: DatabaseReference = FirebaseDatabase.getInstance().getReference("watering-apps").child("keterangan")
        dbAlat.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                var keterangan: Keterangan = p0.getValue(Keterangan::class.java)!!
                txtWind.text = keterangan.kelembapan.substring(0, 2) + "%"
                txtSuhu.text = keterangan.suhu.substring(0, 2)
                val pressure: Double = keterangan.soil.toDouble()
                if (pressure > 40.0) {
                    txtHumidity.text = "Tanah Basah"
                } else if (pressure < 40.0) {
                    txtHumidity.text = "Tanah Kering"
                }
            }
        })
    }

    private fun parseData() {
        pDialog.show()
        val stringRequest = StringRequest(Request.Method.GET, URL,
            Response.Listener { response ->
                try {
                    val obj = JSONObject(response)
                    txtSuhu.text = obj.getString("temperature")
                    txtWind.text = obj.getString("humidity") + "%"
                    val pressure = obj.getDouble("pressure")
                    if (pressure < 40.0) {
                        txtHumidity.text = "Tanah Kering"
                    } else {
                        txtHumidity.text = "Tanah Basah"
                    }
                    pDialog.dismiss()
                } catch (a: JSONException) {
                    a.printStackTrace()
                }
            }, Response.ErrorListener {

            })
        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(stringRequest)
    }


    private fun nyiram() {
        val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("watering-apps").child("status")
        database.setValue("On")

        val stringRequest = StringRequest(
            Request.Method.GET,
            "http://iot.alwiyahyamuljabar.xyz/Android/On.php",
            Response.Listener { response ->
                try {
                    val obj = JSONObject(response)
                    val success: Boolean = obj.getBoolean("success")
                    if (success) {
                        Toast.makeText(applicationContext, "Alat Berhasil Dinyalakan!", Toast.LENGTH_SHORT).show()
                    }
                } catch (a: JSONException) {

                }
            },
            Response.ErrorListener {

            })
        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(stringRequest)
    }


    private fun time() {
        val sdf = SimpleDateFormat("HH:mm")
        val tanggal: String = sdf.format(Date())
        val pagi = "05:00"
        val siang = "11:00"
        val sore = "15:00"
        val malam = "18:00"
        val pagiku: Date = sdf.parse(pagi)
        val now: Date = sdf.parse(tanggal)
        val siangku: Date = sdf.parse(siang)
        val soreku: Date = sdf.parse(sore)
        val malamku: Date = sdf.parse(malam)

        if (now.after(pagiku) && now.before(siangku)) {
            txtJam.text = "Selamat Pagi!"
        } else if (now.after(siangku) && now.before(soreku)) {
            txtJam.text = "Selamat Siang!"
        } else if (now.after(soreku) && now.before(malamku)) {
            txtJam.text = "Selamat Sore!"
        } else {
            txtJam.text = "Selamat Malam!"
        }
    }

    private fun showTimePicker(v: View?) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timePicker =
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener(function = { view, hourOfDay, minute ->

                jam = hourOfDay.toString()
                menit = minute.toString()
                if (menit.length < 2) {
                    menit = "0" + minute.toString()
                }
                var jadwal = jam + ":" + menit + ":00"
                if (jadwal.length < 8) {
                    jadwal = "0" + jam + ":" + menit + ":00"
                }
                //Upload ke Database
                val listener = Response.Listener<String> { response ->
                    try {
                        val obj = JSONObject(response)
                        val success = obj.getBoolean("success")
                        if (success) {
                            session.save("jam", jam)
                            session.save("menit", menit)
                            txtJadwal.text = jam + ":" + menit
                            Toast.makeText(applicationContext, obj.getString("data"), Toast.LENGTH_SHORT).show()
                        }

                    } catch (a: JSONException) {

                    }
                }
                val jadwalRequest = JadwalRequest(jadwal, listener)
                val requestQueue = Volley.newRequestQueue(applicationContext)
                requestQueue.add(jadwalRequest)


            }), hour, minute, true)
        timePicker.show()
    }

    override fun onClick(v: View?) {
        if (v == btnJadwal) {
            showTimePicker(v)

        }

        if (v == btnSiram) {
            nyiram()
        }
    }
}
