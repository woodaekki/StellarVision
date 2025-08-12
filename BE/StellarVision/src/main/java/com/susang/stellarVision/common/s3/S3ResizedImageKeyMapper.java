package com.susang.stellarVision.common.s3;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

@UtilityClass
public class S3ResizedImageKeyMapper {
    private static final String P_GALLERY = "gallery/";
    private static final String R_GALLERY = "galleryResize/";
    private static final String P_PROFILE = "profile/";
    private static final String R_PROFILE = "profileResize/";
    private static final String DEFAULT_PROFILE_IMAGE_KEY = "profile/profile.png";

    public static String toResizedKey(String key) {
        if (key == null || key.isBlank()) return key;

        if (key.startsWith(R_GALLERY) || key.startsWith(R_PROFILE)) return key;
        if (DEFAULT_PROFILE_IMAGE_KEY.equals(key)) return key;

        if (key.startsWith(P_GALLERY)) {
            String filename = key.substring(P_GALLERY.length()); // e.g. abc.png or user1/photo.png
            int dotIndex = filename.lastIndexOf('.');
            String nameWithoutExt = (dotIndex != -1) ? filename.substring(0, dotIndex) : filename;
            return R_GALLERY + nameWithoutExt + ".jpg";
        }
        if (key.startsWith(P_PROFILE)) {
            String filename = key.substring(P_PROFILE.length()); // e.g. user123.jpg
            int dotIndex = filename.lastIndexOf('.');
            String nameWithoutExt = (dotIndex != -1) ? filename.substring(0, dotIndex) : filename;
            return R_PROFILE + nameWithoutExt + ".jpg";
        }
        return key;
    }

}
