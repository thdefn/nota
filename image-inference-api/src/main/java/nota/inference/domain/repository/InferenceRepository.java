package nota.inference.domain.repository;

import nota.inference.domain.model.Inference;
import nota.inference.domain.model.Runtime;
import nota.inference.dto.response.InferenceHistoryItem;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
public interface InferenceRepository extends JpaRepository<Inference, Long>, JpaSpecificationExecutor<Inference> {

}
