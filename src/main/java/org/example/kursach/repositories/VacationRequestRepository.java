package org.example.kursach.repositories;

import org.example.kursach.model.VacationRequest;
import org.example.kursach.model.User;
import org.example.kursach.model.VacationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {
    List<VacationRequest> findByEmployee(User employee);
    @Query("SELECT v FROM VacationRequest v WHERE v.employee.userInfo.department = :department")
    List<VacationRequest> findAllByEmployeeDepartment(@Param("department") String department);
    List<VacationRequest> findByEmployeeIdAndStatus(Long employeeId, VacationStatus status);

}
