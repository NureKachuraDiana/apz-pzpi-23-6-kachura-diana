package com.example.ecomonitormobile.network.Repositories

import com.example.ecomonitormobile.models.Sensor.Sensor
import com.example.ecomonitormobile.models.Sensor.SensorReading
import com.example.ecomonitormobile.network.ApiService.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class StationDetailData(
    val sensors: List<Sensor>,
    val latestReadings: List<SensorReading>
)

class StationDetailRepository(private val apiService: ApiService) {

    suspend fun getSensors(stationId: Int): Result<List<Sensor>> = apiCall {
        apiService.getSensorsByStation(stationId)
    }

    suspend fun getLatestReadings(stationId: Int, limit: Int = 50): Result<List<SensorReading>> = apiCall {
        apiService.getLatestReadings(stationId = stationId, limit = limit)
    }

    suspend fun getStationDetail(stationId: Int, readingsLimit: Int = 50): Result<StationDetailData> {
        return try {
            coroutineScope {
                val sensorsDeferred = async { getSensors(stationId) }
                val readingsDeferred = async { getLatestReadings(stationId, readingsLimit) }

                val sensorsResult = sensorsDeferred.await()
                val readingsResult = readingsDeferred.await()

                if (sensorsResult.isFailure) {
                    return@coroutineScope Result.failure(
                        sensorsResult.exceptionOrNull() ?: Exception("Failed to load sensors")
                    )
                }
                if (readingsResult.isFailure) {
                    return@coroutineScope Result.failure(
                        readingsResult.exceptionOrNull() ?: Exception("Failed to load readings")
                    )
                }

                Result.success(
                    StationDetailData(
                        sensors = sensorsResult.getOrThrow(),
                        latestReadings = readingsResult.getOrThrow()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun <T> apiCall(
        call: suspend () -> retrofit2.Response<T>
    ): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string()
                Result.failure(Exception("Помилка ${response.code()}: ${error ?: "невідома"}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
