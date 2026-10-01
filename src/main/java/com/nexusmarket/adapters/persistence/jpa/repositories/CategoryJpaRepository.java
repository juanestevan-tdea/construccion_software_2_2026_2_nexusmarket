package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    Optional<CategoryJpaEntity> findByName(String name);

    boolean existsByName(String name);

    List<CategoryJpaEntity> findByParentIsNull();

    List<CategoryJpaEntity> findByParent(CategoryJpaEntity parent);

    @Query("SELECT c FROM CategoryJpaEntity c WHERE c.parent.id = :parentId")
    List<CategoryJpaEntity> findByParentId(@Param("parentId") Long parentId);
}
