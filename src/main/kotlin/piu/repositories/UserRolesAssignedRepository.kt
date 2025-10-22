package piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.piu.models.SystemUser
import piu.models.UserRolesAssigned

@ApplicationScoped
class UserRolesAssignedRepository: PanacheRepository<UserRolesAssigned> {

    fun findByUser(){

    }

    fun findRolesByUser(systemUser: SystemUser): List<UserRolesAssigned> {
        return find(
            "SELECT rol FROM UserRolesAssigned rol LEFT JOIN FETCH rol.systemUser where rol.systemUser=?1",systemUser
        ).list()
    }


    fun findRolesByUserId(usid: Long?): List<UserRolesAssigned> {
        return find(
            "SELECT rol FROM UserRolesAssigned rol LEFT JOIN FETCH rol.systemUser where rol.systemUser.id=?1",usid
        ).list()
    }


    fun deleteAllUserAssignments(usid: Long){
            delete("systemUser.id",usid)
    }





}