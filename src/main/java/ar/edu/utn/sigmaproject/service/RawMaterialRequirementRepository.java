package ar.edu.utn.sigmaproject.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.utn.sigmaproject.domain.ProductionPlan;
import ar.edu.utn.sigmaproject.domain.RawMaterialRequirement;
import ar.edu.utn.sigmaproject.domain.Wood;

@Repository
public interface RawMaterialRequirementRepository extends JpaRepository<RawMaterialRequirement, Long> {
	
	RawMaterialRequirement findByProductionPlanAndWood(ProductionPlan productionPlan, Wood wood);

}
