package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer globalErrorResponses() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components.getSchemas() == null || !components.getSchemas().containsKey("ErrorResponse")) {
                Schema<?> errorSchema = new Schema<>()
                        .addProperties("timestamp", new StringSchema().format("date-time").description("Время ошибки"))
                        .addProperties("status", new IntegerSchema().description("HTTP статус"))
                        .addProperties("error", new StringSchema().description("Краткое имя ошибки"))
                        .addProperties("message", new StringSchema().description("Описание ошибки"));
                components.addSchemas("ErrorResponse", errorSchema);
            }

            ApiResponse resp400 = api(400, "Bad Request — некорректные данные запроса");
            ApiResponse resp401 = api(401, "Unauthorized — неверные учётные данные или недействительный/отсутствующий Bearer токен");
            ApiResponse resp403 = api(403, "Forbidden — недостаточно прав");
            ApiResponse resp404 = api(404, "Not Found — ресурс не найден");
            ApiResponse resp409 = api(409, "Conflict — конфликт целостности/уникальности");

            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(op -> {
                ApiResponses rs = op.getResponses();
                rs.addApiResponse("400", resp400);
                rs.addApiResponse("401", resp401);
                rs.addApiResponse("403", resp403);
                rs.addApiResponse("404", resp404);
                rs.addApiResponse("409", resp409);
            }));
        };
    }

    private static ApiResponse api(int status, String description) {
        Schema<?> ref = new Schema<>().$ref("#/components/schemas/ErrorResponse");
        MediaType mt = new MediaType().schema(ref)
                .example(java.util.Map.of(
                        "timestamp", "2025-11-02T12:00:00",
                        "status", status,
                        "error", description.split(" — ")[0],
                        "message", "Описание причины ошибки"
                ));
        Content content = new Content().addMediaType("application/json", mt);
        return new ApiResponse().description(description).content(content);
    }
}


