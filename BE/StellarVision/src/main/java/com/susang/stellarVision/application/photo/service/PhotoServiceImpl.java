package com.susang.stellarVision.application.photo.service;

import com.susang.stellarVision.application.collection.dto.CollectionListResponse;
import com.susang.stellarVision.application.collection.dto.CollectionResponse;
import com.susang.stellarVision.application.collection.repository.CollectionRepository;
import com.susang.stellarVision.application.collection.repository.MemberCollectionRepository;
import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.photo.dto.AiPhotoRequest;
import com.susang.stellarVision.application.photo.dto.AiPhotoResponse;
import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.photo.dto.PhotoTagListResponse;
import com.susang.stellarVision.application.photo.dto.PhotoTagResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponse;
import com.susang.stellarVision.application.photo.dto.Prediction;
import com.susang.stellarVision.application.photo.error.DuplicatedPhotoException;
import com.susang.stellarVision.application.photo.error.PhotoNotFoundException;
import com.susang.stellarVision.application.photo.repository.PhotoRepository;
import com.susang.stellarVision.application.photo.repository.PhotoTagRepository;
import com.susang.stellarVision.common.s3.ContentTypeMapper;
import com.susang.stellarVision.common.s3.FileExtensionUtil;
import com.susang.stellarVision.common.s3.S3Directory;
import com.susang.stellarVision.common.s3.S3FileManager;
import com.susang.stellarVision.common.s3.S3KeyGenerator;
import com.susang.stellarVision.entity.Collection;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.MemberCollection;
import com.susang.stellarVision.entity.Photo;

import com.susang.stellarVision.entity.PhotoTag;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final MemberRepository memberRepository;
    private final S3FileManager s3FileManager;
    private final S3KeyGenerator s3KeyGenerator;
    private final PhotoTagRepository photoTagRepository;
    private final WebClient aiWebClient;
    private final CollectionRepository collectionRepository;
    private final MemberCollectionRepository memberCollectionRepository;

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
    public PhotoResponse getPhoto(Long photoId) {
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
    public Photo savePhotoMeta(Long memberId, String originalFilename, String s3Key) {
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

        return photo;
    }

    @Transactional
    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId.toString()) {
                });
        photoTagRepository.deleteByPhotoId(photoId);
        s3FileManager.delete(photo.getPhotoS3Key());
        photoRepository.delete(photo);
    }

    @Override
    @Transactional(readOnly = true)
    public PhotoTagListResponse getTagsByPhotoId(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId.toString()));
        List<PhotoTag> tags = photoTagRepository.findAllByPhoto(photo);
        List<PhotoTagResponse> tagList = tags.stream()
                .map(tag -> new PhotoTagResponse(tag.getId(), tag.getTagName())).toList();

        return new PhotoTagListResponse(tagList);

    }
    private List<String> analyzePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId.toString()));

        String imageUrl = s3FileManager.getPresignedDownloadUrl(photo.getPhotoS3Key());
        AiPhotoRequest requestBody = new AiPhotoRequest(photoId, imageUrl);

        AiPhotoResponse response = aiWebClient.post()
                .uri("https://i13c106.p.ssafy.io/api/detect/photo")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AiPhotoResponse.class)
                .block();
        log.debug("[AI 태그 분석 결과] result={}", response);
        if (response == null || response.getPredictions() == null) {
            log.debug("AI 분석 결과 없음. photoId={}", photoId);
            return List.of();
        }

        return response.getPredictions().stream()
                .map(Prediction::getClassName)
                .distinct()
                .toList();
    }
    @Transactional
    public List<Collection> saveTagsAndCollections(Long photoId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId.toString()));

        Member member = photo.getMember();
        List<Collection> allCollections = collectionRepository.findAll();

        for (String tag : tags) {
            String koreanName = allCollections.stream()
                    .filter(c -> c.getAbbreviation().equals(tag))
                    .map(Collection::getKoreanName)
                    .findFirst()
                    .orElse(null);

            String tagName = (koreanName != null) ? koreanName : tag;

            PhotoTag photoTag = PhotoTag.builder()
                    .tagName(tagName)
                    .photo(photo)
                    .build();

            photoTagRepository.save(photoTag);
        }


        List<Collection> newCollections = new java.util.ArrayList<>();
        List<Collection> matchedCollections = collectionRepository.findByAbbreviationIn(tags);
        Set<Integer> existingIds = memberCollectionRepository.findCollectionIdsByMemberId(member.getId());
        for (Collection collection : matchedCollections) {
            if (existingIds.contains(collection.getId().intValue())) {
                continue;
            }

            MemberCollection memberCollection = MemberCollection.builder()
                    .collection(collection)
                    .member(member)
                    .build();
            memberCollectionRepository.save(memberCollection);
            newCollections.add(collection);
        }


        return newCollections;
    }
    @Transactional
    public PhotoUploadCompleteResponse completeUpload(PhotoUploadCompleteRequest request) {
        log.debug("[사진 업로드 완료] memberId={}, originalFilename={}, s3Key={}",
                request.getMemberId(), request.getOriginalFilename(), request.getS3Key());
        Photo photo = savePhotoMeta(request.getMemberId(), request.getOriginalFilename() ,request.getS3Key());
        log.debug("[사진 메타 저장 완료] photoId={}", photo.getId());
        List<String> tags = analyzePhoto(photo.getId());
        log.debug("[AI 태그 분석 완료] photoId={}, tags={}", photo.getId(), tags);

        List<Collection> newCollections = saveTagsAndCollections(photo.getId(), tags);
        log.debug("[컬렉션 저장 완료] photoId={}, newCollectionCount={}", photo.getId(), newCollections.size());

        List<CollectionResponse> collectionResponses = newCollections.stream()
                .map(c -> CollectionResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .abbreviation(c.getAbbreviation())
                        .koreanName(c.getKoreanName())
                        .collected(true)
                        .build())
                .toList();

        CollectionListResponse collectionListResponse = new CollectionListResponse(collectionResponses);


        return PhotoUploadCompleteResponse.builder()
                .title(photo.getTitle())
                .creationDate(photo.getCreatedAt())
                .tagList(getTagsByPhotoId(photo.getId()))
                .collectionList(collectionListResponse)
                .build();

    }



}
