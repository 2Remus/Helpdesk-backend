package piu.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import piu.models.Institution
import piu.repositories.InstitutionRepository

@ApplicationScoped
class InstitutionService {
    @Inject
    lateinit var institutionRepository: InstitutionRepository

    fun findAll(): List<Institution> = institutionRepository.listAll()

    fun findById(id: Long): Institution? = institutionRepository.findById(id)

    @Transactional
    fun create(institution: Institution): Institution {
        institutionRepository.persist(institution)
        return institution
    }

    @Transactional
    fun update(id: Long, updated: Institution): Institution? {
        val existing = institutionRepository.findById(id) ?: return null
        existing.name = updated.name
        existing.address = updated.address
        existing.email = updated.email
        existing.phoneNumber = updated.phoneNumber
        return existing
    }

    @Transactional
    fun delete(id: Long): Boolean {
        return institutionRepository.deleteById(id)
    }
}