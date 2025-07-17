package piu.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import piu.models.Institution

@ApplicationScoped
class InstitutionRepository  : PanacheRepository<Institution>{


}