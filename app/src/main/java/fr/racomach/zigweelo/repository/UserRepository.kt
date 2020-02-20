package fr.racomach.zigweelo.repository

import androidx.annotation.VisibleForTesting
import fr.racomach.zigweelo.data.local.UserLocalStorage
import fr.racomach.zigweelo.data.network.UserRequest
import fr.racomach.zigweelo.data.network.ZigweeloApi
import fr.racomach.zigweelo.extension.toTry
import fr.racomach.zigweelo.model.User
import fr.racomach.zigweelo.utils.Try
import fr.racomach.zigweelo.utils.flatMap
import fr.racomach.zigweelo.utils.fold
import fr.racomach.zigweelo.utils.map

interface UserRepository {

    suspend fun current(): Try<User>

}

class UserRepositoryImpl(
    private val userLocalStorage: UserLocalStorage,
    private val zigweeloApi: ZigweeloApi
) : UserRepository {

    override suspend fun current() =
        loadOrCreate { register() }

    @VisibleForTesting
    suspend fun loadOrCreate(
        createNewUser: suspend () -> Try<User>
    ) =
        userLocalStorage.load()
            .fold(
                ifFailure = {
                    createNewUser()
                        .flatMap { userLocalStorage.save(User(authenticationToken = it.authenticationToken)) }
                },
                ifSuccess = { Try.Success(it) }
            )

    @VisibleForTesting
    suspend fun register() =
        zigweeloApi.createAnonymousUser(user = UserRequest()).toTry()
            .map { User(authenticationToken = it.authenticationToken) }
}
