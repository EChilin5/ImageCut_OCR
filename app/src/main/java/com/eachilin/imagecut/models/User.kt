package com.eachilin.imagecut.models

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class User(
    @get:Exclude var id: String = "",
    var username: String =""
) :Serializable