package com.susang.stellarVision.application.photo.service;

import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.photo.dto.PhotoResponseDTO;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponseDTO;
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

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public PhotoUploadResponseDTO getPresignedUploadUrl(Long memberId, String originalFilename) {
        String key = s3KeyGenerator.generateKey(S3Directory.GALLERY, memberId, originalFilename);
        String extension = FileExtensionUtil.extractExtension(originalFilename);
        String contentType = ContentTypeMapper.fromExtension(extension);
        String url = s3FileManager.getPresignedUploadUrl(key, contentType);

        return PhotoUploadResponseDTO.builder()
                .uploadUrl(url)
                .s3Key(key)
                .build();
    }

    @Override
    public String getPresignedDownloadUrl(String photoS3Key) {

        return s3FileManager.getPresignedDownloadUrl(photoS3Key);
    }

    @Override
    public PhotoResponseDTO getPhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId));

        return PhotoResponseDTO.builder()
                .id(photo.getId())
                .originalFilename(photo.getTitle())
                .extension(photo.getFileExtension())
                .createdAt(photo.getCreatedAt())
                .downloadUrl(s3FileManager.getPresignedDownloadUrl(photo.getPhotoS3Key()))
                .build();
    }


    @Override
    public Page<PhotoResponseDTO> getPhotosByMemberId(Long memberId, Pageable pageable) {
        Page<Photo> photos = photoRepository.findByMemberId(memberId, pageable);

        return photos.map(photo ->
                PhotoResponseDTO.builder()
                        .id(photo.getId())
                        .originalFilename(photo.getTitle())
                        .extension(photo.getFileExtension())
                        .createdAt(photo.getCreatedAt())
                        .downloadUrl(s3FileManager.getPresignedDownloadUrl(photo.getPhotoS3Key()))
                        .build()
        );
    }
    // Todo : add MemberNotFound , DuplicatedPhoto error
    @Override
    @Transactional
    public void savePhotoMeta(Long memberId, String originalFilename, String s3Key) {
        if (photoRepository.existsByPhotoS3Key(s3Key)){
            throw new DuplicatedPhotoException(s3Key);
        }

        Member member = memberRepository.findById(memberId).get();

        String extension = FileExtensionUtil.extractExtension(originalFilename);

        Photo photo = Photo.builder()
                .photoS3Key(s3Key)
                .title(originalFilename)
                .fileExtension(extension)
                .member(member)
                .build();

        photoRepository.save(photo);
    }



    // 삭제
    @Override
    public void deletePhoto(Long photoId) {
        return;
    }

}
