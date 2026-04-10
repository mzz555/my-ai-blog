package com.blog.repository;

import com.blog.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByStatusOrderBySortOrder(Integer status);
}
