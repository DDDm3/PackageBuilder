package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.ServiceTag;
import gascolae.group9.package_builder.catalog.entity.ServiceTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ServiceTagRepository extends JpaRepository<ServiceTag, ServiceTagId> {
    List<ServiceTag> findByServiceServiceId(String serviceId);
    List<ServiceTag> findByTagTagId(String tagId);

    @Query("select st.tag.tagCode from ServiceTag st where st.service.serviceId = :serviceId")
    List<String> findTagCodesByServiceId(@Param("serviceId") String serviceId);

    @Query("select st.service.serviceId, st.tag.tagCode from ServiceTag st where st.service.serviceId in :serviceIds")
    List<Object[]> findTagCodesByServiceIds(@Param("serviceIds") Collection<String> serviceIds);
}
