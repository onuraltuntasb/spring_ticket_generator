package com.springticketgenerator.service;

import com.springticketgenerator.entity.Tag;
import com.springticketgenerator.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceImplTest {

    @InjectMocks
    private TagServiceImpl underTest;

    @Mock
    private TagRepository tagRepository;


    @BeforeEach
    void setUp(){

    }

    @Test
    void canUpdateTag(){

        Tag tag = Tag.builder()
                .id(1L)
                .name("tag1")
                .events(null)
                .build();

        tag.setName("tag1Updated");

        //when

        when(tagRepository.save(any())).then(returnsFirstArg());
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Tag rTag = underTest.updateTag(tag,1L);

        assertThat(rTag.getName()).isEqualTo(tag.getName());

        verify(tagRepository,times(1)).save(rTag);
        verify(tagRepository,times(1)).findById(1L);



    }



}
