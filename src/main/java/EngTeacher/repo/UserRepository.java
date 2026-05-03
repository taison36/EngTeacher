package EngTeacher.repo;

import EngTeacher.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    java.util.Optional<User> findByName(String name);
}
