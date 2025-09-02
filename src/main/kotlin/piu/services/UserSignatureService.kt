package piu.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import piu.models.UserSignature
import piu.repositories.UserSignatureRepository

@ApplicationScoped
class UserSignatureService {

@Inject
lateinit var userSignatureRepository: UserSignatureRepository


    fun findById(id: Long): UserSignature?{
        return userSignatureRepository.findById(id)

    }
    fun findByUserId(id: Long): UserSignature?{
        return userSignatureRepository.findByUserId(id)

    }

    fun findCurrentByUserId(id: Long): UserSignature?{
        return userSignatureRepository.findCurrentByUserId(id)

    }


fun deleteUser(id: Long){
        userSignatureRepository.deleteById(id)
    }


    fun save(sig: UserSignature?) {
        userSignatureRepository.persist(sig)
    }

    fun hasCurrentSignature(id: Long): Boolean {
        return userSignatureRepository.hasCurrentSignature(id);
    }

    fun findCurrentSignature(id: Long): UserSignature{
        return userSignatureRepository.findCurrentSignature(id);
    }


    

}