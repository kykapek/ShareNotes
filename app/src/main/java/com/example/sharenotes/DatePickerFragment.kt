package com.example.sharenotes

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*


private const val ARG_DATE = "date"

class DatePickerFragment : DialogFragment() {

    interface Callbacks {
        fun onDateSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dateListener = DatePickerDialog.OnDateSetListener {  //возврат даты
            _: DatePicker, year: Int, month: Int, day: Int ->  // _ это параметр который не используется

            val resultDate : Date = GregorianCalendar(year, month, day).time  //выбранная дата. .time нужен для получения об-та Date

            targetFragment?.let { fragment ->   //тут хранится экземпляр фрагмента, который запустил DatePickerFragment
                (fragment as Callbacks).onDateSelected(resultDate)  //экземпляр фр-та передается в интерфейс Callbacks
            }
        }
        val date = arguments?.getSerializable(ARG_DATE) as Date //забрал из аргументов
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
                requireContext(),
                dateListener,
                initialYear,
                initialMonth,
                initialDay
        )
    }

    /*
    private fun timePicker() {
        val calendar = Calendar.getInstance()
        var initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        var initialMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this,
                OnTimeSetListener { view, hourOfDay, minute ->
                    initialHour = hourOfDay
                    initialMinute = minute
                    //et_show_date_time.setText(date_time.toString() + " " + hourOfDay + ":" + minute)
                }, initialHour, initialMinute, false)
        timePickerDialog.show()

    }

    private fun TimePickerDialog(datePickerFragment: DatePickerFragment, onTimeSetListener: TimePickerDialog.OnTimeSetListener, initialHour: Int, initialMinute: Int, b: Boolean): TimePickerDialog {

    }

     */

    companion object {
        fun newInstance(date: Date): DatePickerFragment {  //аргументы, конструктор фрагмента
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }

}