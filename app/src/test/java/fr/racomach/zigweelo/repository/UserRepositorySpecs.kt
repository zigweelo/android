package fr.racomach.zigweelo.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import fr.racomach.zigweelo.data.local.UserLocalStorage
import fr.racomach.zigweelo.data.network.UserResponse
import fr.racomach.zigweelo.data.network.ZigweeloApi
import fr.racomach.zigweelo.model.User
import fr.racomach.zigweelo.utils.Try
import fr.racomach.zigweelo.extension.isSuccess
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import retrofit2.Response

object UserRepositorySpecs : Spek({

    Feature("No existing user") {

        val userLocalStorageMock: UserLocalStorage = mockk()

        beforeEachTest {
            try {
                val user = slot<User>()
                coEvery { userLocalStorageMock.load() } returns Try.Failure(Exception("Test"))
                coEvery { userLocalStorageMock.save(capture(user)) } answers { Try.Success(user.captured) }
            } catch (exception: Exception) {
                println(exception)
                System.err.print(exception)
            }
        }

        Scenario("Success: create a new user from the remote API") {

            val zigweeloApi: ZigweeloApi = mockk()
            val expectedAuthToken = "expected_output_token"

            Given("A successful API response") {
                coEvery { zigweeloApi.createAnonymousUser(any()) } returns Response.success(
                    UserResponse(
                        expectedAuthToken
                    )
                )
            }

            lateinit var user: Try<User>
            When("getting the current user") {
                runBlocking {
                    val userRepository =
                        UserRepositoryImpl(
                            userLocalStorage = userLocalStorageMock,
                            zigweeloApi = zigweeloApi
                        )
                    user = userRepository.current()
                }
            }

            Then("it should received a valid user with authentication token returns by the API") {
                assertThat(user).isSuccess().isEqualTo(User(null, expectedAuthToken))
            }
        }
    }
})