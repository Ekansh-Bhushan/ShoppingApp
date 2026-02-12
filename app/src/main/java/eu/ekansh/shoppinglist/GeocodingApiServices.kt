package eu.ekansh.shoppinglist

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiServices {

    @GET("maps/api/geocode/json")
    suspend fun getAddressFromCoordinates(
        @Query("latlng") latlng : String,
        @Query("Key") apiKey : String
    ) : GeocodingResponse
}