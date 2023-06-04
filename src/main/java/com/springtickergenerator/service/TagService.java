package com.springtickergenerator.service;

import com.springtickergenerator.entity.Tag;

public interface TagService {

    Tag updateTag (Tag tag, Long tagId);

    void deleteTag(Long tagId);

}
