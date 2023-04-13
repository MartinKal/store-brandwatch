package brandwatch.assessment.store.repository;

import brandwatch.assessment.store.model.Product;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    @Query(value = "{ 'productId' : ?0 }", fields = "{ '_id': 0, 'quantity': 1 }")
    Optional<Product> findStockByProductId(String productId);

    @Query(value = "{ 'needed' : {'$gt' : 0 }}")
    List<Product> findShortages();

    @Query(value = "{'productId' : ?0}", fields = "{'quantity': 1}")
    Optional<Integer> findQuantityByProductId(String productId);

    @Query(value = "{'productId' : ?0}")
    Optional<Product> findByProductId(String productId);

    @Query(value = "{'productId' : { $in: ?0 }}")
    Set<Product> findAllByProductId(Set<String> ids);
}
