package brandwatch.assessment.store.repository;

import brandwatch.assessment.store.model.StockShortage;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface StockShortageRepository extends MongoRepository<StockShortage, ObjectId> {
    //Optional<StockShortage> findByProductId(String productId);

    @Query(value = "{'productId' : ?0}", fields = "{'quantity': 1}")
    Optional<Integer> findQuantityByProductId(String productId);
}
