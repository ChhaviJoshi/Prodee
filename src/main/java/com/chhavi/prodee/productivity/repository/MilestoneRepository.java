package com.chhavi.prodee.productivity.repository;

import com.chhavi.prodee.productivity.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByUserIdOrderByTargetDateAsc(Long userId);
}
