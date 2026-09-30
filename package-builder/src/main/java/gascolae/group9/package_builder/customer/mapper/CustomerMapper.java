package gascolae.group9.package_builder.customer.mapper;

import gascolae.group9.package_builder.customer.dto.request.CustomerCreateRequest;
import gascolae.group9.package_builder.customer.dto.request.CustomerUpdateRequest;
import gascolae.group9.package_builder.customer.dto.response.CustomerResponse;
import gascolae.group9.package_builder.customer.entity.Customer;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toCustomer(CustomerCreateRequest request);

    CustomerResponse toCustomerResponse(Customer customer);

    List<CustomerResponse> toCustomerResponseList(List<Customer> customers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCustomerFromRequest(CustomerUpdateRequest request, @MappingTarget Customer customer);
}
