package piu.DTO

data class EmailRequestDTO(
    val to: String,
    val subject: String,
    val body: String
)