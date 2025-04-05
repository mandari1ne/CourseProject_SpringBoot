package org.example.kursach.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vacation_reports")
public class VacationReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VacationStatus status;
}
