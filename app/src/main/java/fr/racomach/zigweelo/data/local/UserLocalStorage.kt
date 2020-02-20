package fr.racomach.zigweelo.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import fr.racomach.zigweelo.model.User
import fr.racomach.zigweelo.utils.Try
import fr.racomach.zigweelo.utils.getOrNull
import fr.racomach.zigweelo.utils.map
import fr.racomach.zigweelo.utils.runTry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserLocalStorage(
    val sharedPreferences: SharedPreferences,
    val moshi: Moshi
) {

    suspend fun save(user: User) = withContext(Dispatchers.IO) {
        runTry { moshi.adapter(User::class.java).toJson(user) }
            .map { sharedPreferences.edit { putString("current_user", it) } }
            .map { user }
    }

    suspend fun load() = withContext(Dispatchers.IO) {
        sharedPreferences.getString("current_user", "")
            .runTry { moshi.adapter(User::class.java).fromJson(this!!)!! }
    }
}
