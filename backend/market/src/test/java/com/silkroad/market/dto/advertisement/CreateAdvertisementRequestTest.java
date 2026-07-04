package com.silkroad.market.dto.advertisement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class CreateAdvertisementRequestTest {

    @Test
    void shouldBindImagesFromMultipartData() {
        CreateAdvertisementRequest request = new CreateAdvertisementRequest();
        MultipartFile image = new MockMultipartFile("images", "image.jpg", "image/jpeg", "test".getBytes());

        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(request);
        beanWrapper.setPropertyValue("images", List.of(image));

        assertNotNull(request.getImages());
        assertEquals(1, request.getImages().size());
        assertEquals(image, request.getImages().get(0));
    }
}
