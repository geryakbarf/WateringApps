package com.id.wtf.wateringapps.models

import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import com.id.wtf.wateringapps.R
import com.id.wtf.wateringapps.utils.Session
import kotlinx.android.synthetic.main.activity_menu.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MenuActivity : AppCompatActivity(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {


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
            } else {
                session.notif("siram", false)
                btnJadwal.isEnabled = false
            }
        })
        parseData()
        swipeView.setOnRefreshListener {
            mRunnable = Runnable {
                pDialog.show()
                parseData()
                time()
                swipeView.isRefreshing = false
            }
            mHandler.postDelayed(
                mRunnable,
                (randomInRange(1, 2) * 1000).toLong()
            )
        }

        btnSiram.setOnClickListener(this)
        val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("watering-apps").child("status")
        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                var value: String = p0.getValue(String::class.java)!!
                if (value.equals("Off")) {
                    Toast.makeText(applicationContext, "Tanah Basah, penyiraman selesai", Toast.LENGTH_SHORT).show()
                }else
                    Toast.makeText(applicationContext,"Penyiraman Dimulai...",Toast.LENGTH_SHORT).show()

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
                    txtTanggal.text = obj.getString("date")
                    val pressure = obj.getInt("pressure")
                    if (pressure < 40) {
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

    override fun onRefresh() {


    }

    private fun nyiram() {
        val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("watering-apps").child("status")
        database.setValue("On")
    }

    private fun randomInRange(min: Int, max: Int): Int {
        // Define a new Random class
        val r = Random()

        // Get the next random number within range
        // Including both minimum and maximum number
        return r.nextInt((max - min) + 1) + min
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
                session.save("jam", jam)
                session.save("menit", menit)
                txtJadwal.text = jam + "." + menit
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
