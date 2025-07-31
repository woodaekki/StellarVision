package com.susang.stellarVision.application.photo.service;

import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponse;
import com.susang.stellarVision.application.photo.error.DuplicatedPhotoException;
import com.susang.stellarVision.application.photo.error.PhotoNotFoundException;
import com.susang.stellarVision.application.photo.repository.PhotoRepository;
import com.susang.stellarVision.common.s3.ContentTypeMapper;
import com.susang.stellarVision.common.s3.FileExtensionUtil;
import com.susang.stellarVision.common.s3.S3Directory;
import com.susang.stellarVision.common.s3.S3FileManager;
import com.susang.stellarVision.common.s3.S3KeyGenerator;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.Photo;

import com.susang.stellarVision.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final MemberRepository memberRepository;
    private final S3FileManager s3FileManager;
    private final S3KeyGenerator s3KeyGenerator;


    public PhotoUploadResponse generatePresignedUploadUrl(S3Directory directory, Long memberId,
            String originalFilename) {
        String key = s3KeyGenerator.generateKey(directory, memberId, originalFilename);
        String extension = FileExtensionUtil.extractExtension(originalFilename);
        String contentType = ContentTypeMapper.fromExtension(extension);
        String url = s3FileManager.getPresignedUploadUrl(key, contentType);

        return PhotoUploadResponse.builder().uploadUrl(url).s3Key(key).build();
    }

    public PhotoUploadResponse getGalleryPresignedUploadUrl(Long memberId,
            String originalFilename) {
        return generatePresignedUploadUrl(S3Directory.GALLERY, memberId, originalFilename);
    }

    public PhotoUploadResponse getProfilePresignedUploadUrl(Long memberId,
            String originalFilename) {
        return generatePresignedUploadUrl(S3Directory.PROFILE, memberId, originalFilename);
    }


    @Override
    public PhotoResponse getPhoto(Long photoId) throws PhotoNotFoundException  {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId.toString()) {
                });

        return PhotoResponse.builder().id(photo.getId()).originalFilename(photo.getTitle())
                .extension(photo.getFileExtension()).createdAt(photo.getCreatedAt())
                .downloadUrl(s3FileManager.getPresignedDownloadUrl(photo.getPhotoS3Key())).build();
    }


    @Override
    public Page<PhotoResponse> getPhotosByMemberId(Long memberId, Pageable pageable) {
        Page<Photo> photos = photoRepository.findByMemberId(memberId, pageable);

        return photos.map(photo -> PhotoResponse.builder().id(photo.getId())
                .originalFilename(photo.getTitle()).extension(photo.getFileExtension())
                .createdAt(photo.getCreatedAt())
                .downloadUrl(s3FileManager.getPresignedDownloadUrl(photo.getPhotoS3Key())).build());
    }


    @Override
    @Transactional
    public void savePhotoMeta(Long memberId, String originalFilename, String s3Key) throws MemberNotFoundException  {
        if (photoRepository.existsByPhotoS3Key(s3Key)) {
            throw new DuplicatedPhotoException(s3Key);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()) {
                });
        String extension = FileExtensionUtil.extractExtension(originalFilename);

        Photo photo = Photo.builder().photoS3Key(s3Key).title(originalFilename)
                .fileExtension(extension).member(member).build();

        photoRepository.save(photo);
    }

    @Transactional
    public void deletePhoto(Long photoId) throws PhotoNotFoundException  {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId.toString()) {
                });
        s3FileManager.delete(photo.getPhotoS3Key());
        photoRepository.delete(photo);
    }

}
