package com.pawelholownia.payback_zadanie.repository;

import com.pawelholownia.payback_zadanie.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
}