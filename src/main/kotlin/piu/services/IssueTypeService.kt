package piu.services

import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.piu.models.Ticket
import piu.models.Institution
import piu.models.IssueType
import piu.repositories.IssueTypeRepository
import java.time.LocalDateTime

@ApplicationScoped
class IssueTypeService {

    @Inject
    lateinit var issueTypeRepository: IssueTypeRepository

    fun findById(id: Long): IssueType? = issueTypeRepository.findById(id)

    fun findAll(): List<IssueType>{
        return issueTypeRepository.listAll(Sort.ascending("createdAt"))
    }

    fun findAllActive(): List<IssueType> {
        return issueTypeRepository.list("active=true", Sort.ascending("createdAt"))
    }


    fun saveIssueType(issueType: IssueType){
        return issueTypeRepository.persist(issueType)
    }

    fun deleteIssueTypePermanently(id: Long){
        issueTypeRepository.deleteById(id)
    }

    fun deleteIssueType(id: Long){
        val issueType = issueTypeRepository.findById(id)
        issueType.active = false
        issueType.updatedAt = LocalDateTime.now()
        issueTypeRepository.persist(issueType)
    }

}