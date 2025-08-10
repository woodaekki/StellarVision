package com.susang.stellarVision.application.video.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.video.repository.ThumbnailRepository;
import com.susang.stellarVision.application.video.repository.VideoRepository;
import com.susang.stellarVision.application.video.repository.VideoTagRepository;
import com.susang.stellarVision.common.s3.S3Directory;
import com.susang.stellarVision.common.s3.S3FileManager;
import com.susang.stellarVision.common.s3.S3KeyGenerator;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.Thumbnail;
import com.susang.stellarVision.entity.Video;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VideoServiceImplTest {

	@Mock private VideoRepository videoRepository;
	@Mock private S3FileManager s3FileManager;
	@Mock private VideoTagRepository videoTagRepository;
	@Mock private S3KeyGenerator s3KeyGenerator;
	@Mock private MemberRepository memberRepository;
	@Mock private ThumbnailRepository thumbnailRepository;

	@InjectMocks
	private VideoServiceImpl videoService;

	private Member member;

	@BeforeEach
	void setUp() throws Exception {
		Constructor<Member> ctor = Member.class.getDeclaredConstructor();
		ctor.setAccessible(true);
		member = ctor.newInstance();
		ReflectionTestUtils.setField(member, "id", 1L);
	}

	@Test
	void uploadVideo_shouldCreateExpectedThumbnailKey_andPersistIt() throws Exception {
		// given
		Long memberId = 1L;
		String title = "myvideo";
		String videoKey = "video/1/myvideo.mp4";

		when(s3KeyGenerator.generateKey(eq(S3Directory.VIDEO), eq(memberId), anyString()))
				.thenReturn(videoKey);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		doNothing().when(s3FileManager).uploadFile(any(InputStream.class), anyLong(), anyString(), anyString());

		// when
		InputStream input = new ByteArrayInputStream(new byte[0]);
		videoService.uploadVideo(input, 0L, title, memberId);

		// then
		// Thumbnail 저장된 키 검증
		ArgumentCaptor<Thumbnail> thumbCap = ArgumentCaptor.forClass(Thumbnail.class);
		verify(thumbnailRepository).save(thumbCap.capture());
		assertThat(thumbCap.getValue().getThumbnailS3Key())
				.isEqualTo("thumbnail/1/myvideo.jpg");

		// Video 저장 시 연결 검증
		ArgumentCaptor<Video> videoCap = ArgumentCaptor.forClass(Video.class);
		verify(videoRepository).save(videoCap.capture());
		Video saved = videoCap.getValue();
		assertThat(saved.getVideoS3Key()).isEqualTo(videoKey);
		assertThat(saved.getTitle()).isEqualTo(title);
		assertThat(saved.getMember()).isEqualTo(member);
		assertThat(saved.getThumbnail()).isNotNull();
		assertThat(saved.getThumbnail().getThumbnailS3Key())
				.isEqualTo("thumbnail/1/myvideo.jpg");

		// S3 업로드 호출 키 검증
		verify(s3FileManager).uploadFile(any(InputStream.class), eq(0L), eq("video/mp4"), eq(videoKey));

		verifyNoMoreInteractions(videoTagRepository);
	}

	@Test
	void uploadVideo_shouldThrow_whenMemberNotFound() {
		Long memberId = 99L;
		when(s3KeyGenerator.generateKey(eq(S3Directory.VIDEO), eq(memberId), anyString()))
				.thenReturn("video/99/abc.mp4");
		when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

		org.junit.jupiter.api.Assertions.assertThrows(
				MemberNotFoundException.class,
				() -> videoService.uploadVideo(new ByteArrayInputStream(new byte[0]), 0L, "abc", memberId)
		);
	}
}