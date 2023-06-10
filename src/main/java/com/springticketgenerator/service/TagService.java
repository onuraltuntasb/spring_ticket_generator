package com.springticketgenerator.service;

import com.springticketgenerator.entity.Tag;

public interface TagService {

    Tag updateTag (Tag tag, Long tagId);
    void deleteTag(Long tagId);

}
