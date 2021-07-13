package com.sanjay.contactmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sanjay.contactmanager.adaptor.ContactAdaptor
import com.sanjay.contactmanager.model.ContactModel


class MainActivity : AppCompatActivity() {

    var cols = listOf<String>(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone._ID
    ).toTypedArray()

    val REQUEST = 111

    private var contactAdapter: ContactAdaptor? = null
    private lateinit var contactRecyclerView: RecyclerView
    private lateinit var contactList: ArrayList<ContactModel>
    private lateinit var contact: ContactModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkReadContactsPermission()
        inti()
    }

    private fun inti() {
        contactRecyclerView = findViewById(R.id.contacts_recycler_view)
        contactRecyclerView.layoutManager = LinearLayoutManager(this)
        contactRecyclerView.setHasFixedSize(true)
        contactRecyclerView.adapter = ContactAdaptor(contactList)
    }

    // function to check permission in run time
    private fun checkReadContactsPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                Array(1) { Manifest.permission.READ_CONTACTS },
                REQUEST
            )
        } else {
            readContact()
        }
    }

    // function to read contacts from ContactContract class
    private fun readContact() {
        try {
            contactList = arrayListOf<ContactModel>()

            var resultSet = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                cols,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )

            if (resultSet != null && resultSet.count > 0) {
                while (resultSet.moveToNext()) {
                    val name =
                        resultSet.getString(resultSet.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val phoneNumber =
                        resultSet.getString(resultSet.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val hasPhoneNumber =
                        resultSet.getInt(resultSet.getColumnIndex(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER))
                    Log.e("CONTACTS", "$name -> $phoneNumber")

                    contactList.add(ContactModel(name, phoneNumber))
                }
                contactAdapter?.notifyDataSetChanged()
                resultSet.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readContact()
        }
    }
}