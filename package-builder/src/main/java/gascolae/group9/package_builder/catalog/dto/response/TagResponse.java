package gascolae.group9.package_builder.catalog.dto.response;

import gascolae.group9.package_builder.catalog.enums.TagType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagResponse {
    String tagId;
    String tagCode;
    String tagName;
    TagType tagType;
    String parentTagId;
    String parentTagCode;
    String description;
    String sourceRef;
    Boolean active;
}
