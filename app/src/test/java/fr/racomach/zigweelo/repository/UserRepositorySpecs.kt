package fr.racomach.zigweelo.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import fr.racomach.zigweelo.data.local.UserLocalStorage
import fr.racomach.zigweelo.data.network.UserRequest
import fr.racomach.zigweelo.data.network.UserResponse
import fr.racomach.zigweelo.data.network.ZigweeloApi
import fr.racomach.zigweelo.extension.isFail
import fr.racomach.zigweelo.extension.isSuccess
import fr.racomach.zigweelo.model.User
import fr.racomach.zigweelo.utils.Try
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.gherkin.Feature
import retrofit2.Response

object UserRepositorySpecs : Spek({

    Feature("No existing user") {

        val userLocalStorageMock by memoized(mode = CachingMode.EACH_GROUP) {
            val user = slot<User>()
            mockk<UserLocalStorage> {
                coEvery { load() } returns Try.Failure(Exception("Test"))
                coEvery { save(capture(user)) } answers { Try.Success(user.captured) }
            }
        }

        Scenario("Success: create a new user from the remote API") {

            val zigweeloApiMock = mockk<ZigweeloApi>()
            val expectedAuthToken = "expected_output_token"

            Given("A successful API response") {
                coEvery { zigweeloApiMock.createAnonymousUser(UserRequest()) } returns Response.success(
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
                            zigweeloApi = zigweeloApiMock
                        )
                    user = userRepository.current()
                }
            }

            Then("it should received a valid user with authentication token returns by the API") {
                assertThat(user).isSuccess().isEqualTo(User(null, expectedAuthToken))
            }

            And("it should save the user in the local storage") {
                coVerify(exactly = 1) { userLocalStorageMock.save(User(null, expectedAuthToken)) }
            }
        }

        Scenario("Failure: remote API returns an error") {

            val zigweeloApiMock = mockk<ZigweeloApi>()

            Given("A failed API response") {
                coEvery { zigweeloApiMock.createAnonymousUser(any()) } returns Response.error(
                    404,
                    ResponseBody.create(null, "Error")
                )
            }

            lateinit var user: Try<User>
            When("getting the current user") {
                runBlocking {
                    val userRepository =
                        UserRepositoryImpl(
                            userLocalStorage = userLocalStorageMock,
                            zigweeloApi = zigweeloApiMock
                        )
                    user = userRepository.current()
                }
            }

            Then("it should received an invalid user with error") {
                assertThat(user).isFail()
            }

            And("it should not save the user in the local storage") {
                coVerify(exactly = 0) { userLocalStorageMock.save(any()) }
            }
        }
    }

    Feature("Existing user") {

        val user = User("name", "expected_token")

        val userLocalStorageMock by memoized(mode = CachingMode.EACH_GROUP) {
            mockk<UserLocalStorage> {
                coEvery { load() } returns Try.Success(user)
            }
        }

        Scenario("Success: load the user from the local storage") {

            val zigweeloApiMock = mockk<ZigweeloApi>()

            lateinit var userResponse: Try<User>
            When("getting the current user") {
                runBlocking {
                    val userRepository =
                        UserRepositoryImpl(
                            userLocalStorage = userLocalStorageMock,
                            zigweeloApi = zigweeloApiMock
                        )
                    userResponse = userRepository.current()
                }
            }

            Then("it should return a valid user") {
                assertThat(userResponse).isSuccess().isEqualTo(user)
            }

            And("it should call the register API") {
                coVerify(exactly = 0) { zigweeloApiMock.createAnonymousUser(any()) }
            }

            And("it should not save the user in the local storage") {
                coVerify(exactly = 0) { userLocalStorageMock.save(any()) }
            }
        }
    }
})
