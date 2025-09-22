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
/*
    @Transactional

    fun updateInstitution(id: Long?, institution: Institution){
        institutionRepository.update(
            "name = '${institution.name}', "+
                    "email = '${institution.email}', "+
                    "address = '${institution.address}' "+
                    "phoneNumber = '${institution.phoneNumber}' "+
                    "where id = $id"
        )

    }*/


    fun findByName(name: String?): Institution? {
        return institutionRepository.findByName(name)
    }


    @Transactional
    fun updateInstitution(institution: Institution) {
        institutionRepository.persist(institution)
    }

    @Transactional
    fun delete(id: Long): Boolean {
        return institutionRepository.deleteById(id)
    }
}