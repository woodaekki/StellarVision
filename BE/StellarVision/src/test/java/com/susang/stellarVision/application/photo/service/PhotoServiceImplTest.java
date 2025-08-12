package com.susang.stellarVision.application.photo.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.susang.stellarVision.common.s3.S3ResizedImageKeyMapper;
class PhotoServiceImplTest {

    private static final String P_GALLERY = "gallery/";
    private static final String R_GALLERY = "galleryResize/";
    private static final String P_PROFILE = "profile/";
    private static final String R_PROFILE = "profileResize/";


    @Test
    void testGalleryKeyConversion() {
        String key = "gallery/abc.png";
        String expected = "galleryResize/abc.jpg";
        assertEquals(expected,  S3ResizedImageKeyMapper.toResizedKey(key));
    }

    @Test
    void testGalleryKeyConversion2() {
        String key = "gallery/abc.jpg";
        String expected = "galleryResize/abc.jpg";
        assertEquals(expected,  S3ResizedImageKeyMapper.toResizedKey(key));
    }

    @Test
    void testProfileKeyConversion() {
        String key = "profile/user123.jpg";
        String expected = "profileResize/user123.jpg";
        assertEquals(expected, S3ResizedImageKeyMapper.toResizedKey(key));
    }
    @Test
    void testProfileKeyConversion2() {
        String key = "profile/user123.png";
        String expected = "profileResize/user123.jpg";
        assertEquals(expected, S3ResizedImageKeyMapper.toResizedKey(key));
    }

    @Test
    void testNullKey() {
        assertNull(S3ResizedImageKeyMapper.toResizedKey(null));
    }

    @Test
    void testEmptyKey() {
        assertEquals("", S3ResizedImageKeyMapper.toResizedKey(""));
    }

    @Test
    void testNonMatchingKey() {
        String key = "random/path.jpg";
        assertEquals(key, S3ResizedImageKeyMapper.toResizedKey(key)); // 변경 없음
    }


}