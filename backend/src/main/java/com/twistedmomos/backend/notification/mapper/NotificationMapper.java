package com.twistedmomos.backend.notification.mapper;

import com.twistedmomos.backend.notification.dto.response.NotificationResponse;
import com.twistedmomos.backend.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "type", expression = "java(notification.getType().name())")
    @Mapping(target = "read", expression = "java(notification.getReadAt() != null)")
    NotificationResponse toResponse(Notification notification);
}
