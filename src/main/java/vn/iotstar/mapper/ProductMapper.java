package vn.iotstar.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;

/**
 * MapStruct mapper for Product and ProductDTO.
 * Uses abstract class pattern to ensure full compatibility with Spring Framework 7.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class ProductMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "image", ignore = true)
    public abstract ProductDTO toDTO(Product entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract Product toEntity(ProductDTO dto);

    public ProductDTO toDto(Product entity) {
        return toDTO(entity);
    }
}
