package com.example.yourtime

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class ReportFragment : Fragment() {

    //pie chart view
    private lateinit var reportPieChart: PieChart
    private lateinit var text: TextView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = "Report"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        val model = TimeViewModel()
        val cal = Calendar.getInstance()
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        reportPieChart = view.findViewById(R.id.reportPieChart)
        button = view.findViewById(R.id.button)
        text = view.findViewById(R.id.input)
        setupPieChart()

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                reportPieChart.centerText = sdf.format(cal.time)
                loadPieChart(calculateTime(model.allEvents.value!!, cal))
            }

        button.setOnClickListener {
            context?.let { it1 ->
                DatePickerDialog(
                    it1, dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        return view
    }

    private fun calculateTime(events: List<Event>, selectedData: Calendar): FloatArray {
        var work = 0.0f
        var exercise = 0.0f
        var restaurant = 0.0f
        var other = 0.0f
        var sum = 0.0f

        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd H:m:s", Locale.ENGLISH)

        for (event in events) {
            cal.time = event.start?.let { sdf.parse(it) } as Date
            if (cal.get(Calendar.DAY_OF_YEAR) == selectedData.get(Calendar.DAY_OF_YEAR) &&
                cal.get(Calendar.YEAR) == selectedData.get(Calendar.YEAR)
            ) {
                when (event.title) {
                    "work" -> {
                        work += event.duration?.toFloat()!!
                    }
                    "exercise" -> {
                        exercise += event.duration?.toFloat()!!
                    }
                    "restaurant" -> {
                        restaurant += event.duration?.toFloat()!!
                    }
                    else -> {
                        other += event.duration?.toFloat()!!
                    }
                }
                sum += event.duration?.toFloat()!!
            }
        }
        if (sum == 0.0f) {
            val myFormat = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            reportPieChart.centerText = sdf.format(cal.time) + " No Data"
        }

        return floatArrayOf(work / sum, exercise / sum, restaurant / sum, other / sum)
    }

    private fun setupPieChart() {
        reportPieChart.isDrawHoleEnabled = true
        reportPieChart.setUsePercentValues(true)
        reportPieChart.setEntryLabelTextSize(12F)
        reportPieChart.setEntryLabelColor(Color.BLACK)
        reportPieChart.setCenterTextSize(24F)
        reportPieChart.description.isEnabled = false

    }

    private fun loadPieChart(arr: FloatArray) {
        val entries = ArrayList<PieEntry>()

        if (arr[0] != 0.0f) {
            entries.add(PieEntry(arr[0], "work"))
        }
        if (arr[1] != 0.0f) {
            entries.add(PieEntry(arr[1], "exercise"))
        }
        if (arr[2] != 0.0f) {
            entries.add(PieEntry(arr[2], "restaurant"))
        }
        if (arr[3] != 0.0f) {
            entries.add(PieEntry(arr[3], "other"))
        }

        val colors = ArrayList<Int>()
        for (i in ColorTemplate.MATERIAL_COLORS) {
            colors.add(i)
        }
        val dataSet = PieDataSet(entries, "time")
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setDrawValues(true)
        data.setValueFormatter(PercentFormatter(reportPieChart))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)

        reportPieChart.data = data
        reportPieChart.invalidate()

        reportPieChart.animateY(1400, Easing.EaseInOutQuad)
    }
}