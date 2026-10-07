package com.accenture.franchises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void apiDocIsDescribedInSpanish() throws Exception {
        String body = apiDocs();

        assertThat(body)
                .contains("API de Franquicias")
                .contains("Gestión de franquicias, sucursales, productos e inventario de stock.")
                .contains("Alta, consulta, renombrado y baja de franquicias.")
                .contains("Crear franquicia")
                .contains("Listar franquicias")
                .contains("Obtener una franquicia")
                .contains("Renombrar franquicia")
                .contains("Eliminar franquicia")
                .contains("Página a consultar, empezando en 0.")
                .contains("Elementos por página, con un máximo de 100.")
                .contains("Identificador UUID de la franquicia.");
    }

    @Test
    void paginationIsDocumentedWithPageAndLimit() throws Exception {
        String body = apiDocs();

        assertThat(body).contains("\"name\":\"page\"").contains("\"name\":\"limit\"");
    }

    @Test
    void versionIsNoLongerPartOfTheModel() throws Exception {
        assertThat(apiDocs()).doesNotContain("\"name\":\"version\"");
    }

    private String apiDocs() throws Exception {
        return mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }
}
