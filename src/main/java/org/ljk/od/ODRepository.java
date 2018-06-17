package org.ljk.od;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ODRepository extends JpaRepository<OpticalDevice, Long>, JpaSpecificationExecutor<OpticalDevice> {

}
