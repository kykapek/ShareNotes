package com.example.sharenotes

import android.content.Context
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest.newInstance
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenotes.CrimeListFragment.Companion.newInstance
import java.lang.reflect.Array.newInstance
import java.net.URLClassLoader.newInstance
import java.text.DateFormat
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())
    private var callbacks: Callbacks? = null

    private val crimeListViewModel : CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    override fun onAttach(context: Context) { //прикрепиили фрагмент к активити
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {  //отвязали фрагмент от активити
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu) //передал идентификатор ресурса файла меню
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {  //реакция на выбор команды меню
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()  //новый объект Crime
                crimeListViewModel.addCrime(crime)  //добавил его в БД
                callbacks?.onCrimeSelected(crime.id)  //сказать активити, что запрошено добавление нового преступления
                true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) //вызов onCreateOptionsMenu
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context) //LayoutManager располагает каждый элемент и следит за прокруткой
        //updateUI()
        crimeRecyclerView.adapter = adapter
        return  view
    }

    private fun updateUI(crimes: List<Crime>){
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimesListLiveData.observe(
                viewLifecycleOwner,
                Observer { crime ->
                    crime?.let {
                        Log.i(TAG, "Got crimes ${crime.size}")
                        updateUI(crime)
                    }
                })
    }



    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var crime: Crime

        private val solvedImageView: ImageView = itemView.findViewById(R.id.ivCrimeSolved)
        private val titleTextView: TextView = itemView.findViewById(R.id.tvCrimeTitle)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvCrimeDate)

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = DateFormat.getDateInstance(DateFormat.LONG).format(this.crime.date) //this.crime.date.toString()
            //dateTextView.text = crime.date.toString()
            Log.i("DateList", dateTextView.text.toString())
            solvedImageView.visibility = if(crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            callbacks?.onCrimeSelected(crime.id)

        }

    }

    private inner class CrimeAdapter(var crimes: List<Crime>) : RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {  //создание представления на дисплее
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

    }




    companion object {
        fun newInstance() : CrimeListFragment {
            return CrimeListFragment()
        }
    }

}