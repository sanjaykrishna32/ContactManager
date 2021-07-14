package com.sanjay.contactmanager

import android.Manifest
import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sanjay.contactmanager.adaptor.ContactAdaptor
import com.sanjay.contactmanager.model.ContactModel
import kotlinx.android.synthetic.main.add_contact_layout_file.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private var cols = listOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone._ID
    ).toTypedArray()

    private val REQUEST = 111

    private lateinit var contactRecyclerView: RecyclerView
    private lateinit var contactList: ArrayList<ContactModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inti()
        checkReadContactsPermission()
    }

    private fun inti() {
        contactRecyclerView = findViewById(R.id.contacts_recycler_view)
        contactRecyclerView.layoutManager = LinearLayoutManager(this)
        contactRecyclerView.setHasFixedSize(true)
    }

    // function to check permission in run time
    private fun checkReadContactsPermission() {
        val PERMISSIONS =
            arrayOf<String>(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
        if (ActivityCompat.checkSelfPermission(this, PERMISSIONS.toString())
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                REQUEST
            )
        } else {
            readContact()
        }
    }

    // function to read contacts from ContactContract class
    private fun readContact() {
        try {
            contactList = arrayListOf()

            val resultSet = contentResolver.query(
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

                    Log.e("CONTACTS", "$name -> $phoneNumber")

                    contactList.add(ContactModel(name, phoneNumber))
                }
                contactRecyclerView.adapter = ContactAdaptor(contactList)
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

        if (requestCode == REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            readContact()
        }
    }

    // onclick function for floating action button
    fun addContactButton(view: View) {
        showAddContactDialog()
    }

    //Function to insert contact
    private fun insertContactCode(displayName: String, mobileNumber: String) {
        val ops = ArrayList<ContentProviderOperation>()

        ops.add(
            ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        //Name
        if (displayName != null) {
            ops.add(
                ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI
                )
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        displayName
                    ).build()
            )
        }
        //Mobile Number
        if (mobileNumber != null) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
                    .build()
            )
        }
        // Asking the Contact provider to create a new contact
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            readContact()
            Toast.makeText(this, "Contact Added Successfully", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Exception: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    // function to show custom dialog
    private fun showAddContactDialog() {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.add_contact_layout_file, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Add Contact")
        //show dialog
        val mAlertDialog = mBuilder.show()
        //add contact button click
        mDialogView.addContactDialogButton.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
            //get text from EditText
            val name = mDialogView.add_contact_name.text.toString()
            val phoneNumber = mDialogView.add_contact_phone_number.text.toString()

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show()
                showAddContactDialog()
            } else if (isValidName(name)) {
                Toast.makeText(
                    this,
                    "Contact name should not contain special characters",
                    Toast.LENGTH_SHORT
                ).show()
                showAddContactDialog()
            } else if (!isValidPhone(phoneNumber) || phoneNumber.length < 10) {
                Toast.makeText(
                    this,
                    "Please enter valid phone number",
                    Toast.LENGTH_SHORT
                ).show()
                showAddContactDialog()
            } else {
                // insert contact
                insertContactCode(name, phoneNumber)
                mAlertDialog.dismiss()
            }
        }
        //cancel button click of custom layout
        mDialogView.cancelContactDialogButton.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }
    }

    //function to validate contact name
    private fun isValidName(name: String): Boolean {
        val p: Pattern = Pattern.compile("[^_a-z0-9 ]", Pattern.CASE_INSENSITIVE)
        val m: Matcher = p.matcher(name)
        val b: Boolean = m.find()

        return b
    }

    //function to validate contact phone number
    private fun isValidPhone(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }
}