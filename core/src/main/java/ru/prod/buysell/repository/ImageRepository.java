package ru.prod.buysell.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import ru.prod.buysell.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
