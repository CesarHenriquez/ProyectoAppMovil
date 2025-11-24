package com.example.appmovilfitquality.data.remote

import com.example.appmovilfitquality.data.dto.*
import retrofit2.http.*


private const val REG_URL = MicroserviceUrls.REGISTRO_USUARIO
private const val AUTH_URL = MicroserviceUrls.AUTENTICAR_USUARIO
private const val PROD_URL = MicroserviceUrls.PRODUCTOS_TIENDA
private const val VENTAS_URL = MicroserviceUrls.VENTAS
private const val DIRECCIONES_URL = MicroserviceUrls.DIRECCIONES



interface ApiService {


    @POST
    suspend fun register(@Url url: String = "${REG_URL}usuarios", @Body user: UserCredentialsDto): UserDto


    @POST
    suspend fun login(@Url url: String = "${AUTH_URL}auth/login", @Body credentials: UserCredentialsDto): LoginResponseDto






    @GET("${AUTH_URL}auth/users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): UserDto

    // ACTUALIZAR PERFIL (PUT /usuarios/{id}) - (8020)
    @PUT
    suspend fun updateProfile(@Url url: String = "${REG_URL}usuarios/{id}", @Path("id") id: Long, @Body userDto: UserDto): UserDto




    @PUT("${REG_URL}usuarios/clave/{email}")
    suspend fun updatePassword(@Path("email") email: String, @Body request: Map<String, String>): Unit // request es {"nuevaClave": "..."}


    @GET
    suspend fun getProducts(@Url url: String = "${PROD_URL}api/productos"): List<ProductDto>

    @POST
    suspend fun addProduct(@Url url: String = "${PROD_URL}api/productos", @Body product: ProductDto): ProductDto

    @PUT
    suspend fun updateProduct(@Url url: String = "${PROD_URL}api/productos/{id}", @Path("id") id: Int, @Body product: ProductDto): ProductDto

    @DELETE
    suspend fun deleteProduct(@Url url: String = "${PROD_URL}api/productos/{id}", @Path("id") id: Int): Unit




    @POST
    suspend fun createOrder(@Url url: String = "${VENTAS_URL}api/ventas", @Body request: CreateOrderRequestDto): OrderDto


    @GET
    suspend fun getOrdersByCustomer(@Url url: String = "${VENTAS_URL}api/ventas/usuario/{id}", @Path("id") id: Long): List<OrderDto>



    @GET
    suspend fun getAllOrders(@Url url: String = "${VENTAS_URL}api/ventas"): List<OrderDto>

    @PUT("${VENTAS_URL}api/ventas/{id}/proof")
    suspend fun setDeliveryProof(@Path("id") orderId: Long, @Body request: Map<String, String>): OrderDto // request es {"proofUri": "..."}




    @GET
    suspend fun getShippingAddressByUserId(@Url url: String = "${DIRECCIONES_URL}api/direcciones/usuario/{usuarioId}", @Path("usuarioId") userId: Long): List<DireccionDto>

}