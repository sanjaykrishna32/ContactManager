package com.sanjay.contactmanager.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sanjay.contactmanager.R
import com.sanjay.contactmanager.model.ContactModel

class ContactAdaptor(private val contactList: ArrayList<ContactModel>) :
    RecyclerView.Adapter<ContactAdaptor.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.contact_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactList.get(position)
        holder.contactName.text = contact.contactName
        holder.phoneNumber.text = contact.phoneNumber
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName: TextView = itemView.findViewById(R.id.contact_name)
        val phoneNumber: TextView = itemView.findViewById(R.id.contact_number)
    }
}