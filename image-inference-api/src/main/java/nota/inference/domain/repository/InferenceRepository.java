package nota.inference.domain.repository;

import nota.inference.domain.model.Inference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InferenceRepository extends JpaRepository<Inference, Long> {
}
