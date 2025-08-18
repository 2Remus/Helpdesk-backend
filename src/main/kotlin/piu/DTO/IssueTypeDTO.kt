package piu.DTO

import piu.models.IssueType

data class IssueTypeDTO(

    val id: Long?,
    val name: String?,
    val description: String?,
    )

fun IssueType.toDTO(): IssueTypeDTO = IssueTypeDTO(
    id = this.id,
    name = this.name,
    description = this.description,
)

