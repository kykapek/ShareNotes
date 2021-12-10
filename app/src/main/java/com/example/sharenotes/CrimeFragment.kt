package com.example.sharenotes

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.io.File
import java.lang.String.format
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID //из аргументов забрал идентификатор
        crimeDetailViewModel.loadCrime(crimeId) //вынести
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.editTextCrimeTitle) as EditText
        dateButton = view.findViewById(R.id.btnCrimeDate) as Button
        solvedCheckBox = view.findViewById(R.id.checkBoxCrimeSolved) as CheckBox
        reportButton = view.findViewById(R.id.btnCrimeReport) as Button
        suspectButton = view.findViewById(R.id.btnCrimeSuspect) as Button
        photoButton = view.findViewById(R.id.imageButtonCrimeCamera) as ImageButton
        photoView = view.findViewById(R.id.ivCrimePhoto) as ImageView
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
                viewLifecycleOwner,
                Observer { crime ->
                    crime?.let {
                        this.crime = crime
                        photoFile = crimeDetailViewModel.getPhotoFile(crime)  //сохранение местонахождения фотки
                        photoUri = FileProvider.getUriForFile(requireActivity(),
                                "com.example.sharenotes.fileprovider",
                                photoFile) //преобразует локальный путь к файлу в Uri, который видит приложение камеры
                        updateUI()
                    }
                }
        )
    }




    private fun updateUI() { //правки пользователя
        titleField.setText(crime.title)  //трабл тут
        dateButton.text = crime.date.toString()
        Log.i("Date", dateButton.text.toString())
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if(crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {  //получение имени контакта
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data  //ну типа запрос
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME) //Указать, для каких полей запрос должен возвращать значекние
                val cursor = requireActivity().contentResolver.query(contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    if(it.count == 0) {
                        return
                    }
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object: TextWatcher{

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?, //ввод пользователя, вернет строку
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener{ _, isChecked->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {  //вызов конструктора фрагмента выбора даты
                setTargetFragment(this@CrimeFragment, REQUEST_DATE) //назначение целевого фрагмента
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {  //отправка репорта
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_suspect)
                )
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
            //pickContactIntent.addCategory(Intent.CATEGORY_HOME) проверка блока кнопки
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)  //найти активити с соответствующим интентом
            if(resolvedActivity == null) {
                suspectButton.isEnabled = false
            }
        }

        photoButton.apply {   //отправка интента камеры
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if(resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                        packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)

            }

        }


    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime) //сохранить отредактированное
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private fun getCrimeReport(): String {
        val solvedString = if(crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if(crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)  //создали и заполнили строку
    }

    companion object {

        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

}