package piu.models

import piu.DTO.IssueTypeDTO

data class InstitutionDTO(
    val id: Long?,
    val name: String?,
    val address: String?,
    val email: String?,
    val phoneNumber: String?
)

fun Institution.toDTO(): InstitutionDTO {
    return InstitutionDTO(
        id = this.id ,
        name = this.name,
        address = this.address,
        email = this.email,
        phoneNumber = this.phoneNumber,

        )
}
