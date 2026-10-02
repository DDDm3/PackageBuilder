package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.Tag;
import gascolae.group9.package_builder.catalog.enums.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {
    Optional<Tag> findByTagCode(String tagCode);
    boolean existsByTagCode(String tagCode);
    List<Tag> findByTagType(TagType tagType);
    List<Tag> findByParentTagTagId(String parentTagId);
    List<Tag> findByActiveTrue();
}
